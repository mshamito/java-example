package ru.cryptopro.support.spring.example.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RedirectController {
    @RequestMapping(path = "/")
    public ResponseEntity<Void> redirectToSwagger() {
        return ResponseEntity
                .status(HttpStatus.PERMANENT_REDIRECT)
                .header("Location", "/swagger-ui/index.html")
                .build();
    }
}
