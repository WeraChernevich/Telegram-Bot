package ru.chernevich.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chernevich.entity.RawData;

public interface RawDataDAO extends JpaRepository<RawData, Long> {
}
