package com.pdfwordeditor.app.word;

import com.pdfwordeditor.app.documentmodel.EditableDocument;
import java.io.InputStream;

public interface WordConverterPort {
  byte[] exportDocx(EditableDocument document);

  EditableDocument importDocx(InputStream docxStream);
}
