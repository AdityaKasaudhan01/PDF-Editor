package com.pdfwordeditor.app.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pdfwordeditor.app.documentmodel.BoundingBox;
import com.pdfwordeditor.app.documentmodel.DocumentMetadata;
import com.pdfwordeditor.app.documentmodel.EditableDocument;
import com.pdfwordeditor.app.documentmodel.PageModel;
import com.pdfwordeditor.app.documentmodel.TextRunModel;
import com.pdfwordeditor.app.export.PdfBoxDocumentExporter;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PdfBoxDocumentParserTest {

  private EditableDocument sampleDocument() {
    TextRunModel run = new TextRunModel(
      UUID.randomUUID().toString(),
      "Hello World",
      "Arial",
      24,
      "normal",
      "normal",
      "#000000",
      new BoundingBox(72, 72, 200, 28),
      0
    );
    PageModel page = new PageModel(1, 612, 792, List.of(run));
    DocumentMetadata metadata = new DocumentMetadata("sample.pdf", null, 1);
    return new EditableDocument(UUID.randomUUID(), metadata, List.of(page));
  }

  @Test
  void parsesGeneratedPdfIntoEditableDocument() {
    PdfBoxDocumentExporter exporter = new PdfBoxDocumentExporter();
    byte[] pdf = exporter.export(sampleDocument());

    PdfBoxDocumentParser parser = new PdfBoxDocumentParser();
    EditableDocument parsed = parser.parse(pdf, "sample.pdf");

    assertEquals(1, parsed.pages().size());
    assertFalse(parsed.pages().get(0).textRuns().isEmpty());
    boolean containsHello = parsed.pages().get(0).textRuns().stream()
      .anyMatch(r -> r.text().contains("Hello"));
    assertTrue(containsHello, "Parsed document should contain the original text");
  }
}
