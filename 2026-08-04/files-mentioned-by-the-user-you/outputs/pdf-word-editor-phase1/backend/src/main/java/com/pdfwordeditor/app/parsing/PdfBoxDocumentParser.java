package com.pdfwordeditor.app.parsing;

import com.pdfwordeditor.app.documentmodel.BoundingBox;
import com.pdfwordeditor.app.documentmodel.DocumentMetadata;
import com.pdfwordeditor.app.documentmodel.EditableDocument;
import com.pdfwordeditor.app.documentmodel.PageModel;
import com.pdfwordeditor.app.documentmodel.TextRunModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;

@Component
public class PdfBoxDocumentParser implements DocumentParserPort {

  @Override
  public EditableDocument parse(byte[] content, String fileName) {
    try (PDDocument pdf = Loader.loadPDF(content)) {
      List<PageModel> pages = new ArrayList<>();

      ParserStripper stripper = new ParserStripper();
      stripper.setSortByPosition(true);

      for (int i = 0; i < pdf.getNumberOfPages(); i++) {
        PDPage page = pdf.getPage(i);
        PDRectangle media = page.getMediaBox();
        double pageWidth = media.getWidth();
        double pageHeight = media.getHeight();

        stripper.beginPage(pageHeight);
        stripper.setStartPage(i + 1);
        stripper.setEndPage(i + 1);
        stripper.getText(pdf);

        pages.add(new PageModel(i + 1, pageWidth, pageHeight, stripper.collectRuns()));
      }

      DocumentMetadata metadata = new DocumentMetadata(fileName, null, pages.size());
      return new EditableDocument(UUID.randomUUID(), metadata, pages);
    } catch (IOException ex) {
      throw new DocumentParseException("Failed to parse PDF document", ex);
    }
  }

  private static final class ParserStripper extends PDFTextStripper {
    private List<TextRunModel> currentRuns = new ArrayList<>();
    private double currentPageHeight;

    ParserStripper() throws IOException {
      super();
    }

    void beginPage(double pageHeight) {
      this.currentPageHeight = pageHeight;
      this.currentRuns = new ArrayList<>();
    }

    List<TextRunModel> collectRuns() {
      return currentRuns;
    }

    @Override
    protected void writeString(String text, List<TextPosition> positions) throws IOException {
      if (positions == null || positions.isEmpty()) {
        return;
      }

      TextPosition first = positions.get(0);
      TextPosition last = positions.get(positions.size() - 1);

      double fontSize = first.getFontSize();
      if (fontSize <= 0) {
        fontSize = 12;
      }

      double x = first.getXDirAdj();
      double baselineY = first.getYDirAdj();
      double xEnd = last.getXDirAdj() + last.getWidthDirAdj();
      double width = Math.max(xEnd - x, 1);
      double height = fontSize * 1.2;

      double topY = currentPageHeight - (baselineY + fontSize * 0.8);

      String fontName = first.getFont() != null ? first.getFont().getName() : "";
      String family = mapFontFamily(fontName);
      String weight = mapWeight(fontName);
      String style = mapStyle(fontName);

      currentRuns.add(new TextRunModel(
        UUID.randomUUID().toString(),
        text,
        family,
        fontSize,
        weight,
        style,
        "#000000",
        new BoundingBox(x, topY, width, height),
        0
      ));
    }

    private String mapFontFamily(String pdfFontName) {
      if (pdfFontName == null) {
        return "Arial";
      }
      String name = pdfFontName.toLowerCase();
      if (name.contains("times")) {
        return "Times New Roman";
      }
      if (name.contains("courier")) {
        return "Courier New";
      }
      return "Arial";
    }

    private String mapWeight(String pdfFontName) {
      if (pdfFontName == null) {
        return "normal";
      }
      return pdfFontName.toLowerCase().contains("bold") ? "bold" : "normal";
    }

    private String mapStyle(String pdfFontName) {
      if (pdfFontName == null) {
        return "normal";
      }
      String name = pdfFontName.toLowerCase();
      return (name.contains("italic") || name.contains("oblique")) ? "italic" : "normal";
    }
  }
}
