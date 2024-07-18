package ru.chernevich.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chernevich.entity.AppDocument;

public interface AppDocumentDAO extends JpaRepository<AppDocument, Long> {
}
