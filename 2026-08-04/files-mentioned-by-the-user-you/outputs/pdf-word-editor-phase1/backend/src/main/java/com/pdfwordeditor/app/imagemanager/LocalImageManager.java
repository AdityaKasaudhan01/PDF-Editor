package com.pdfwordeditor.app.imagemanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalImageManager implements ImageManagerPort {

  private final String root;

  public LocalImageManager(@Value("${storage.local-root:./storage}") String root) {
    this.root = root;
  }

  @Override
  public String storeImage(byte[] bytes, String contentType) {
    try {
      Path dir = Paths.get(root, "images");
      Files.createDirectories(dir);
      String key = UUID.randomUUID().toString();
      Files.write(dir.resolve(key), bytes);
      return key;
    } catch (IOException e) {
      throw new ImageStorageException("Failed to store image", e);
    }
  }

  @Override
  public byte[] loadImage(String storageKey) {
    try {
      return Files.readAllBytes(Paths.get(root, "images", storageKey));
    } catch (IOException e) {
      throw new ImageStorageException("Failed to load image", e);
    }
  }
}
