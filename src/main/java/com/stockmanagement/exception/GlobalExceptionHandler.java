
// GlobalExceptionHandler.java
package com.stockmanagement.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFoundException(ResourceNotFoundException ex,
                                                  RedirectAttributes redirectAttributes) {
        logger.error("Resource not found: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/admin/dashboard";
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateResourceException(DuplicateResourceException ex,
                                                   RedirectAttributes redirectAttributes) {
        logger.error("Duplicate resource: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/users";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public String handleMaxSizeException(MaxUploadSizeExceededException ex, RedirectAttributes redirectAttributes) {
        logger.error("File too large: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage",
                "The uploaded file is too large. Maximum file size allowed is 5MB.");

        // Return to appropriate page based on the request path
        // You might need to customize this based on your application's URL structure
        return "redirect:/staff";
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMultipartException(MultipartException ex, RedirectAttributes redirectAttributes) {
        logger.error("Multipart error: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage",
                "There was an error processing the file upload. Please try again with a smaller file (max 5MB).");
        return "redirect:/staff";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, RedirectAttributes redirectAttributes) {
        logger.error("Unexpected error occurred", ex);
        redirectAttributes.addFlashAttribute("errorMessage",
                "An unexpected error occurred. Please try again.");
        return "redirect:/admin/dashboard";
    }
}

