package ams.services.impl;

import ams.domain.Instance;
import ams.domain.ScalingRule;
import ams.services.DiamondUtils;
import ams.services.InstanceService;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.ecs.model.v20140526.*;
import com.aliyuncs.ess.model.v20140828.*;
import com.aliyuncs.exceptions.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

@Component
@Slf4j
public class AliyunInstanceService implements InstanceService {


  @Value("${AttachKeyName}")
  private String attachKey;

  @Value("${ScalingGroupMaxSize}")
  private Integer scalingGroupMaxSize;

  @Value("${ScalingGroupMinSize}")
  private Integer scalingGroupMinSize;

  @Value("${DefaultName}")
  private String allDefaultName;


  @Autowired
  private IAcsClient iAcsClient;


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


  private Instance getInstance(String instanceID) throws ClientException {
    DescribeInstancesRequest instancesRequest = new DescribeInstancesRequest();
    instancesRequest.setInstanceIds(String.format("[\"%s\"]", instanceID));
    DescribeInstancesResponse response = iAcsClient.getAcsResponse(instancesRequest);
    DescribeInstancesResponse.Instance instance = response.getInstances().get(0);
    return new Instance() {{
      setId(instance.getInstanceId());
      setRegionId(instance.getRegionId());
      setIp(instance.getPublicIpAddress().get(0));
      setStatus(instance.getStatus());
    }};
  }


  private void executRule(String ruleAri) throws ClientException {
    ExecuteScalingRuleRequest executeScalingRuleRequest = new ExecuteScalingRuleRequest();
    executeScalingRuleRequest.setScalingRuleAri(ruleAri);
    iAcsClient.getAcsResponse(executeScalingRuleRequest);
  }


  public String getInstanceId(String scalingGroupId, String activeScalingConfigurationId) throws ClientException {
    DescribeScalingInstancesRequest scalingInstancesRequest = new DescribeScalingInstancesRequest();
    scalingInstancesRequest.setScalingGroupId(scalingGroupId);
    scalingInstancesRequest.setScalingConfigurationId(activeScalingConfigurationId);
    DescribeScalingInstancesResponse response = iAcsClient.getAcsResponse(scalingInstancesRequest);
    if (response.getScalingInstances().size() > 0) {
      return response.getScalingInstances().get(0).getInstanceId();
    }
    return null;
  }

  /*
 读取配置文件。创建一个ssh文件夹，里面存放一个privateKey
  */
  public String createPrivateKey() throws ClientException, IOException {
    String pairName = attachKey;
    File sshFolder = new File("ssh");
    if (!sshFolder.exists()) {
      if (sshFolder.mkdir()) {
        log.debug("成功创建ssh文件夹");
      } else {
        log.error("创建文件失败");
      }
    }
    String privateKey = createNewKeyPair(pairName);
    DiamondUtils.saveFile(privateKey, pairName);
    return pairName;
  }


  private String createNewKeyPair(String pairName) throws ClientException {
    CreateKeyPairRequest request = new CreateKeyPairRequest();
    request.setKeyPairName(pairName);
    CreateKeyPairResponse response = iAcsClient.getAcsResponse(request);
    return response.getPrivateKeyBody();
  }


  /*根据伸缩组名称创建伸缩组 返回伸缩组id*/
  public String createScalingGroup(String vSwitchID) throws ClientException {
    CreateScalingGroupRequest createScalingGroupRequest = new CreateScalingGroupRequest();
    createScalingGroupRequest.setMaxSize(scalingGroupMaxSize);
    createScalingGroupRequest.setMinSize(scalingGroupMinSize);
    createScalingGroupRequest.setVSwitchId(vSwitchID);
    CreateScalingGroupResponse response = iAcsClient.getAcsResponse(createScalingGroupRequest);
    return response.getScalingGroupId();
  }


  /*根据安全组名称创建安全组*/
  public String createSecurityGroup(String securityGroupName, String vpvId) throws ClientException {
    CreateSecurityGroupRequest createSecurityGroup = new CreateSecurityGroupRequest();
    createSecurityGroup.setSecurityGroupName(securityGroupName);
    createSecurityGroup.setVpcId(vpvId);
    CreateSecurityGroupResponse response = iAcsClient.getAcsResponse(createSecurityGroup);
    return response.getSecurityGroupId();
  }

  public String createScalingConfiguration(String scalingGroupId, String securityGroupId) throws ClientException {
    CreateScalingConfigurationRequest createScalingConfigurationRequest = new CreateScalingConfigurationRequest();
    createScalingConfigurationRequest.setInstanceType("ecs.xn4.small");
    createScalingConfigurationRequest.setImageId("ubuntu_16_0402_64_40G_alibase_20170711.vhd");
    createScalingConfigurationRequest.setInternetChargeType("PayByTraffic");
    createScalingConfigurationRequest.setSystemDiskCategory("cloud_efficiency");
    createScalingConfigurationRequest.setInternetMaxBandwidthOut(100);
    createScalingConfigurationRequest.setIoOptimized("optimized");
    createScalingConfigurationRequest.setSecurityGroupId(securityGroupId);
    createScalingConfigurationRequest.setScalingGroupId(scalingGroupId);
    CreateScalingConfigurationResponse response = iAcsClient.getAcsResponse(createScalingConfigurationRequest);
    return response.getScalingConfigurationId();
  }

