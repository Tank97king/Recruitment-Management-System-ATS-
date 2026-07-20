package com.example.ats.service.impl;

import com.example.ats.config.FileStorageProperties;
import com.example.ats.exception.FileStorageException;
import com.example.ats.exception.InvalidFileTypeException;
import com.example.ats.exception.FileNotFoundException;
import com.example.ats.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service implementation for local file storage management.
 */
@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageServiceImpl(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not create the base uploads directory.", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subFolder, String customFileName) {
        validatePdf(file);

        String targetFolder = subFolder == null ? "" : subFolder;
        Path targetPath = this.fileStorageLocation.resolve(targetFolder).normalize();

        try {
            Files.createDirectories(targetPath);
            Path targetFile = targetPath.resolve(customFileName).normalize();
            
            // Path traversal prevention security check
            if (!targetFile.startsWith(this.fileStorageLocation)) {
                throw new FileStorageException("Cannot store file outside the base uploads directory.");
            }

            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            return targetFile.toString();
        } catch (IOException e) {
            throw new FileStorageException("Could not store file " + customFileName + ".", e);
        }
    }

    @Override
    public Path loadFile(String filePath) {
        try {
            Path file = Paths.get(filePath).toAbsolutePath().normalize();
            
            // Path traversal protection security check
            if (!file.startsWith(this.fileStorageLocation)) {
                throw new FileStorageException("Cannot read file outside the base uploads directory.");
            }
            
            if (Files.exists(file) && Files.isReadable(file)) {
                return file;
            } else {
                throw new FileNotFoundException("File not found on disk: " + filePath);
            }
        } catch (Exception e) {
            if (e instanceof FileNotFoundException || e instanceof FileStorageException) {
                throw e;
            }
            throw new FileNotFoundException("Could not load file: " + filePath, e);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return;
        }
        try {
            Path file = Paths.get(filePath).toAbsolutePath().normalize();
            if (Files.exists(file)) {
                Files.delete(file);
                log.info("Deleted physical file from disk: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete physical file from disk: {}", filePath, e);
        }
    }

    @Override
    public void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileTypeException("File is empty.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new InvalidFileTypeException("Only PDF files are allowed. Rejected content type: " + contentType);
        }
    }

    @Override
    public String generateUniqueFileName(String originalFileName) {
        String cleanName = StringUtils.cleanPath(originalFileName == null ? "resume.pdf" : originalFileName);
        String extension = ".pdf";
        int dotIndex = cleanName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = cleanName.substring(dotIndex);
        }
        if (!extension.equalsIgnoreCase(".pdf")) {
            extension = ".pdf";
        }
        return UUID.randomUUID().toString() + extension;
    }
}
