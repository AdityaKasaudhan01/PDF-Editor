package com.pdfwordeditor.app.documentmodel;

import java.util.List;
import java.util.UUID;

public record EditableDocument(UUID id, DocumentMetadata metadata, List<PageModel> pages) {}
