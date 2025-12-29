package com.opencode.alumxbackend.basics.LooninS.house.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HelloController {

    @GetMapping("/hello/LooninS")
    public String hello() {
        return "This is LooninS's house";
    }
}

