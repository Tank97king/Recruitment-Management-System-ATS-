package com.example.ats.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a file storage operation fails.
 *
 * <p>Maps to HTTP 500 Internal Server Error, because file I/O failures
 * are infrastructure-level problems, not client mistakes.
 *
 * <p>Usage:
 * <pre>{@code
 *   try {
 *       Files.copy(inputStream, targetPath);
 *   } catch (IOException e) {
 *       throw new FileStorageException("Failed to store resume file: " + filename, e);
 *   }
 * }</pre>
 */
public class FileStorageException extends AtsBaseException {

    public FileStorageException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
