package com.pdfwordeditor.app.controller;

import com.pdfwordeditor.app.documentmodel.EditableDocument;
import com.pdfwordeditor.app.documentmodel.PageModel;
import com.pdfwordeditor.app.documentmodel.TextRunModel;
import com.pdfwordeditor.app.service.DocumentService;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

  private final DocumentService documentService;

  public DocumentController(DocumentService documentService) {
    this.documentService = documentService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UploadDocumentResponse upload(@RequestParam("file") @NotNull MultipartFile file) {
    return documentService.acceptUpload(file);
  }

  @PutMapping("/{id}")
  public void saveDocument(@PathVariable UUID id, @RequestBody EditableDocument document) {
    documentService.saveDocument(id, document);
  }

  @GetMapping("/{id}/export")
  public ResponseEntity<byte[]> exportDocument(@PathVariable UUID id) {
    byte[] pdf = documentService.exportDocument(id);
    String safeName = "document-" + id + ".pdf";
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeName + "\"")
      .contentType(MediaType.APPLICATION_PDF)
      .body(pdf);
  }

  public record UploadDocumentResponse(UUID id, String fileName, String status) {}
}
