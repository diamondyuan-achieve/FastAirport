package ams.services.impl;

import ams.domain.Config;
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
import java.util.UUID;

@Component
@Slf4j
public class AliyunInstanceService implements InstanceService {

  private static final String VPC_SWITCH_ZONE_ID = "cn-hongkong-c";
  private static final String DEFAULT_CIDR_BLOCK = "172.31.99.0/24";
  private static final String DEFAULT_IMAGE_ID = "ubuntu_16_0402_64_40G_alibase_20170711.vhd";
  private static final String DEFAULT_INSTALCE_TYPE = "ecs.xn4.small";
  private static final int scalingGroupMaxSize = 1;
  private static final int scalingGroupMinSize = 0;


  private Config config;

  @Value("config/fastAirport.conf")
  private String DEFAULT_CONFIG_PATH;


  @Autowired
  private void getConfig(Config config) {
    this.config = config;
  }

  private Instance instance;

  @Autowired
  public void setInstance(Instance instance) {
    this.instance = instance;
  }

  private IAcsClient iAcsClient;

  @Autowired
  private void setIcsClient(IAcsClient iAcsClient) {
    this.iAcsClient = iAcsClient;
  }


  /*创建一个实例*/
  public void createInstance() throws ClientException {
    instance.setExist(true);
    execRule(config.getScalingAddRuleAri());
  }

  /*移除一个实例*/
  public void releaseInstance() throws ClientException {
    execRule(config.getScalingRemoveRuleAri());
    instance.setExist(false);
  }


  /*刷新实例状态*/
  public void refreshInstance() throws ClientException {
    if (instance.getId() == null) {
      instance.setId(getInstanceId(config.getScalingGroupId(), config.getScalingConfigurationId()));
    }
    DescribeInstancesRequest instancesRequest = new DescribeInstancesRequest();
    instancesRequest.setInstanceIds(String.format("[\"%s\"]", instance.getId()));
    DescribeInstancesResponse response = iAcsClient.getAcsResponse(instancesRequest);
    if (response.getInstances().size() > 0) {
      DescribeInstancesResponse.Instance aliInstance = response.getInstances().get(0);
      if (aliInstance.getPublicIpAddress().size() > 0) {
        instance.setIp(aliInstance.getPublicIpAddress().get(0));
      }
      instance.setStatus(aliInstance.getStatus());
      instance.setRegionId(aliInstance.getRegionId());
      instance.setId(aliInstance.getInstanceId());
    }
  }


  private String attachKeyPair(String instanceID, String attachKey) throws ClientException {
    AttachKeyPairRequest request = new AttachKeyPairRequest();
    request.setKeyPairName(attachKey);
    request.setInstanceIds(String.format("[\"%s\"]", instanceID));
    AttachKeyPairResponse response = iAcsClient.getAcsResponse(request);
    return response.getFailCount();
  }


