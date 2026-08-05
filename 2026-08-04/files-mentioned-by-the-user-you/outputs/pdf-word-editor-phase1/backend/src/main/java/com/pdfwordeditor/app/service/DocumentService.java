package com.pdfwordeditor.app.service;

import com.pdfwordeditor.app.controller.DocumentController.UploadDocumentResponse;
import com.pdfwordeditor.app.documentmodel.BoundingBox;
import com.pdfwordeditor.app.documentmodel.DocumentMetadata;
import com.pdfwordeditor.app.documentmodel.EditableDocument;
import com.pdfwordeditor.app.documentmodel.ImageElement;
import com.pdfwordeditor.app.documentmodel.PageModel;
import com.pdfwordeditor.app.documentmodel.TextRunModel;
import com.pdfwordeditor.app.export.DocumentExportPort;
import com.pdfwordeditor.app.layoutengine.LayoutEnginePort;
import com.pdfwordeditor.app.parsing.DocumentParserPort;
import com.pdfwordeditor.app.word.WordConverterPort;
import com.pdfwordeditor.app.persistence.DocumentEntity;
import com.pdfwordeditor.app.persistence.DocumentRepository;
import com.pdfwordeditor.app.persistence.ImageEntity;
import com.pdfwordeditor.app.persistence.PageEntity;
import com.pdfwordeditor.app.persistence.TextRunEntity;
import java.time.Instant;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

  private final DocumentRepository documentRepository;
  private final LayoutEnginePort layoutEngine;
  private final DocumentExportPort documentExporter;
  private final DocumentParserPort documentParser;
  private final WordConverterPort wordConverter;

  public DocumentService(
      DocumentRepository documentRepository,
      LayoutEnginePort layoutEngine,
      DocumentExportPort documentExporter,
      DocumentParserPort documentParser,
      WordConverterPort wordConverter) {
    this.documentRepository = documentRepository;
    this.layoutEngine = layoutEngine;
    this.documentExporter = documentExporter;
    this.documentParser = documentParser;
    this.wordConverter = wordConverter;
  }

  public UploadDocumentResponse acceptUpload(MultipartFile file) {
    UUID id = UUID.randomUUID();
    EditableDocument doc;
    try {
      doc = documentParser.parse(file.getBytes(), file.getOriginalFilename());
    } catch (Exception ex) {
      doc = layoutEngine.reflow(createSampleDocument(id, file.getOriginalFilename()));
    }
    persist(id.toString(), doc);
    return new UploadDocumentResponse(id, file.getOriginalFilename(), "READY");
  }

  public EditableDocument getDocument(UUID id) {
    DocumentEntity entity = documentRepository.findById(id.toString()).orElse(null);
    if (entity == null) {
      EditableDocument sample = layoutEngine.reflow(createSampleDocument(id, "sample.pdf"));
      persist(id.toString(), sample);
      return sample;
    }
    return toDomain(entity);
  }

  public void saveDocument(UUID id, EditableDocument document) {
    persist(id.toString(), document);
  }

  public byte[] exportDocument(UUID id) {
    EditableDocument doc = getDocument(id);
    return documentExporter.export(doc);
  }

  public byte[] exportDocx(UUID id) {
    return wordConverter.exportDocx(getDocument(id));
  }

  public UploadDocumentResponse acceptDocxUpload(MultipartFile file) {
    UUID id = UUID.randomUUID();
    try {
      EditableDocument doc = wordConverter.importDocx(file.getInputStream());
      persist(id.toString(), doc);
    } catch (IOException e) {
      throw new com.pdfwordeditor.app.parsing.DocumentParseException("Failed to import DOCX document", e);
    }
    return new UploadDocumentResponse(id, file.getOriginalFilename(), "READY");
  }

  private void persist(String id, EditableDocument document) {
    DocumentEntity entity = documentRepository.findById(id).orElse(new DocumentEntity());
    entity.setId(id);
    entity.setFileName(document.metadata() != null && document.metadata().title() != null
      ? document.metadata().title() : "document.pdf");
    entity.setTitle(entity.getFileName());
    entity.setAuthor(document.metadata() != null ? document.metadata().author() : null);
    entity.setPageCount(document.pages().size());
    entity.setCreatedAt(entity.getCreatedAt() == null ? Instant.now() : entity.getCreatedAt());
    entity.setUpdatedAt(Instant.now());

    List<PageEntity> pageEntities = new ArrayList<>();
    int index = 0;
    for (PageModel page : document.pages()) {
      index++;
      PageEntity pageEntity = new PageEntity();
      pageEntity.setPageNumber(page.pageNumber());
      pageEntity.setWidth(page.width());
      pageEntity.setHeight(page.height());
      pageEntity.setDocument(entity);

      List<TextRunEntity> runEntities = new ArrayList<>();
      for (TextRunModel run : page.textRuns()) {
        TextRunEntity runEntity = new TextRunEntity();
        runEntity.setId(run.id());
        runEntity.setText(run.text());
        runEntity.setFontFamily(run.fontFamily());
        runEntity.setFontSize(run.fontSize());
        runEntity.setFontWeight(run.fontWeight());
        runEntity.setFontStyle(run.fontStyle());
        runEntity.setColor(run.color());
        runEntity.setX(run.boundingBox().x());
        runEntity.setY(run.boundingBox().y());
        runEntity.setWidth(run.boundingBox().width());
        runEntity.setHeight(run.boundingBox().height());
        runEntity.setRotation(run.rotation());
        runEntity.setPage(pageEntity);
        runEntities.add(runEntity);
      }
      pageEntity.setTextRuns(runEntities);

      List<ImageEntity> imageEntities = new ArrayList<>();
      List<ImageElement> images = page.images() != null ? page.images() : List.of();
      for (ImageElement image : images) {
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setId(image.id());
        imageEntity.setStorageKey(image.storageKey());
        imageEntity.setContentType(image.contentType());
        imageEntity.setX(image.boundingBox().x());
        imageEntity.setY(image.boundingBox().y());
        imageEntity.setWidth(image.boundingBox().width());
        imageEntity.setHeight(image.boundingBox().height());
        imageEntity.setRotation(image.rotation());
        imageEntity.setPage(pageEntity);
        imageEntities.add(imageEntity);
      }
      pageEntity.setImages(imageEntities);

      pageEntities.add(pageEntity);
    }
    entity.setPages(pageEntities);

    documentRepository.save(entity);
  }

  private EditableDocument toDomain(DocumentEntity entity) {
    List<PageModel> pages = new ArrayList<>();
    for (PageEntity pageEntity : entity.getPages()) {
      List<TextRunModel> runs = new ArrayList<>();
      for (TextRunEntity run : pageEntity.getTextRuns()) {
        runs.add(new TextRunModel(
          run.getId(),
          run.getText(),
          run.getFontFamily(),
          run.getFontSize(),
          run.getFontWeight(),
          run.getFontStyle(),
          run.getColor(),
          new BoundingBox(run.getX(), run.getY(), run.getWidth(), run.getHeight()),
          run.getRotation()
        ));
      }
      List<ImageElement> images = new ArrayList<>();
      for (ImageEntity image : pageEntity.getImages()) {
        images.add(new ImageElement(
          image.getId(),
          image.getStorageKey(),
          image.getContentType(),
          new BoundingBox(image.getX(), image.getY(), image.getWidth(), image.getHeight()),
          image.getRotation()
        ));
      }
      pages.add(new PageModel(
        pageEntity.getPageNumber(),
        pageEntity.getWidth(),
        pageEntity.getHeight(),
        runs,
        images
      ));
    }
    DocumentMetadata metadata = new DocumentMetadata(
      entity.getFileName(),
      entity.getAuthor(),
      pages.size()
    );
    return new EditableDocument(UUID.fromString(entity.getId()), metadata, pages);
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

    PageModel page = new PageModel(1, 612, 792, List.of(run1, run2), List.of());
    DocumentMetadata metadata = new DocumentMetadata(fileName, null, 1);
    return new EditableDocument(id, metadata, List.of(page));
  }
}
