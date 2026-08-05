package com.pdfwordeditor.app.imagemanager;

public interface ImageManagerPort {
  String storeImage(byte[] bytes, String contentType);

  byte[] loadImage(String storageKey);
}
