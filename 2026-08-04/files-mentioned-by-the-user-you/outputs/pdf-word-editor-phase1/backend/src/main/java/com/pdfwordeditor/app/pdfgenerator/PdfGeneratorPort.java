package com.pdfwordeditor.app.pdfgenerator;

import com.pdfwordeditor.app.documentmodel.EditableDocument;

public interface PdfGeneratorPort {
  byte[] generate(EditableDocument document);
}
