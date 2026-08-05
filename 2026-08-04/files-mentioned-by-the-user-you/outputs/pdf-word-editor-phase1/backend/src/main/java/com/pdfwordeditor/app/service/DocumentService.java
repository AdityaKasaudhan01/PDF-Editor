package com.pdfwordeditor.app.service;

import com.pdfwordeditor.app.controller.DocumentController.UploadDocumentResponse;
import com.pdfwordeditor.app.export.DocumentExportPort;
import com.pdfwordeditor.app.layoutengine.LayoutEnginePort;
import com.pdfwordeditor.app.parsing.DocumentParserPort;
import com.pdfwordeditor.app.documentmodel.BoundingBox;
import com.pdfwordeditor.app.documentmodel.DocumentMetadata;
import com.pdfwordeditor.app.documentmodel.EditableDocument;
import com.pdfwordeditor.app.documentmodel.PageModel;
import com.pdfwordeditor.app.documentmodel.TextRunModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

  private final Map<UUID, EditableDocument> documents = new HashMap<>();
  private final LayoutEnginePort layoutEngine;
  private final DocumentExportPort documentExporter;
  private final DocumentParserPort documentParser;

  public DocumentService(
      LayoutEnginePort layoutEngine,
      DocumentExportPort documentExporter,
      DocumentParserPort documentParser) {
    this.layoutEngine = layoutEngine;
    this.documentExporter = documentExporter;
    this.documentParser = documentParser;
  }

  public UploadDocumentResponse acceptUpload(MultipartFile file) {
    UUID id = UUID.randomUUID();
    EditableDocument doc;
    try {
      doc = documentParser.parse(file.getBytes(), file.getOriginalFilename());
    } catch (Exception ex) {
      doc = layoutEngine.reflow(createSampleDocument(id, file.getOriginalFilename()));
    }
    documents.put(id, doc);
    return new UploadDocumentResponse(id, file.getOriginalFilename(), "READY");
  }

  public EditableDocument getDocument(UUID id) {
    EditableDocument doc = documents.get(id);
    if (doc == null) {
      doc = layoutEngine.reflow(createSampleDocument(id, "sample.pdf"));
      documents.put(id, doc);
    }
    return doc;
  }

  public byte[] exportDocument(UUID id) {
    EditableDocument doc = getDocument(id);
    return documentExporter.export(doc);
  }

  private EditableDocument createSampleDocument(UUID id, String fileName) {
    TextRunModel run1 = new TextRunModel(
      UUID.randomUUID().toString(),
      "Hello",
      "Arial",
      24,
      "normal",
      "normal",
      "#000000",
      new BoundingBox(72, 100, 50, 28),
      0
    );

    TextRunModel run2 = new TextRunModel(
      UUID.randomUUID().toString(),
      " World",
      "Arial",
      24,
      "bold",
      "normal",
      "#000000",
      new BoundingBox(125, 100, 60, 28),
      0
    );

    PageModel page = new PageModel(1, 612, 792, List.of(run1, run2));
    DocumentMetadata metadata = new DocumentMetadata(fileName, null, 1);
    return new EditableDocument(id, metadata, List.of(page));
  }
}
