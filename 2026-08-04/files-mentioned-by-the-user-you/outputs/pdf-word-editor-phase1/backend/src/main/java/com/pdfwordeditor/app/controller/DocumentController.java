package com.pdfwordeditor.app.controller;

import com.pdfwordeditor.app.documentmodel.EditableDocument;
import com.pdfwordeditor.app.documentmodel.PageModel;
import com.pdfwordeditor.app.documentmodel.TextRunModel;
import com.pdfwordeditor.app.service.DocumentService;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

  @GetMapping("/{id}")
  public EditableDocument getDocument(@PathVariable UUID id) {
    return documentService.getDocument(id);
  }

  public record UploadDocumentResponse(UUID id, String fileName, String status) {}
}
