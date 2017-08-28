package ams.services.impl;

import ams.domain.GenericException;
import ams.services.Service;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.ecs.model.v20140526.*;
import com.aliyuncs.ess.model.v20140828.*;
import com.aliyuncs.exceptions.ClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class DefaultService implements Service {


  @Value("${AttachKeyName}")
  private String attachKey;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private IAcsClient iAcsClient;


  /*创建一个实例*/
  private String createInstance() {
    try {
      DescribeScalingGroupsRequest groupsRequest = new DescribeScalingGroupsRequest();
      DescribeScalingGroupsResponse response = iAcsClient.getAcsResponse(groupsRequest);
      return response.getScalingGroups().get(0).getScalingGroupId();
    } catch (ClientException e) {
      e.printStackTrace();
    }
    return null;
  }


  private Map<String, String> getScalingRules(String scalingGroupId) {
    Map<String, String> scalingMap = new HashMap<>();
    DescribeScalingRulesRequest rulesRequest = new DescribeScalingRulesRequest();
    rulesRequest.setScalingGroupId(scalingGroupId);
    try {
      DescribeScalingRulesResponse response = iAcsClient.getAcsResponse(rulesRequest);
      response.getScalingRules().forEach(o -> {
        scalingMap.put(o.getScalingRuleName(), o.getScalingRuleAri());
      });
    } catch (ClientException e) {
      e.printStackTrace();
    }
    return scalingMap;
  }


  public void test() {
    DescribeScalingInstancesRequest scalingInstancesRequest = new DescribeScalingInstancesRequest();
    scalingInstancesRequest.setScalingGroupId(createInstance());
    try {
      DescribeScalingInstancesResponse response = iAcsClient.getAcsResponse(scalingInstancesRequest);
      String instanceId = response.getScalingInstances().get(0).getInstanceId();
      System.out.println(response.getScalingInstances().get(0).getHealthStatus());
      getInstance(instanceId);
      attachKeyPair(instanceId);
    } catch (ClientException e) {
      e.printStackTrace();
    }
  }


  private String createNewKeyPair(String pairName) throws GenericException {
    CreateKeyPairRequest request = new CreateKeyPairRequest();
    request.setKeyPairName(pairName);
    try {
      CreateKeyPairResponse response = iAcsClient.getAcsResponse(request);
      return response.getPrivateKeyBody();
    } catch (ClientException e) {
      throw new GenericException("10001", "创建key失败");
    }
  }


  private void attachKeyPair(String instanceID) {
    AttachKeyPairRequest request = new AttachKeyPairRequest();
    request.setKeyPairName(attachKey);
    request.setInstanceIds(String.format("[\"%s\"]", instanceID));
    try {
      AttachKeyPairResponse response = iAcsClient.getAcsResponse(request);
      System.out.println(response.getFailCount());
    } catch (ClientException e) {
      e.printStackTrace();
    }
  }

  private void getInstance(String instanceID) {
    DescribeInstancesRequest instancesRequest = new DescribeInstancesRequest();
    try {
      DescribeInstancesResponse response = iAcsClient.getAcsResponse(instancesRequest);
      System.out.println(response.getTotalCount());
      DescribeInstancesResponse.Instance instance = response.getInstances().get(0);
      System.out.println(instance.getPublicIpAddress());
      System.out.println(instance.getInstanceName());
    } catch (ClientException e) {
      e.printStackTrace();
    }
  }


  /*
  读取配置文件。创建一个ssh文件夹，里面存放一个privateKey
   */
  public void createPrivateKey() throws GenericException {
    String pairName = attachKey;
    File sshFolder = new File("ssh");
    if (!sshFolder.exists()) {
      if (sshFolder.mkdir()) {
        log.debug("成功创建ssh文件夹");
      } else {
        log.error("创建文件失败");
      }
    }
    String filePath = String.format("ssh/%s.pem", pairName);
    File myFile = new File(filePath);
    if (!myFile.exists()) {
      String privateKey = createNewKeyPair(pairName);
      try {
        if (myFile.createNewFile()) {
          log.debug("文件创建成功");
          FileWriter writer;
          writer = new FileWriter(filePath);
          writer.write(privateKey);
          writer.flush();
          writer.close();
        }
      } catch (IOException e) {
        throw new GenericException("20001", "创建文件失败");
      }
    }
  }


  public void open() {
    getScalingRules(createInstance());
    ExecuteScalingRuleRequest executeScalingRuleRequest = new ExecuteScalingRuleRequest();
    executeScalingRuleRequest.setScalingRuleAri(getScalingRules(createInstance()).get("开启"));
    try {
      ExecuteScalingRuleResponse response = iAcsClient.getAcsResponse(executeScalingRuleRequest);
      String scalingActivityId = response.getScalingActivityId();
    } catch (ClientException e) {
      e.printStackTrace();
    }
  }


  public void close() {
    System.out.println(createInstance());
    getScalingRules(createInstance());
    ExecuteScalingRuleRequest executeScalingRuleRequest = new ExecuteScalingRuleRequest();
    executeScalingRuleRequest.setScalingRuleAri(getScalingRules(createInstance()).get("关闭"));
    try {
      ExecuteScalingRuleResponse response = iAcsClient.getAcsResponse(executeScalingRuleRequest);
      String scalingActivityId = response.getScalingActivityId();
    } catch (ClientException e) {
      e.printStackTrace();
    }
  }


}
