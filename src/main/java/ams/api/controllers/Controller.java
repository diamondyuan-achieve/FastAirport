package ams.api.controllers;


import ams.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {


  @Autowired
  Service service;

  @GetMapping(path = "getip")
  public void searchByName() {
    service.test();
  }

  @GetMapping(path = "close")
  public void searchByeName() {
    service.close();
  }

  @GetMapping(path = "open")
  public void searchByesdName() {
    service.open();
  }

}
