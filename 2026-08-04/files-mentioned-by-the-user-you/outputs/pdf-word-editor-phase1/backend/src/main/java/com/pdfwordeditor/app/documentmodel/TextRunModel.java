package com.pdfwordeditor.app.documentmodel;

public record TextRunModel(
    String id,
    String text,
    String fontFamily,
    double fontSize,
    String fontWeight,
    String fontStyle,
    String color,
    BoundingBox boundingBox,
    double rotation) {}
