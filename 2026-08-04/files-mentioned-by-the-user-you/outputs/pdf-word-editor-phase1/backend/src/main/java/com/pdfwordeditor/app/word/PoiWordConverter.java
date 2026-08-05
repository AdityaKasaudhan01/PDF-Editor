package com.pdfwordeditor.app.word;

import com.pdfwordeditor.app.documentmodel.BoundingBox;
import com.pdfwordeditor.app.documentmodel.DocumentMetadata;
import com.pdfwordeditor.app.documentmodel.EditableDocument;
import com.pdfwordeditor.app.documentmodel.ImageElement;
import com.pdfwordeditor.app.documentmodel.PageModel;
import com.pdfwordeditor.app.documentmodel.TextRunModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

@Component
public class PoiWordConverter implements WordConverterPort {

  @Override
  public byte[] exportDocx(EditableDocument document) {
    try (XWPFDocument doc = new XWPFDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      boolean first = true;
      for (PageModel page : document.pages()) {
        if (!first) {
          XWPFParagraph breakParagraph = doc.createParagraph();
          breakParagraph.setPageBreak(true);
        }
        first = false;

        for (TextRunModel run : page.textRuns()) {
          XWPFParagraph paragraph = doc.createParagraph();
          XWPFRun wordRun = paragraph.createRun();
          wordRun.setText(run.text());
          if (run.fontFamily() != null) {
            wordRun.setFontFamily(run.fontFamily());
          }
          wordRun.setFontSize((int) Math.round(run.fontSize()));
          wordRun.setBold("bold".equalsIgnoreCase(run.fontWeight()));
          wordRun.setItalic("italic".equalsIgnoreCase(run.fontStyle()));
          wordRun.setColor(stripHash(run.color()));
        }
      }
      doc.write(out);
      return out.toByteArray();
    } catch (IOException e) {
      throw new WordConversionException("Failed to export DOCX document", e);
    }
  }

  @Override
  public EditableDocument importDocx(InputStream docxStream) {
    try (XWPFDocument doc = new XWPFDocument(docxStream)) {
      List<PageModel> pages = new ArrayList<>();
      double cursorY = 72;
      List<TextRunModel> runs = new ArrayList<>();

      for (XWPFParagraph paragraph : doc.getParagraphs()) {
        for (XWPFRun run : paragraph.getRuns()) {
          String text = run.getText(0);
          if (text == null || text.isEmpty()) {
            continue;
          }
          double fontSize = run.getFontSize() > 0 ? run.getFontSize() : 12;
          String family = run.getFontFamily() != null ? run.getFontFamily() : "Arial";
          double width = Math.max(text.length() * fontSize * 0.5, 20);
          runs.add(new TextRunModel(
            UUID.randomUUID().toString(),
            text,
            family,
            fontSize,
            run.isBold() ? "bold" : "normal",
            run.isItalic() ? "italic" : "normal",
            "#" + safeColor(run.getColor()),
            new BoundingBox(72, cursorY, width, fontSize * 1.2),
            0
          ));
          cursorY += fontSize * 1.4;
        }
        cursorY += 12;
      }

      pages.add(new PageModel(1, 612, 792, runs, List.of()));
      DocumentMetadata metadata = new DocumentMetadata("document.docx", null, pages.size());
      return new EditableDocument(UUID.randomUUID(), metadata, pages);
    } catch (IOException e) {
      throw new WordConversionException("Failed to import DOCX document", e);
    }
  }

  private String stripHash(String color) {
    if (color == null) {
      return "000000";
    }
    return color.replace("#", "");
  }

  private String safeColor(String color) {
    if (color == null) {
      return "000000";
    }
    String value = color.replace("#", "");
    if (value.length() == 3) {
      value = "" + value.charAt(0) + value.charAt(0)
        + value.charAt(1) + value.charAt(1)
        + value.charAt(2) + value.charAt(2);
    }
    if (value.length() != 6) {
      return "000000";
    }
    try {
      Integer.parseInt(value, 16);
      return value;
    } catch (NumberFormatException e) {
      return "000000";
    }
  }
}
