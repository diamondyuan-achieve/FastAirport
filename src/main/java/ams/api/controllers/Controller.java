package ams.api.controllers;


import ams.domain.GenericException;
import ams.services.InstanceService;
import com.aliyuncs.exceptions.ClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class Controller {


  String INSTALL_DOCKER = "curl -s https://get.docker.com/ | sudo sh";

  String INSTALL_SS = "docker pull mritd/shadowsocks";

  String SS_RUN = "docker run -dt --name ss -p %s:%s mritd/shadowsocks -s \"-s 0.0.0.0 -p 6443 -m aes-256-cfb -k test123 --fast-open\"";


  @Value("${DefaultName}")
  private String allDefaultName;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  InstanceService instanceService;

  @GetMapping(path = "AliyunInit")
  public void init() throws GenericException, ClientException, InterruptedException, IOException {
    instanceService.serviceInit();
  }


}
