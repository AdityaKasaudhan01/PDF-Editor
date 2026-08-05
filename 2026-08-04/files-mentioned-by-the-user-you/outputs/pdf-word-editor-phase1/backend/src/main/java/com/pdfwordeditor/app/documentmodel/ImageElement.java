package com.pdfwordeditor.app.documentmodel;

public record ImageElement(
    String id,
    String storageKey,
    String contentType,
    BoundingBox boundingBox,
    double rotation) {}
