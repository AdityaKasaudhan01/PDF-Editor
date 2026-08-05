package com.pdfwordeditor.app.historymanager;

import com.pdfwordeditor.app.documentmodel.EditableDocument;

public interface HistoryManagerPort {
  void recordSnapshot(EditableDocument document, String reason);
}
