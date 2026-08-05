package com.pdfwordeditor.app.pdfparser;

import com.pdfwordeditor.app.documentmodel.EditableDocument;
import java.io.InputStream;

public interface PdfParserPort {
  EditableDocument parse(InputStream pdfStream);
}
