package ru.chernevich.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chernevich.entity.AppUser;

public interface AppUserDAO extends JpaRepository<AppUser, Long>{
    public static AppUser findAppUserByTelegramUserId(Long id) {
        return null;
    }
}
