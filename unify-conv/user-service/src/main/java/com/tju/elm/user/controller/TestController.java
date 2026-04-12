package com.tju.elm.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public String addDeliveryAddress(@RequestHeader(value = "username", required = false) String username) {
        log.info("username:" + username);

        return "test";
    }
}
