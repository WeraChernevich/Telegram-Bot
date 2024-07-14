package ru.chernevich.servise.impl;

import lombok.extern.log4j.Log4j;
import org.jvnet.hk2.annotations.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.chernevich.dao.AppUserDAO;
import ru.chernevich.entity.AppDocument;
import ru.chernevich.entity.AppUser;
import ru.chernevich.entity.RawData;
import ru.chernevich.entity.RawDataDAO;
import ru.chernevich.exeptions.UploadFileException;
import ru.chernevich.servise.FileService;
import ru.chernevich.servise.MainService;
import ru.chernevich.servise.ProducerService;
import ru.chernevich.servise.enums.ServiceCommand;

import static ru.chernevich.entity.enums.UserState.BASIC_STATE;
import static ru.chernevich.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static ru.chernevich.servise.enums.ServiceCommand.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final FileService fileService;


    public MainServiceImpl(RawDataDAO rawDataDAO,
                           ProducerService producerService,
                           AppUserDAO appUserDAO,
                           FileService fileService) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.fileService = fileService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getUserState();
        var text = update.getMessage().getText();
        var outPut = "";

        var serviceCommand = ServiceCommand.valueOf(text);

        if(CANCEL.equals(serviceCommand)) {
            outPut = cancelPtocess(appUser);
        } else if (BASIC_STATE.equals(userState)){
            outPut = processServiceCommand(appUser, text);
        } else if(WAIT_FOR_EMAIL_STATE.equals(userState)){

        } else {
            log.error("Unknown user state " + userState);
            outPut = "Unknown user state";

        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(outPut, chatId);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if(isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        try {
            AppDocument doc = fileService.processDoc(update.getMessage());
            var answer = "Документ успешно загружен";
            sendAnswer(answer, chatId);
        } catch (UploadFileException ex) {
            log.error(ex);
            String error = "Ошибка загрузки файла";
            sendAnswer(error, chatId);
        }
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if(isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        var answer = "Фото успешно загружено";
        sendAnswer(answer, chatId);
    }

    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        var userState = appUser.getUserState();
        if(!appUser.getIsActive()) {
            var error = "Завершите регистрацию";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            var error = "Отмените текущую команду с помощью команды/cancel";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }


    private void sendAnswer(String outPut, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(outPut);
        producerService.producerAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        if(REGISTRATION.equals(cmd)) {
            return "Временно недоступна";
        } else if (HELP.equals(cmd)) {
            return help();
        } else if (START.equals(cmd)) {
            return "Привет! Чтобы просмотреть список доступных команд введите /help";
        } else {
            return "Неизвестная команда! Чтобы просмотреть список доступных команд введите /help";
        }
    }

    private String help() {
        return "Список доступных команд:\n"
                + "/cancel - отмена выполнения текущей команды\n"
                + "/registration - зарегистрировать нового пользователя.";
    }

    private String cancelPtocess(AppUser appUser) {
        appUser.setUserState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "Команда отменена!";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        AppUser persistedAppUser = AppUserDAO.findAppUserByTelegramUserId(telegramUser.getId());
        if(persistedAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .isActive(true)
                    .userState(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return persistedAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }
}
