package com.stockmanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;

    /**
     * Stores a file in the specified directory
     *
     * @param file         The file to store
     * @param subDirectory The subdirectory within the upload directory
     * @return The relative path to the stored file
     * @throws IOException If an error occurs during file storage
     */
    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        // Create directories if they don't exist
        String directory = uploadDir + File.separator + subDirectory;
        Path directoryPath = Paths.get(directory);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // Generate a unique filename to prevent overwriting existing files
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + fileExtension;

        // Save the file
        Path filePath = directoryPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return the relative path that can be used in URLs
        return subDirectory + "/" + filename;
    }

    /**
     * Deletes a file from the file system
     *
     * @param relativePath The relative path of the file to delete
     * @return true if the file was deleted successfully, false otherwise
     */
    public boolean deleteFile(String relativePath) {
        try {
            Path filePath = Paths.get(uploadDir + File.separator + relativePath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
}