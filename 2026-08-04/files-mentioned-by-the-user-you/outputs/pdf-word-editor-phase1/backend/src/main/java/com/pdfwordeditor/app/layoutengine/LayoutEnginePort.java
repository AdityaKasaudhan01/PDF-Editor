package com.pdfwordeditor.app.layoutengine;

import com.pdfwordeditor.app.documentmodel.EditableDocument;

public interface LayoutEnginePort {
  EditableDocument reflow(EditableDocument document);
}
