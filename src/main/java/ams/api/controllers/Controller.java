package ams.api.controllers;


import ams.domain.GenericException;
import ams.domain.ScalingRule;
import ams.services.InstanceService;
import com.aliyuncs.exceptions.ClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

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

  /*项目初始化
  * 1.建立专有网络
  * 2.建立交换机
  * 3.创建伸缩组
  * 4.建立安全组
  * 5.设置伸缩组规则
  * 6.启用伸缩组
  * 7.设置安全组出/入规则
  * 8.创建SSH Key并且保存在本地
  * 9.把上面的数据保存好，并且写入配置文件
  *
  * */
  @GetMapping(path = "AliyunInit")
  public void init() throws GenericException, ClientException, InterruptedException, IOException, JsonProcessingException {
    String vpcId = instanceService.createVpc(allDefaultName);
    Thread.sleep(10000);
    String vSwitch = instanceService.createVSwitch(allDefaultName, vpcId);
    Thread.sleep(5000);
    String scalingGroupId = instanceService.createScalingGroup(vSwitch);
    String securityGroupId = instanceService.createSecurityGroup(allDefaultName, vpcId);
    Thread.sleep(5000);
    String scalingConfigurationId = instanceService.createScalingConfiguration(scalingGroupId, securityGroupId);
    ScalingRule scalingAddRule = instanceService.createAddScalingRule(scalingGroupId);
    ScalingRule scalingRemoveRule = instanceService.createRemoveScalingRule(scalingGroupId);
    instanceService.enableScalingGroup(scalingGroupId,scalingConfigurationId);
    instanceService.authorizeSecurityGroup(securityGroupId);
    instanceService.AuthorizeSecurityGroupEgress(securityGroupId);
    String pairName = instanceService.createPrivateKey();
    Properties properties = new Properties();
    properties.put("vpcId", vpcId);
    properties.put("switchId", vSwitch);
    properties.put("scalingGroupId", scalingGroupId);
    properties.put("securityGroupId", securityGroupId);
    properties.put("scalingConfigurationId", scalingConfigurationId);
    properties.put("scalingAddRuleAri", scalingAddRule.getScalingRuleAri());
    properties.put("scalingRemoveRuleAri", scalingRemoveRule.getScalingRuleAri());
    properties.put("pairName",pairName);
    properties.store(new FileOutputStream(new File("Aliyun.properties")), null);
  }


}
