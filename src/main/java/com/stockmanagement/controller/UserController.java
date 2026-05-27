package com.stockmanagement.controller;

import com.stockmanagement.dto.PasswordResetDTO;
import com.stockmanagement.dto.UserCreateDTO;
import com.stockmanagement.dto.UserResponseDTO;
import com.stockmanagement.dto.UserUpdateDTO;
import com.stockmanagement.entity.User;
import com.stockmanagement.entity.UserRole;
import com.stockmanagement.exception.DuplicateResourceException;
import com.stockmanagement.exception.ResourceNotFoundException;
import com.stockmanagement.repository.UserRepository;
import com.stockmanagement.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String listUsers(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(defaultValue = "createdAt") String sort,
                            @RequestParam(defaultValue = "desc") String direction,
                            @RequestParam(required = false) String search,
                            Model model) {

        // Whitelist valid sort fields to avoid invalid property errors
        if (!sort.equals("createdAt") &&
                !sort.equals("username") &&
                !sort.equals("firstName") &&
                !sort.equals("email")) {
            logger.warn("Invalid sort field '{}' received. Falling back to 'createdAt'", sort);
            sort = "createdAt";
        }

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<UserResponseDTO> users;
        try {
            if (search != null && !search.trim().isEmpty()) {
                users = userService.searchUsers(search.trim(), pageable);
                model.addAttribute("search", search);
            } else {
                users = userService.getAllUsers(pageable);
            }
        } catch (Exception ex) {
            logger.error("Failed to load users list", ex);
            users = Page.empty(pageable);
            model.addAttribute("errorMessage", "Failed to load users. Please try again.");
        }

        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("totalElements", users.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        return "users/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("userCreateDTO", new UserCreateDTO());
        model.addAttribute("roles", UserRole.values());
        return "users/create";
    }

    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute UserCreateDTO userCreateDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication,
                             Model model) {

        logger.info("=== USER CREATION DEBUG ===");
        logger.info("Received userCreateDTO: {}", userCreateDTO);
        logger.info("Role from form: {}", userCreateDTO.getRole());
        logger.info("Binding result has errors: {}", bindingResult.hasErrors());
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error ->
                    logger.error("Validation error: {} - {}",
                            error instanceof org.springframework.validation.FieldError ?
                                    ((org.springframework.validation.FieldError) error).getField() : "global",
                            error.getDefaultMessage()));
        }

        // Custom validation
        if (!userCreateDTO.getPassword().equals(userCreateDTO.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword",
                    "Passwords do not match");
        }

        if (userService.existsByUsername(userCreateDTO.getUsername())) {
            bindingResult.rejectValue("username", "error.username",
                    "Username already exists");
        }

        if (userService.existsByEmail(userCreateDTO.getEmail())) {
            bindingResult.rejectValue("email", "error.email",
                    "Email already exists");
        }

        if (bindingResult.hasErrors()) {
            logger.info("Returning to create form due to validation errors");
            model.addAttribute("roles", UserRole.values());
            return "users/create";
        }

        try {
            logger.info("Create user requested with role: {}", userCreateDTO.getRole());
            if (userCreateDTO.getRole() == null) {
                bindingResult.rejectValue("role", "error.role", "Role is required");
                model.addAttribute("roles", UserRole.values());
                return "users/create";
            }
            User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

            UserResponseDTO createdUser = userService.createUser(userCreateDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + createdUser.getUsername() + "' created successfully!");

            logger.info("User {} created successfully by {}",
                    createdUser.getUsername(), authentication.getName());

        } catch (DuplicateResourceException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users/create";
        } catch (Exception e) {
            logger.error("Error creating user", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An error occurred while creating the user. Please try again.");
            return "redirect:/users/create";
        }

        return "redirect:/users";
    }

    @GetMapping("/{userId}/edit")
    public String showEditForm(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
        Optional<UserResponseDTO> userOpt = userService.getUserById(userId);

        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found!");
            return "redirect:/users";
        }

        UserResponseDTO user = userOpt.get();
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setEmail(user.getEmail());
        userUpdateDTO.setFirstName(user.getFirstName());
        userUpdateDTO.setLastName(user.getLastName());
        userUpdateDTO.setRole(user.getRole());

        model.addAttribute("userUpdateDTO", userUpdateDTO);
        model.addAttribute("user", user);
        model.addAttribute("roles", UserRole.values());

        return "users/edit";
    }

    @PostMapping("/{userId}/edit")
    public String updateUser(@PathVariable Long userId,
                             @Valid @ModelAttribute UserUpdateDTO userUpdateDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication,
                             Model model) {

        if (bindingResult.hasErrors()) {
            Optional<UserResponseDTO> userOpt = userService.getUserById(userId);
            model.addAttribute("user", userOpt.orElse(null));
            model.addAttribute("roles", UserRole.values());
            return "users/edit";
        }

        try {
            User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

            UserResponseDTO updatedUser = userService.updateUser(userId, userUpdateDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + updatedUser.getUsername() + "' updated successfully!");

            logger.info("User {} updated successfully by {}",
                    updatedUser.getUsername(), authentication.getName());

        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating user with ID: {}", userId, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An error occurred while updating the user. Please try again.");
        }

        return "redirect:/users";
    }

    @GetMapping("/{userId}")
    public String viewUser(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
        Optional<UserResponseDTO> userOpt = userService.getUserById(userId);

        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found!");
            return "redirect:/users";
        }

        model.addAttribute("user", userOpt.get());
        return "users/view";
    }

    @PostMapping("/{userId}/delete")
    public String deleteUser(@PathVariable Long userId,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication) {
        try {
            User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

            // Prevent users from deleting themselves
            Optional<UserResponseDTO> userToDelete = userService.getUserById(userId);
            if (userToDelete.isPresent() && userToDelete.get().getUsername().equals(authentication.getName())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "You cannot delete your own account!");
                return "redirect:/users";
            }

            userService.deleteUser(userId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");

            logger.info("User with ID {} deleted successfully by {}", userId, authentication.getName());

        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting user with ID: {}", userId, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An error occurred while deleting the user. Please try again.");
        }

        return "redirect:/users";
    }

    @PostMapping("/{userId}/toggle-status")
    public String toggleUserStatus(@PathVariable Long userId,
                                   RedirectAttributes redirectAttributes,
                                   Authentication authentication) {
        try {
            User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

            userService.toggleUserStatus(userId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully!");

            logger.info("User status with ID {} toggled successfully by {}", userId, authentication.getName());

        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Error toggling user status with ID: {}", userId, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An error occurred while updating user status. Please try again.");
        }

        return "redirect:/users";
    }

    @GetMapping("/{userId}/reset-password")
    public String showResetPasswordForm(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
        Optional<UserResponseDTO> userOpt = userService.getUserById(userId);

        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found!");
            return "redirect:/users";
        }

        model.addAttribute("passwordResetDTO", new PasswordResetDTO());
        model.addAttribute("user", userOpt.get());

        return "users/reset-password";
    }

    @PostMapping("/{userId}/reset-password")
    public String resetPassword(@PathVariable Long userId,
                                @Valid @ModelAttribute PasswordResetDTO passwordResetDTO,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Authentication authentication,
                                Model model) {

        if (!passwordResetDTO.getNewPassword().equals(passwordResetDTO.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword",
                    "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            Optional<UserResponseDTO> userOpt = userService.getUserById(userId);
            model.addAttribute("user", userOpt.orElse(null));
            return "users/reset-password";
        }

        try {
            User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

            userService.resetPassword(userId, passwordResetDTO.getNewPassword(), currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Password reset successfully!");

            logger.info("Password reset for user ID {} by {}", userId, authentication.getName());

        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Error resetting password for user ID: {}", userId, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An error occurred while resetting password. Please try again.");
        }

        return "redirect:/users";
    }

    // AJAX endpoints for better user experience
    @GetMapping("/check-username")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", userService.existsByUsername(username));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-email")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", userService.existsByEmail(email));
        return ResponseEntity.ok(response);
    }
}