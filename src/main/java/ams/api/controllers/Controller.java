package ams.api.controllers;


import ams.domain.GenericException;
import ams.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {


  @Autowired
  Service service;

  @GetMapping(path = "getip")
  public void getip() {
    service.test();
  }
  @GetMapping(path = "createFile")
  public void createFile() throws GenericException {
    service.createPrivateKey();
  }

  @GetMapping(path = "close")
  public void close() {
    service.close();
  }

  @GetMapping(path = "open")
  public void open() {
    service.open();
  }

}
