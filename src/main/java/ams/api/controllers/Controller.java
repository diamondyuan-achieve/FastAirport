package ams.api.controllers;


import ams.services.Service;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class Controller {


  @Autowired
  Service service;

  @GetMapping(path = "getip")
  public void getip() {
    service.test();
  }
  @GetMapping(path = "createFile")
  public void createFile(
    @ApiParam(value = "密钥名字", required = true) @RequestParam(value = "pairName") String pairName
  ) throws IOException {
    service.createFile(pairName);
  }


  @GetMapping(path = "createNewKeyPair")
  public String createNewPair(
    @ApiParam(value = "密钥名字", required = true) @RequestParam(value = "pairName") String pairName
  ) {
    return service.createNewKeyPair(pairName);
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
