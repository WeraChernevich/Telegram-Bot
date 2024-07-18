package ru.chernevich.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.chernevich.service.UserActivationService;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class ActivationController {

    private final UserActivationService userActivationService;

    @RequestMapping(method = RequestMethod.GET, value = "/user/activation")
    public ResponseEntity<?> activation(@RequestParam("id") String id) {
        var res = userActivationService.activation(id);
        if (res) {
            return ResponseEntity.ok().body("Регистрация успешно завершена!");
        }
        return ResponseEntity.badRequest().body("Неверная ссылка!");
    }
}
