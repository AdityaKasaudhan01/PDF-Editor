package com.pdfwordeditor.app.renderengine;

import com.pdfwordeditor.app.documentmodel.EditableDocument;

public interface RenderEnginePort {
  byte[] renderPagePreview(EditableDocument document, int pageNumber);
}
