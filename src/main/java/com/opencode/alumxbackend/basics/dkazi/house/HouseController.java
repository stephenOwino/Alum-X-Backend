package com.opencode.alumxbackend.basics.dkazi.house;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor; // Αν το project έχει Lombok

@RestController
@RequestMapping("/house")
public class HouseController {

    private final HouseService houseService;

    // Constructor Injection (Ο σωστός τρόπος για να πάρει τον Controller το Service)
    public HouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    @GetMapping("/hello/{your_name}")
    public String sayHello(@PathVariable String your_name) {
        // Καλούμε τη μέθοδο από το Service αντί να γράψουμε το κείμενο εδώ
        return houseService.getHelloMessage(your_name);
    }
}