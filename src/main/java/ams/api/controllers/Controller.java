package ams.api.controllers;


import ams.domain.GenericException;
import ams.services.InstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {


  @Autowired
  InstanceService instanceService;

  @GetMapping(path = "getip")
  public void getip() {
    instanceService.test();
  }
  @GetMapping(path = "createFile")
  public void createFile() throws GenericException {
    instanceService.createPrivateKey();
  }

  @GetMapping(path = "close")
  public void close() {
    instanceService.close();
  }

  @GetMapping(path = "open")
  public void open() {
    instanceService.open();
  }

}
