package com.pdfwordeditor.app.parsing;

import com.pdfwordeditor.app.documentmodel.EditableDocument;

public interface DocumentParserPort {
  EditableDocument parse(byte[] content, String fileName);
}
