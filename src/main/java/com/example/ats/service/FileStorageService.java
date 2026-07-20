package com.example.ats.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * Service interface for local file storage management.
 */
public interface FileStorageService {

    /**
     * Stores a multipart file on the local filesystem.
     *
     * @param file           the multipart file to store
     * @param subFolder      the sub-folder layout under the uploads root
     * @param customFileName the unique filename to write as
     * @return the absolute target file path
     */
    String storeFile(MultipartFile file, String subFolder, String customFileName);

    /**
     * Loads a file from path as a Path object.
     *
     * @param filePath path of the target file
     * @return the Path pointing to the file
     */
    Path loadFile(String filePath);

    /**
     * Deletes a file from the local disk if it exists.
     *
     * @param filePath path of the target file
     */
    void deleteFile(String filePath);

    /**
     * Validates that the uploaded file is of type PDF.
     *
     * @param file the multipart file to check
     */
    void validatePdf(MultipartFile file);

    /**
     * Generates a unique filename using a random UUID while preserving the extension.
     *
     * @param originalFileName the original name of the uploaded file
     * @return the generated unique filename
     */
    String generateUniqueFileName(String originalFileName);
}
