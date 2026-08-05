package com.pdfwordeditor.app.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pdfwordeditor.app.documentmodel.BoundingBox;
import com.pdfwordeditor.app.documentmodel.DocumentMetadata;
import com.pdfwordeditor.app.documentmodel.EditableDocument;
import com.pdfwordeditor.app.documentmodel.PageModel;
import com.pdfwordeditor.app.documentmodel.TextRunModel;
import java.util.List;
import java.util.UUID;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

class PdfBoxDocumentExporterTest {

  private EditableDocument sampleDocument() {
    TextRunModel run1 = new TextRunModel(
      UUID.randomUUID().toString(),
      "Hello",
      "Arial",
      24,
      "normal",
      "normal",
      "#000000",
      new BoundingBox(72, 72, 50, 28),
      0
    );

    TextRunModel run2 = new TextRunModel(
      UUID.randomUUID().toString(),
      "Bold World",
      "Times New Roman",
      18,
      "bold",
      "italic",
      "#FF0000",
      new BoundingBox(72, 120, 120, 24),
      0
    );

    PageModel page = new PageModel(1, 612, 792, List.of(run1, run2), List.of());
    DocumentMetadata metadata = new DocumentMetadata("sample.pdf", null, 1);
    return new EditableDocument(UUID.randomUUID(), metadata, List.of(page));
  }

  @Test
  void exportsDocumentToValidPdf() {
    PdfBoxDocumentExporter exporter = new PdfBoxDocumentExporter();

    byte[] pdf = exporter.export(sampleDocument());

    assertNotNull(pdf);
    assertTrue(pdf.length > 0);
    assertEquals("%PDF", new String(pdf, 0, 4));
  }

  @Test
  void exportedPdfHasExpectedPageCount() throws Exception {
    PdfBoxDocumentExporter exporter = new PdfBoxDocumentExporter();

    byte[] pdf = exporter.export(sampleDocument());

    try (PDDocument document = Loader.loadPDF(pdf)) {
      assertEquals(1, document.getNumberOfPages());
    }
  }
}
