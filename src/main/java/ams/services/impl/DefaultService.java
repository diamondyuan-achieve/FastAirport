package ams.services.impl;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.ecs.model.v20140526.AttachKeyPairRequest;
import com.aliyuncs.ecs.model.v20140526.AttachKeyPairResponse;
import com.aliyuncs.ecs.model.v20140526.DescribeInstancesRequest;
import com.aliyuncs.ecs.model.v20140526.DescribeInstancesResponse;
import com.aliyuncs.ess.model.v20140828.*;
import com.aliyuncs.exceptions.ClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ams.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DefaultService implements Service {


  @Value("${AttachKey}")
  private String attachKey;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private IAcsClient iAcsClient;


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
      System.out.println(instanceId);
      System.out.println(response.getScalingInstances().get(0).getHealthStatus());
      getInstance(instanceId);
      attachKeyPair(instanceId);
    } catch (ClientException e) {
      e.printStackTrace();
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

  public void open() {
    System.out.println(createInstance());
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
