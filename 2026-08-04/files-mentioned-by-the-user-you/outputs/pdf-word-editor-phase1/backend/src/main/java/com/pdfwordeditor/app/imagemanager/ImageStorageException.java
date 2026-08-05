package com.pdfwordeditor.app.imagemanager;

public class ImageStorageException extends RuntimeException {
  public ImageStorageException(String message) {
    super(message);
  }

  public ImageStorageException(String message, Throwable cause) {
    super(message, cause);
  }
}
