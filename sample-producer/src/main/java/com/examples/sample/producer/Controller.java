package com.examples.sample.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    @Autowired ProducerService producerService;

    @PutMapping("/buy/ice-creams")
    public void buyIcecreams(){
        producerService.buyIcecreams();
    }
    @PostMapping("/summer/season")
    public void summerSeason(){
        producerService.summerSeason();
    }
    @PostMapping("/winter/season")
    public void winterSeason(){
        producerService.winterSeason();
    }
    
}
