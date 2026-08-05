package com.pdfwordeditor.app.export;

import com.pdfwordeditor.app.documentmodel.EditableDocument;

public interface DocumentExportPort {
  byte[] export(EditableDocument document);
}