  public void authorizeSecurityGroup(String securityGroupId) throws ClientException {
    AuthorizeSecurityGroupRequest authorizeSecurityGroupRequest = new AuthorizeSecurityGroupRequest();
    authorizeSecurityGroupRequest.setSecurityGroupId(securityGroupId);
    authorizeSecurityGroupRequest.setSourceCidrIp("0.0.0.0/0");
    authorizeSecurityGroupRequest.setIpProtocol("all");
    authorizeSecurityGroupRequest.setPortRange("-1/-1");
    iAcsClient.getAcsResponse(authorizeSecurityGroupRequest);
  }

  public void AuthorizeSecurityGroupEgress(String securityGroupId) throws ClientException {
    AuthorizeSecurityGroupEgressRequest authorizeSecurityGroupEgressRequest = new AuthorizeSecurityGroupEgressRequest();
    authorizeSecurityGroupEgressRequest.setSecurityGroupId(securityGroupId);
    authorizeSecurityGroupEgressRequest.setDestCidrIp("0.0.0.0/0");
    authorizeSecurityGroupEgressRequest.setIpProtocol("all");
    authorizeSecurityGroupEgressRequest.setPortRange("-1/-1");
    iAcsClient.getAcsResponse(authorizeSecurityGroupEgressRequest);
  }


  public ScalingRule createAddScalingRule(String ScalingGroupId) throws ClientException {
    return createTotalCapacityRule(ScalingGroupId, 1);
  }

  private ScalingRule createTotalCapacityRule(String ScalingGroupId, int count) throws ClientException {
    CreateScalingRuleRequest createScalingRuleRequest = new CreateScalingRuleRequest();
    createScalingRuleRequest.setScalingGroupId(ScalingGroupId);
    createScalingRuleRequest.setAdjustmentType("TotalCapacity");
    createScalingRuleRequest.setAdjustmentValue(count);
    CreateScalingRuleResponse response = iAcsClient.getAcsResponse(createScalingRuleRequest);
    return new ScalingRule() {{
      setScalingRuleAri(response.getScalingRuleAri());
      setScalingRuleId(response.getScalingRuleId());
    }};
  }

  public ScalingRule createRemoveScalingRule(String ScalingGroupId) throws ClientException {
    return createTotalCapacityRule(ScalingGroupId, 0);
  }

  public void enableScalingGroup(String scalingGroupId, String activeScalingConfigurationId) throws ClientException {
    EnableScalingGroupRequest enableScalingGroupRequest = new EnableScalingGroupRequest();
    enableScalingGroupRequest.setScalingGroupId(scalingGroupId);
    enableScalingGroupRequest.setActiveScalingConfigurationId(activeScalingConfigurationId);
    iAcsClient.getAcsResponse(enableScalingGroupRequest);
  }


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
  * */
  public void serviceInit() throws IOException, ClientException, InterruptedException {
    String vpcId = createVpc(allDefaultName);
    Thread.sleep(10000);
    String vSwitch = createVSwitch(allDefaultName, vpcId);
    Thread.sleep(5000);
    String scalingGroupId = createScalingGroup(vSwitch);
    String securityGroupId = createSecurityGroup(allDefaultName, vpcId);
    Thread.sleep(5000);
    String scalingConfigurationId = createScalingConfiguration(scalingGroupId, securityGroupId);
    ScalingRule scalingAddRule = createAddScalingRule(scalingGroupId);
    ScalingRule scalingRemoveRule = createRemoveScalingRule(scalingGroupId);
    enableScalingGroup(scalingGroupId, scalingConfigurationId);
    authorizeSecurityGroup(securityGroupId);
    AuthorizeSecurityGroupEgress(securityGroupId);
    String pairName = createPrivateKey();
    Properties properties = new Properties();
    properties.put("vpcId", vpcId);
    properties.put("switchId", vSwitch);
    properties.put("scalingGroupId", scalingGroupId);
    properties.put("securityGroupId", securityGroupId);
    properties.put("scalingConfigurationId", scalingConfigurationId);
    properties.put("scalingAddRuleAri", scalingAddRule.getScalingRuleAri());
    properties.put("scalingRemoveRuleAri", scalingRemoveRule.getScalingRuleAri());
    properties.put("pairName", pairName);
    properties.store(new FileOutputStream(new File("Aliyun.properties")), null);
  }


  /*建立专有网络 返回专有网络的id
  *create VPC and return id of VPC*/
  public String createVpc(String vpcName) throws ClientException {
    CreateVpcRequest createVpcRequest = new CreateVpcRequest();
    createVpcRequest.setVpcName(vpcName);
    CreateVpcResponse response = iAcsClient.getAcsResponse(createVpcRequest);
    return response.getVpcId();
  }

  public String createVSwitch(String vSwitchName, String VpcId) throws ClientException {
    CreateVSwitchRequest createVpcRequest = new CreateVSwitchRequest();
    createVpcRequest.setVSwitchName(vSwitchName);
    createVpcRequest.setVpcId(VpcId);
    createVpcRequest.setCidrBlock("172.31.99.0/24");
    createVpcRequest.setZoneId("cn-hongkong-c");
    CreateVSwitchResponse response = iAcsClient.getAcsResponse(createVpcRequest);
    return response.getVSwitchId();
  }



}
