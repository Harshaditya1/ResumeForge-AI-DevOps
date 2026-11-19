package com.resumeforge.resume;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir = "uploads";

    public String storeFile(MultipartFile file) throws IOException {

        String originalFileName = file.getOriginalFilename();

        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String storedFileName = UUID.randomUUID() + extension;

        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Files.copy(
                file.getInputStream(),
                uploadPath.resolve(storedFileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        return storedFileName;
    }

    /**
     * Delete stored resume file.
     */
    public void deleteFile(String storedFileName) throws IOException {

        if (storedFileName == null || storedFileName.isBlank()) {
            return;
        }

        Path filePath = Paths.get(uploadDir).resolve(storedFileName);

        Files.deleteIfExists(filePath);
    }

    /**
     * Returns the path of a stored resume file.
     */
    public Path getFilePath(String storedFileName) {
        return Paths.get(uploadDir).resolve(storedFileName);
    }
}
