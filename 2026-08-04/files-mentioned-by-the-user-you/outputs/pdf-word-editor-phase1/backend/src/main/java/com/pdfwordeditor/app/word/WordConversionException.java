package com.pdfwordeditor.app.word;

public class WordConversionException extends RuntimeException {
  public WordConversionException(String message) {
    super(message);
  }

  public WordConversionException(String message, Throwable cause) {
    super(message, cause);
  }
}