  private String getInstanceId(String scalingGroupId, String activeScalingConfigurationId) throws ClientException {
    DescribeScalingInstancesRequest scalingInstancesRequest = new DescribeScalingInstancesRequest();
    scalingInstancesRequest.setScalingGroupId(scalingGroupId);
    scalingInstancesRequest.setScalingConfigurationId(activeScalingConfigurationId);
    DescribeScalingInstancesResponse response = iAcsClient.getAcsResponse(scalingInstancesRequest);
    if (response.getScalingInstances().size() > 0) {
      return response.getScalingInstances().get(0).getInstanceId();
    }
    return null;
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
  public void serviceInit() throws ClientException, IOException, InterruptedException {
    String vpcId = createVpc();
    config.setVpcId(vpcId);
    Thread.sleep(10000);
    String vSwitch = createVSwitch(vpcId);
    Thread.sleep(5000);
    config.setSwitchId(vSwitch);
    String scalingGroupId = createScalingGroup(vSwitch);
    config.setScalingGroupId(scalingGroupId);
    String securityGroupId = createSecurityGroup(vpcId);
    config.setSecurityGroupId(securityGroupId);
    Thread.sleep(3000);
    String scalingConfigurationId = createScalingConfiguration(scalingGroupId, securityGroupId);
    config.setScalingConfigurationId(scalingConfigurationId);
    ScalingRule scalingAddRule = createAddScalingRule(scalingGroupId);
    config.setScalingAddRuleAri(scalingAddRule.getScalingRuleAri());
    ScalingRule scalingRemoveRule = createRemoveScalingRule(scalingGroupId);
    config.setScalingRemoveRuleAri(scalingRemoveRule.getScalingRuleAri());
    enableScalingGroup(scalingGroupId, scalingConfigurationId);
    authorizeSecurityGroup(securityGroupId);
    AuthorizeSecurityGroupEgress(securityGroupId);
    String pairName = createPrivateKey();
    config.setPairName(pairName);
    Properties properties = new Properties();
    properties.put("vpcId", vpcId);
    properties.put("switchId", vSwitch);
    properties.put("scalingGroupId", scalingGroupId);
    properties.put("securityGroupId", securityGroupId);
    properties.put("scalingConfigurationId", scalingConfigurationId);
    properties.put("scalingAddRuleAri", scalingAddRule.getScalingRuleAri());
    properties.put("scalingRemoveRuleAri", scalingRemoveRule.getScalingRuleAri());
    properties.put("pairName", pairName);
    properties.store(new FileOutputStream(new File(DEFAULT_CONFIG_PATH)), null);
    config.setStatus("ok");
  }


  /*建立专有网络 返回专有网络的id
  *create VPC and return id of VPC*/
  private String createVpc() throws ClientException {
    CreateVpcRequest createVpcRequest = new CreateVpcRequest();
    CreateVpcResponse response = iAcsClient.getAcsResponse(createVpcRequest);
    return response.getVpcId();
  }

  /*在指定的VPC上创建交换机 返回交换机ID*/
  private String createVSwitch(String VpcId) throws ClientException {
    CreateVSwitchRequest createVpcRequest = new CreateVSwitchRequest();
    createVpcRequest.setVpcId(VpcId);
    createVpcRequest.setCidrBlock(DEFAULT_CIDR_BLOCK);
    createVpcRequest.setZoneId(VPC_SWITCH_ZONE_ID);
    CreateVSwitchResponse response = iAcsClient.getAcsResponse(createVpcRequest);
    return response.getVSwitchId();
  }


  private ScalingRule createAddScalingRule(String ScalingGroupId) throws ClientException {
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

  private ScalingRule createRemoveScalingRule(String ScalingGroupId) throws ClientException {
    return createTotalCapacityRule(ScalingGroupId, 0);
  }


  private void enableScalingGroup(String scalingGroupId, String activeScalingConfigurationId) throws ClientException {
    EnableScalingGroupRequest enableScalingGroupRequest = new EnableScalingGroupRequest();
    enableScalingGroupRequest.setScalingGroupId(scalingGroupId);
    enableScalingGroupRequest.setActiveScalingConfigurationId(activeScalingConfigurationId);
    iAcsClient.getAcsResponse(enableScalingGroupRequest);
  }


  /*根据安全组名称创建安全组*/
  private String createSecurityGroup(String vpvId) throws ClientException {
    CreateSecurityGroupRequest createSecurityGroup = new CreateSecurityGroupRequest();
    createSecurityGroup.setVpcId(vpvId);
    CreateSecurityGroupResponse response = iAcsClient.getAcsResponse(createSecurityGroup);
    return response.getSecurityGroupId();
  }

  private String createScalingConfiguration(String scalingGroupId, String securityGroupId) throws ClientException {
    CreateScalingConfigurationRequest createScalingConfigurationRequest = new CreateScalingConfigurationRequest();
    createScalingConfigurationRequest.setInstanceType(DEFAULT_INSTALCE_TYPE);
    createScalingConfigurationRequest.setImageId(DEFAULT_IMAGE_ID);
    createScalingConfigurationRequest.setInternetChargeType("PayByTraffic");
    createScalingConfigurationRequest.setSystemDiskCategory("cloud_efficiency");
    createScalingConfigurationRequest.setInternetMaxBandwidthOut(100);
    createScalingConfigurationRequest.setIoOptimized("optimized");
    createScalingConfigurationRequest.setSecurityGroupId(securityGroupId);
    createScalingConfigurationRequest.setScalingGroupId(scalingGroupId);
    CreateScalingConfigurationResponse response = iAcsClient.getAcsResponse(createScalingConfigurationRequest);
    return response.getScalingConfigurationId();
  }

  private void authorizeSecurityGroup(String securityGroupId) throws ClientException {
    AuthorizeSecurityGroupRequest authorizeSecurityGroupRequest = new AuthorizeSecurityGroupRequest();
    authorizeSecurityGroupRequest.setSecurityGroupId(securityGroupId);
    authorizeSecurityGroupRequest.setSourceCidrIp("0.0.0.0/0");
    authorizeSecurityGroupRequest.setIpProtocol("all");
    authorizeSecurityGroupRequest.setPortRange("-1/-1");
    iAcsClient.getAcsResponse(authorizeSecurityGroupRequest);
  }

  private void AuthorizeSecurityGroupEgress(String securityGroupId) throws ClientException {
    AuthorizeSecurityGroupEgressRequest authorizeSecurityGroupEgressRequest = new AuthorizeSecurityGroupEgressRequest();
    authorizeSecurityGroupEgressRequest.setSecurityGroupId(securityGroupId);
    authorizeSecurityGroupEgressRequest.setDestCidrIp("0.0.0.0/0");
    authorizeSecurityGroupEgressRequest.setIpProtocol("all");
    authorizeSecurityGroupEgressRequest.setPortRange("-1/-1");
    iAcsClient.getAcsResponse(authorizeSecurityGroupEgressRequest);
  }

  /*根据伸缩组名称创建伸缩组 返回伸缩组id*/
  private String createScalingGroup(String vSwitchID) throws ClientException {
    CreateScalingGroupRequest createScalingGroupRequest = new CreateScalingGroupRequest();
    createScalingGroupRequest.setMaxSize(scalingGroupMaxSize);
    createScalingGroupRequest.setMinSize(scalingGroupMinSize);
    createScalingGroupRequest.setVSwitchId(vSwitchID);
    CreateScalingGroupResponse response = iAcsClient.getAcsResponse(createScalingGroupRequest);
    return response.getScalingGroupId();
  }

  private void execRule(String ruleAri) throws ClientException {
    ExecuteScalingRuleRequest executeScalingRuleRequest = new ExecuteScalingRuleRequest();
    executeScalingRuleRequest.setScalingRuleAri(ruleAri);
    iAcsClient.getAcsResponse(executeScalingRuleRequest);
  }


  private String createNewKeyPair(String pairName) throws ClientException {
    CreateKeyPairRequest request = new CreateKeyPairRequest();
    request.setKeyPairName(pairName);
    CreateKeyPairResponse response = iAcsClient.getAcsResponse(request);
    return response.getPrivateKeyBody();
  }


  private String createPrivateKey() throws ClientException, IOException {
    String pairName = UUID.randomUUID().toString();
    while (!(pairName.charAt(0) < '0' || pairName.charAt(0) > '9')) {
      pairName = UUID.randomUUID().toString();
    }
    File sshFolder = new File(DEFAULT_CONFIG_PATH).getParentFile();
    if (!sshFolder.exists()) {
      if (sshFolder.mkdir()) {
        log.debug("成功创建文件夹");
      } else {
        log.error("创建文件失败");
      }
    }
    String finalPairPath = String.format("%s/%s",sshFolder.getPath(),pairName);
    String privateKey = createNewKeyPair(pairName);
    DiamondUtils.saveFile(privateKey, finalPairPath);
    return pairName;
  }


}
