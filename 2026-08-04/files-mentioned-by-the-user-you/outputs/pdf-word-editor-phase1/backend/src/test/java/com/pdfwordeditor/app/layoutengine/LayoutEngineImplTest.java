package com.pdfwordeditor.app.layoutengine;

import com.pdfwordeditor.app.documentmodel.BoundingBox;
import com.pdfwordeditor.app.documentmodel.DocumentMetadata;
import com.pdfwordeditor.app.documentmodel.EditableDocument;
import com.pdfwordeditor.app.documentmodel.PageModel;
import com.pdfwordeditor.app.documentmodel.TextRunModel;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LayoutEngineImplTest {

  @Autowired
  private LayoutEnginePort layoutEngine;

  @Test
  void reflow_simpleTwoRuns_sameLine() {
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
    DocumentMetadata metadata = new DocumentMetadata("test.pdf", null, 1);
    EditableDocument doc = new EditableDocument(UUID.randomUUID(), metadata, List.of(page));

    EditableDocument result = layoutEngine.reflow(doc);

    List<TextRunModel> reflowed = result.pages().get(0).textRuns();
    System.out.println("Run1 x=" + reflowed.get(0).boundingBox().x() + " y=" + reflowed.get(0).boundingBox().y());
    System.out.println("Run2 x=" + reflowed.get(1).boundingBox().x() + " y=" + reflowed.get(1).boundingBox().y());

    assert reflowed.size() == 2;
    assert reflowed.get(0).boundingBox().y() == reflowed.get(1).boundingBox().y();
    assert reflowed.get(0).boundingBox().x() + reflowed.get(0).boundingBox().width()
      <= reflowed.get(1).boundingBox().x();
  }
}
