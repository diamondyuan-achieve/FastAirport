package ams.api.controllers;


import ams.domain.GenericException;
import ams.services.InstanceService;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {


  @Value("${DefaultName}")
  private String allDefaultName;

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

  @GetMapping(path = "create")
  public void open() throws GenericException,ClientException,InterruptedException {
    String vpcId = instanceService.createVpc(allDefaultName);
    Thread.sleep(5000);
    String securityGroupId = instanceService.createSecurityGroup(allDefaultName,vpcId);
  }

}
