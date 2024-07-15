package ru.chernevich.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chernevich.entity.AppPhoto;

public interface AppPhotoDAO extends JpaRepository<AppPhoto, Long>{

}
