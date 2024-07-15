package ru.chernevich.servise.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.chernevich.dao.AppUserDAO;
import ru.chernevich.dto.MailParams;
import ru.chernevich.entity.AppUser;
import ru.chernevich.entity.enums.UserState;
import ru.chernevich.servise.AppUserService;
import ru.chernevich.utils.CryptoTool;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import static ru.chernevich.entity.enums.UserState.BASIC_STATE;
import static ru.chernevich.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;

@Log4j
@Service
public class AppUserServiceImpl implements AppUserService {
    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;
    @Value("${service.mail.uri}")
    private String mailServiceUri;

    public AppUserServiceImpl(AppUserDAO appUserDAO, CryptoTool cryptoTool) {
        this.appUserDAO = appUserDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public String registerUser(AppUser appUser) {
        if(appUser.getIsActive()) {
            return "Вы уже зарегистрированы!";
        } else if (appUser.getEmail() != null) {
            return "Вам на почту уже было направлено письмо."
                    + "Пожалуйста, проверьте вашу почту и попробуйте ещё раз.";
        }
        appUser.setUserState(WAIT_FOR_EMAIL_STATE);
        appUserDAO.save(appUser);
         return "Введите, пожалуйста, ваш email";
    }

    @Override
    public String setEmail(AppUser appUser, String email) {
        try{
            InternetAddress emailAddress = new InternetAddress(email);
            emailAddress.validate();
        } catch (AddressException e) {
           return "Неверный email. Для отмены команды введите /cancel";
        }
        var optional = appUserDAO.findByEmail(email);
        if (optional.isEmpty()) {
            appUser.setEmail(email);
            appUser.setUserState(BASIC_STATE);
            appUser = appUserDAO.save(appUser);

            var cryptoUserId = cryptoTool.hashOf(appUser.getId());
            var response = sendRequestToMailService(cryptoUserId, email);
            if(response.getStatusCode() != HttpStatus.OK) {
                var msg = String.format("Не удалось отправить письмо на почту %s", email);
                log.error(msg);
                appUser.setEmail(null);
                appUserDAO.save(appUser);
                return msg;
            }
            return "Вам на почту отправлено письмо."
                    + "Пройдите по ссылке в письме для подтверждения регистрации.";
        } else {
            return "Этот email уже зарегистрирован. Пожалуйста, попробуйте ещё раз."
                    + "Для отмены команды введите /cancel";
        }

    }

    private ResponseEntity<String> sendRequestToMailService(String cryptoUserId, String email) {
        var restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var mailParams = MailParams.builder()
                .id(cryptoUserId)
                .mailTo(email)
                .build();

        var request = new HttpEntity<>(mailParams, headers);
        return restTemplate.exchange(mailServiceUri,
                HttpMethod.POST,
                request,
                String.class);
    }
}
