package com.opencode.alumxbackend.basics.dkazi.house;

import org.springframework.stereotype.Service;

@Service
public class HouseService {

    public String getHelloMessage(String your_name) {
        return "This is " + your_name + "'s house, served from HouseService!";
    }
}