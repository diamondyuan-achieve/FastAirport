package ams.api.controllers;


import ams.domain.Config;
import ams.domain.GenericException;
import ams.services.DiamondUtils;
import ams.services.InstanceService;
import com.aliyuncs.exceptions.ClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.Properties;

@RestController
public class Controller {


  @Value("${DefaultName}")
  private String allDefaultName;

  @Autowired
  ObjectMapper objectMapper;

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


  /*项目初始化
  * 1.建立专有网络
  * 2.建立交换机
  * 3.建立安全组
  * 4.设置安全组规则
  *
  *
  * */
  @GetMapping(path = "AliyunInit")
  public void init() throws GenericException, ClientException, InterruptedException,IOException, JsonProcessingException {
    Properties properties = new Properties();
    Config config = new Config();
    String vpcId = instanceService.createVpc(allDefaultName);
    properties.put("vpcId", vpcId);
    properties.store(new FileOutputStream(new File("ssh/1.properties")),"We");
    config.setVpcId(vpcId);
    Thread.sleep(5000);
    String securityGroupId = instanceService.createSecurityGroup(allDefaultName, vpcId);
    config.setSecurityGroupId(securityGroupId);
    DiamondUtils.saveFile(objectMapper.writeValueAsString(config), "config");
  }


}
