// UserMapper.java
package com.stockmanagement.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockmanagement.dto.UserResponseDTO;
import com.stockmanagement.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    @Autowired
    private ObjectMapper objectMapper;

    public UserResponseDTO toResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setFullName(buildFullName(user.getFirstName(), user.getLastName()));
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        if (user.getCreatedBy() != null) {
            dto.setCreatedBy(buildFullName(user.getCreatedBy().getFirstName(), user.getCreatedBy().getLastName()));
        }

        if (user.getUpdatedBy() != null) {
            dto.setUpdatedBy(buildFullName(user.getUpdatedBy().getFirstName(), user.getUpdatedBy().getLastName()));
        }

        return dto;
    }

    public String toJson(User user) {
        try {
            return objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String buildFullName(String firstName, String lastName) {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();

        if (first.isEmpty() && last.isEmpty()) {
            return "";
        }

        if (first.isEmpty()) {
            return last;
        }

        if (last.isEmpty()) {
            return first;
        }

        return first + " " + last;
    }
}