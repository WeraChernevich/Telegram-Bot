package ru.chernevich.servise;

import ru.chernevich.entity.AppUser;

public interface AppUserService {
    String registerUser(AppUser appUser);
    String setEmail(AppUser appUser, String email);
}
