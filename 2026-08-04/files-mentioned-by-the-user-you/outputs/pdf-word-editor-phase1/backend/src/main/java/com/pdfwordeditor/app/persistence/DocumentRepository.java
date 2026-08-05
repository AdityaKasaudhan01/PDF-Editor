package com.pdfwordeditor.app.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {

  default boolean existsById(UUID id) {
    return existsById(id.toString());
  }
}
