package com.pdfwordeditor.app.layoutengine;

import com.pdfwordeditor.app.documentmodel.BoundingBox;
import com.pdfwordeditor.app.documentmodel.EditableDocument;
import com.pdfwordeditor.app.documentmodel.PageModel;
import com.pdfwordeditor.app.documentmodel.TextRunModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LayoutEngineImpl implements LayoutEnginePort {

  @Override
  public EditableDocument reflow(EditableDocument document) {
    List<PageModel> reflowedPages = new ArrayList<>();

    for (PageModel page : document.pages()) {
      List<TextRunModel> sortedRuns = new ArrayList<>(page.textRuns());
      sortedRuns.sort(Comparator.comparingDouble(run -> run.boundingBox().y() * 10000 + run.boundingBox().x()));

      List<TextBlock> blocks = groupIntoBlocks(sortedRuns);
      List<Paragraph> paragraphs = buildParagraphs(blocks, page.height());

      List<TextRunModel> newRuns = reflowParagraphs(paragraphs, page.width());
      PageModel reflowedPage = new PageModel(
        page.pageNumber(),
        page.width(),
        page.height(),
        newRuns
      );
      reflowedPages.add(reflowedPage);
    }

    return new EditableDocument(document.id(), document.metadata(), reflowedPages);
  }

  private List<TextBlock> groupIntoBlocks(List<TextRunModel> runs) {
    if (runs.isEmpty()) {
      return Collections.emptyList();
    }

    List<TextBlock> blocks = new ArrayList<>();
    TextBlock current = new TextBlock();
    current.runs.add(runs.get(0));

    for (int i = 1; i < runs.size(); i++) {
      TextRunModel prev = runs.get(i - 1);
      TextRunModel curr = runs.get(i);

      double verticalGap = curr.boundingBox().y() - (prev.boundingBox().y() + prev.boundingBox().height());
      double avgHeight = (prev.boundingBox().height() + curr.boundingBox().height()) / 2.0;

      if (verticalGap > avgHeight * 0.8) {
        blocks.add(current);
        current = new TextBlock();
      }
      current.runs.add(curr);
    }
    blocks.add(current);

    return blocks;
  }

  private List<Paragraph> buildParagraphs(List<TextBlock> blocks, double pageHeight) {
    List<Paragraph> paragraphs = new ArrayList<>();
    Paragraph current = new Paragraph();

    for (TextBlock block : blocks) {
      if (current.blocks.isEmpty()) {
        current.blocks.add(block);
        continue;
      }

      TextBlock last = current.blocks.get(current.blocks.size() - 1);
      double lastBottom = last.runs.get(last.runs.size() - 1).boundingBox().y()
        + last.runs.get(last.runs.size() - 1).boundingBox().height();
      double blockTop = block.runs.get(0).boundingBox().y();
      double verticalGap = blockTop - lastBottom;
      double avgHeight = block.runs.get(0).boundingBox().height();

      if (verticalGap > avgHeight * 1.2) {
        paragraphs.add(current);
        current = new Paragraph();
      }
      current.blocks.add(block);
    }
    paragraphs.add(current);

    return paragraphs;
  }

  private List<TextRunModel> reflowParagraphs(List<Paragraph> paragraphs, double pageWidth) {
    List<TextRunModel> result = new ArrayList<>();
    double cursorY = 0;

    for (Paragraph paragraph : paragraphs) {
      double cursorX = 72;
      double lineHeight = 0;

      for (TextBlock block : paragraph.blocks) {
        for (TextRunModel run : block.runs) {
          double runWidth = run.boundingBox().width();
          double runHeight = run.boundingBox().height();

          if (cursorX + runWidth > pageWidth - 72) {
            cursorX = 72;
            cursorY += lineHeight > 0 ? lineHeight : runHeight;
            lineHeight = 0;
          }

          BoundingBox newBox = new BoundingBox(cursorX, cursorY, runWidth, runHeight);
          TextRunModel reflowed = new TextRunModel(
            run.id(),
            run.text(),
            run.fontFamily(),
            run.fontSize(),
            run.fontWeight(),
            run.fontStyle(),
            run.color(),
            newBox,
            run.rotation()
          );
          result.add(reflowed);

          cursorX += runWidth;
          if (runHeight > lineHeight) {
            lineHeight = runHeight;
          }
        }
        cursorX = 72;
        cursorY += lineHeight > 0 ? lineHeight : block.runs.get(0).boundingBox().height();
        lineHeight = 0;
      }
      cursorY += lineHeight > 0 ? lineHeight * 0.5 : 12;
    }

    return result;
  }

  private static class TextBlock {
    List<TextRunModel> runs = new ArrayList<>();
  }

  private static class Paragraph {
    List<TextBlock> blocks = new ArrayList<>();
  }
}
