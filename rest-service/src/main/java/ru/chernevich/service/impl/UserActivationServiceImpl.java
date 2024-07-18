package ru.chernevich.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import ru.chernevich.dao.AppUserDAO;
import ru.chernevich.service.UserActivationService;
import ru.chernevich.utils.Decoder;

@Log4j
@RequiredArgsConstructor
@Service
public class UserActivationServiceImpl implements UserActivationService {

    private final AppUserDAO appUserDAO;

    private final Decoder decoder;

    @Override
    public boolean activation(String cryptoUserId) {
        var userId = decoder.idOf(cryptoUserId);
        log.debug(String.format("User activation with user-id=%s", userId));
        if (userId == null) {
            return false;
        }

        var optional = appUserDAO.findById(userId);
        if (optional.isPresent()) {
            var user = optional.get();
            user.setIsActive(true);
            appUserDAO.save(user);
            return true;
        }
        return false;
    }
}