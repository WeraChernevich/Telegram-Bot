package ru.chernevich.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chernevich.entity.BinaryContent;

public interface BinaryContentDAO extends JpaRepository<BinaryContent, Long>{
}
