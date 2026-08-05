package com.pdfwordeditor.app.export;

import com.pdfwordeditor.app.documentmodel.EditableDocument;
import com.pdfwordeditor.app.documentmodel.ImageElement;
import com.pdfwordeditor.app.documentmodel.PageModel;
import com.pdfwordeditor.app.documentmodel.TextRunModel;
import com.pdfwordeditor.app.imagemanager.ImageManagerPort;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Component;

@Component
public class PdfBoxDocumentExporter implements DocumentExportPort {

  private final ImageManagerPort imageManager;

  public PdfBoxDocumentExporter(ImageManagerPort imageManager) {
    this.imageManager = imageManager;
  }

  @Override
  public byte[] export(EditableDocument document) {
    try (PDDocument pdf = new PDDocument()) {
      for (PageModel page : document.pages()) {
        PDPage pdfPage = new PDPage(new PDRectangle((float) page.width(), (float) page.height()));
        pdf.addPage(pdfPage);

        try (PDPageContentStream content = new PDPageContentStream(pdf, pdfPage)) {
          for (TextRunModel run : page.textRuns()) {
            if (run.text() == null || run.text().isBlank()) {
              continue;
            }
            drawRun(content, page, run);
          }

          List<ImageElement> images = page.images() != null ? page.images() : List.of();
          for (ImageElement image : images) {
            drawImage(content, pdf, page, image);
          }
        }
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      pdf.save(out);
      return out.toByteArray();
    } catch (IOException ex) {
      throw new DocumentExportException("Failed to generate PDF document", ex);
    }
  }

  private void drawRun(PDPageContentStream content, PageModel page, TextRunModel run) throws IOException {
    PDType1Font font = resolveFont(run.fontFamily(), run.fontWeight(), run.fontStyle());
    float size = (float) run.fontSize();

    content.setFont(font, size);
    content.setNonStrokingColor(parseColor(run.color()));

    float ascent = fontAscent(font, size);
    float baselineY = (float) (page.height() - (run.boundingBox().y() + ascent));
    float x = (float) run.boundingBox().x();

    content.beginText();
    if (run.rotation() != 0) {
      content.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(run.rotation()), x, baselineY));
    } else {
      content.newLineAtOffset(x, baselineY);
    }
    content.showText(run.text());
    content.endText();
  }

  private void drawImage(PDPageContentStream content, PDDocument pdf, PageModel page, ImageElement image)
      throws IOException {
    if (image.storageKey() == null) {
      return;
    }
    try {
      byte[] data = imageManager.loadImage(image.storageKey());
      PDImageXObject xobject = PDImageXObject.createFromByteArray(pdf, data, image.id());
      double x = image.boundingBox().x();
      double y = page.height() - (image.boundingBox().y() + image.boundingBox().height());
      content.drawImage(
        xobject,
        (float) x,
        (float) y,
        (float) image.boundingBox().width(),
        (float) image.boundingBox().height()
      );
    } catch (Exception e) {
      // skip image that cannot be loaded
    }
  }

  private PDType1Font resolveFont(String family, String weight, String style) {
    boolean bold = "bold".equalsIgnoreCase(weight);
    boolean italic = "italic".equalsIgnoreCase(style);

    String key = family == null ? "" : family.toLowerCase();
    BaseFamily base;
    if (key.contains("times")) {
      base = BaseFamily.TIMES;
    } else if (key.contains("courier")) {
      base = BaseFamily.COURIER;
    } else {
      base = BaseFamily.HELVETICA;
    }

    Standard14Fonts.FontName name = switch (base) {
      case TIMES -> italic
        ? (bold ? Standard14Fonts.FontName.TIMES_BOLD_ITALIC : Standard14Fonts.FontName.TIMES_ITALIC)
        : (bold ? Standard14Fonts.FontName.TIMES_BOLD : Standard14Fonts.FontName.TIMES_ROMAN);
      case COURIER -> italic
        ? (bold ? Standard14Fonts.FontName.COURIER_BOLD_OBLIQUE : Standard14Fonts.FontName.COURIER_OBLIQUE)
        : (bold ? Standard14Fonts.FontName.COURIER_BOLD : Standard14Fonts.FontName.COURIER);
      case HELVETICA -> italic
        ? (bold ? Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE : Standard14Fonts.FontName.HELVETICA_OBLIQUE)
        : (bold ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA);
    };

    return new PDType1Font(name);
  }

  private float fontAscent(PDType1Font font, float size) {
    PDFontDescriptor descriptor = font.getFontDescriptor();
    if (descriptor != null) {
      float ascent = descriptor.getAscent();
      if (ascent > 0) {
        return (ascent / 1000f) * size;
      }
    }
    return size * 0.8f;
  }

  private Color parseColor(String hex) {
    if (hex == null || hex.isBlank()) {
      return Color.BLACK;
    }
    String value = hex.trim().replace("#", "");
    if (value.length() == 3) {
      value = "" + value.charAt(0) + value.charAt(0)
        + value.charAt(1) + value.charAt(1)
        + value.charAt(2) + value.charAt(2);
    }
    if (value.length() != 6) {
      return Color.BLACK;
    }
    try {
      int r = Integer.parseInt(value.substring(0, 2), 16);
      int g = Integer.parseInt(value.substring(2, 4), 16);
      int b = Integer.parseInt(value.substring(4, 6), 16);
      return new Color(r, g, b);
    } catch (NumberFormatException ex) {
      return Color.BLACK;
    }
  }

  private enum BaseFamily {
    HELVETICA,
    TIMES,
    COURIER
  }
}
