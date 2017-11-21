package diamondyuan.services.impl;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.ecs.model.v20140526.*;
import com.aliyuncs.ecs.model.v20140526.DescribeRegionsRequest;
import com.aliyuncs.ecs.model.v20140526.DescribeRegionsResponse;
import com.aliyuncs.ess.model.v20140828.*;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import diamondyuan.domain.Config;
import diamondyuan.domain.Instance;
import diamondyuan.domain.KeyPair;
import diamondyuan.domain.ScalingRule;
import diamondyuan.domain.aliyun.Region;
import diamondyuan.domain.aliyun.ScalingInstance;
import diamondyuan.domain.aliyun.Zone;
import diamondyuan.domain.consts.ConfigConstants;
import diamondyuan.domain.enums.AliyunInstanceTypeEnum;
import diamondyuan.domain.enums.ConfigStatusEnum;
import diamondyuan.services.ConfigService;
import diamondyuan.services.DiamondUtils;
import diamondyuan.services.InstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author DiamondYuan
 */
@Component
@Slf4j
public class AliyunInstanceServiceImpl implements InstanceService {


  private final IAcsClient iAcsClient;

  private final ConfigService configService;

  @Autowired
  public AliyunInstanceServiceImpl(ConfigService configService, IAcsClient iAcsClient) {
    this.configService = configService;
    this.iAcsClient = iAcsClient;
  }


  @Override
  public List<Zone> getZones(String regionId) throws ClientException {
    DescribeZonesRequest describeZonesRequest = new DescribeZonesRequest();
    DescribeZonesResponse response = iAcsClient.getAcsResponse(describeZonesRequest, DefaultProfile.getProfile(regionId));
    if (response == null || response.getZones() == null || response.getZones().size() == 0) {
      return Collections.emptyList();
    }
    return response.getZones().stream().map(o -> new Zone() {{
      setLocalName(o.getLocalName());
      setZoneId(o.getZoneId());
    }}).collect(Collectors.toList());
  }

  @Override
  public List<Region> getRegions() throws ClientException {
    DescribeRegionsRequest regionsRequest = new DescribeRegionsRequest();
    DescribeRegionsResponse response = iAcsClient.getAcsResponse(regionsRequest);
    if (response == null || response.getRegions() == null || response.getRegions().size() == 0) {
      return Collections.emptyList();
    }
    return response.getRegions().stream().map(o -> new Region() {{
      setLocalName(o.getLocalName());
      setRegionId(o.getRegionId());
    }}).collect(Collectors.toList());
  }

  /*创建一个实例*/
  @Override
  public void createInstance() throws ClientException, IOException {
    Config config = configService.loadConfig();
    execRule(config.getScalingAddRuleAri());
  }

  /*移除一个实例*/
  @Override
  public void releaseInstance() throws ClientException, IOException {
    Config config = configService.loadConfig();
    execRule(config.getScalingRemoveRuleAri());
  }


  @Override
  public List<Instance> getInstances() throws ClientException, IOException {

    Config config = configService.loadConfig();
    if (config.getScalingGroupId() == null || config.getScalingConfigurationId() == null) {
      return Collections.emptyList();
    }
    List<ScalingInstance> scalingInstanceList = getScalingInstances(config.getScalingGroupId(), config.getScalingConfigurationId());
    if (scalingInstanceList == null || scalingInstanceList.size() == 0) {
      return Collections.emptyList();
    }
    DescribeInstancesRequest instancesRequest = new DescribeInstancesRequest();
    instancesRequest.setInstanceIds(String.format("[%s]", String.join(",", scalingInstanceList.
      stream().map(o -> String.format("\"%s\"", o.getInstanceId())).collect(Collectors.toList()))));

    DescribeInstancesResponse response = iAcsClient.getAcsResponse(instancesRequest);
    return response.getInstances().stream().map(o -> new Instance() {{
      setStatus(o.getStatus());
      setKeyPairName(o.getKeyPairName());
      setId(o.getInstanceId());
      setRegionId(o.getRegionId());
      if (o.getPublicIpAddress().size() > 0) {
        setIp(o.getPublicIpAddress().get(0));
      }

    }}).collect(Collectors.toList());
  }

  public void attachKeyPair(String instanceID) throws ClientException, IOException {
    Config config = configService.loadConfig();
    if (config.getPairName() == null) {
      return;
    }
    attachKeyPair(instanceID, config.getPairName());
  }

  private void attachKeyPair(String instanceID, String attachKey) throws ClientException {
    AttachKeyPairRequest request = new AttachKeyPairRequest();
    request.setKeyPairName(attachKey);
    request.setInstanceIds(String.format("[\"%s\"]", instanceID));
    iAcsClient.getAcsResponse(request);
  }


  private List<ScalingInstance> getScalingInstances(String scalingGroupId, String activeScalingConfigurationId) throws ClientException {
    DescribeScalingInstancesRequest scalingInstancesRequest = new DescribeScalingInstancesRequest();
    scalingInstancesRequest.setScalingGroupId(scalingGroupId);
    scalingInstancesRequest.setScalingConfigurationId(activeScalingConfigurationId);
    DescribeScalingInstancesResponse response = iAcsClient.getAcsResponse(scalingInstancesRequest);
    return response.getScalingInstances().stream().map(o -> new ScalingInstance() {{
      setInstanceId(o.getInstanceId());
      setHealthStatus(o.getHealthStatus());
    }}).collect(Collectors.toList());
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
  @Override
  public void serviceInit() throws ClientException, IOException, InterruptedException {
    Config config = configService.loadConfig();
    if (!config.getConfigStatus().equals(ConfigStatusEnum.PENDING)) {
      return;
    }


    KeyPair keyPair = createPrivateKey();
    config.setPairName(keyPair.getName());
    config.setKeyPairPath(keyPair.getPath());
    String vpcId = createVpc();
    config.setVpcId(vpcId);
    configService.saveConfig(config);
    String vSwitch = createVSwitch(vpcId);
    config.setSwitchId(vSwitch);
    configService.saveConfig(config);
    String scalingGroupId = createScalingGroup(vSwitch);
    config.setScalingGroupId(scalingGroupId);
    configService.saveConfig(config);
    String securityGroupId = createSecurityGroup(vpcId);
    config.setSecurityGroupId(securityGroupId);
    configService.saveConfig(config);
    authorizeSecurityGroup(securityGroupId);
    authorizeSecurityGroupEgress(securityGroupId);
    String scalingConfigurationId = createScalingConfiguration(scalingGroupId, securityGroupId, config.getPairName());
    config.setScalingConfigurationId(scalingConfigurationId);
    configService.saveConfig(config);
    ScalingRule scalingAddRule = createAddScalingRule(scalingGroupId);
    config.setScalingAddRuleAri(scalingAddRule.getScalingRuleAri());
    configService.saveConfig(config);
    ScalingRule scalingRemoveRule = createRemoveScalingRule(scalingGroupId);
    config.setScalingRemoveRuleAri(scalingRemoveRule.getScalingRuleAri());
    enableScalingGroup(scalingGroupId, scalingConfigurationId);
    config.setConfigStatus(ConfigStatusEnum.ACTIVE);
    configService.saveConfig(config);
  }


  /**
   * 获取需要的镜像Id
   *
   * @return 返回需要的镜像ID
   * @throws ClientException 阿里云调用出错
   */
  private String getImageId() throws ClientException {
    DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest();
    describeImagesRequest.setStatus("Available");
    describeImagesRequest.setImageOwnerAlias("system");
    describeImagesRequest.setOSType("linux");
    describeImagesRequest.setPageSize(50);
    DescribeImagesResponse describeImagesResponse = iAcsClient.getAcsResponse(describeImagesRequest);
    return describeImagesResponse.getImages().stream()
      .filter(o -> o.getOSName().equals(ConfigConstants.DEFAULT_IMAGE_Name))
      .collect(Collectors.toList()).get(0).getImageName();
  }


  /**
   * 建立专有网络 返回专有网络的ID
   * create VPC and return id of VPC
   */
  private String createVpc() throws ClientException {
    CreateVpcRequest createVpcRequest = new CreateVpcRequest();
    CreateVpcResponse response = iAcsClient.getAcsResponse(createVpcRequest);
    return response.getVpcId();
  }

  /**
   * 在指定的VPC上创建交换机 返回交换机ID
   */
  private String createVSwitch(String vpcId) throws ClientException, IOException, InterruptedException {
    Config config = configService.loadConfig();
    String vpcStatus = getVpcStatus(vpcId);
    if (!vpcStatus.equals("Available")) {
      Thread.sleep(1000);
      log.debug("vpc id{} status{}", vpcId, vpcStatus);
      return createVSwitch(vpcId);
    }
    CreateVSwitchRequest createVpcRequest = new CreateVSwitchRequest();
    createVpcRequest.setVpcId(vpcId);
    createVpcRequest.setCidrBlock(ConfigConstants.DEFAULT_CIDR_BLOCK);
    createVpcRequest.setZoneId(config.getZoneId());
    CreateVSwitchResponse response = iAcsClient.getAcsResponse(createVpcRequest);
    return response.getVSwitchId();
  }

  /**
   * 获取vSwitch的状态
   * Get status of vSwitch
   *
   * @param vSwitchId vSwitchId
   * @return Status of vSwitch
   * @throws ClientException 阿里云调用失败
   */
  private String getSwitchStatus(String vSwitchId) throws ClientException {
    DescribeVSwitchesRequest describeVSwitchesRequest = new DescribeVSwitchesRequest();
    describeVSwitchesRequest.setVSwitchId(vSwitchId);
    DescribeVSwitchesResponse response = iAcsClient.getAcsResponse(describeVSwitchesRequest);
    return response.getVSwitches().get(0).getStatus();
  }


  /**
   * 获取vpcId的状态
   * Get status of vpcId
   *
   * @param vpcId vpcId
   * @return status of vpcId
   * @throws ClientException 阿里云调用失败
   */
  private String getVpcStatus(String vpcId) throws ClientException {
    DescribeVpcsRequest describeVpcsRequest = new DescribeVpcsRequest();
    describeVpcsRequest.setVpcId(vpcId);
    DescribeVpcsResponse response = iAcsClient.getAcsResponse(describeVpcsRequest);
    response.getVpcs().get(0).getStatus();
    return response.getVpcs().get(0).getStatus();
  }


  private ScalingRule createAddScalingRule(String scalingGroupId) throws ClientException {
    return createTotalCapacityRule(scalingGroupId, 1);
  }

  private ScalingRule createRemoveScalingRule(String scalingGroupId) throws ClientException {
    return createTotalCapacityRule(scalingGroupId, 0);
  }


  private ScalingRule createTotalCapacityRule(String scalingGroupId, int count) throws ClientException {
    CreateScalingRuleRequest createScalingRuleRequest = new CreateScalingRuleRequest();
    createScalingRuleRequest.setScalingGroupId(scalingGroupId);
    createScalingRuleRequest.setAdjustmentType("TotalCapacity");
    createScalingRuleRequest.setAdjustmentValue(count);
    CreateScalingRuleResponse response = iAcsClient.getAcsResponse(createScalingRuleRequest);
    return new ScalingRule() {{
      setScalingRuleAri(response.getScalingRuleAri());
      setScalingRuleId(response.getScalingRuleId());
    }};
  }


  private void enableScalingGroup(String scalingGroupId, String activeScalingConfigurationId) throws ClientException {
    EnableScalingGroupRequest enableScalingGroupRequest = new EnableScalingGroupRequest();
    enableScalingGroupRequest.setScalingGroupId(scalingGroupId);
    enableScalingGroupRequest.setActiveScalingConfigurationId(activeScalingConfigurationId);
    iAcsClient.getAcsResponse(enableScalingGroupRequest);
  }


  /**
   * 为指定的vpc创建安全组
   *
   * @param vpvId vpcId
   * @return SecurityGroupId
   * @throws ClientException ClientException
   */
  private String createSecurityGroup(String vpvId) throws ClientException {
    CreateSecurityGroupRequest createSecurityGroup = new CreateSecurityGroupRequest();
    createSecurityGroup.setVpcId(vpvId);
    CreateSecurityGroupResponse response = iAcsClient.getAcsResponse(createSecurityGroup);
    return response.getSecurityGroupId();
  }

  private String createScalingConfiguration(String scalingGroupId, String securityGroupId, String keypairName) throws ClientException {
    CreateScalingConfigurationRequest createScalingConfigurationRequest = new CreateScalingConfigurationRequest();
    createScalingConfigurationRequest.setInstanceType(AliyunInstanceTypeEnum.ECS_XN4_SMALL.toString());
    createScalingConfigurationRequest.setImageId(getImageId());
    createScalingConfigurationRequest.setInternetChargeType("PayByTraffic");
    createScalingConfigurationRequest.setSystemDiskCategory("cloud_efficiency");
    createScalingConfigurationRequest.setInternetMaxBandwidthOut(100);
    createScalingConfigurationRequest.setIoOptimized("optimized");
    createScalingConfigurationRequest.setSecurityGroupId(securityGroupId);
    createScalingConfigurationRequest.setScalingGroupId(scalingGroupId);
    createScalingConfigurationRequest.setKeyPairName(keypairName);
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

  private void authorizeSecurityGroupEgress(String securityGroupId) throws ClientException {
    AuthorizeSecurityGroupEgressRequest authorizeSecurityGroupEgressRequest = new AuthorizeSecurityGroupEgressRequest();
    authorizeSecurityGroupEgressRequest.setSecurityGroupId(securityGroupId);
    authorizeSecurityGroupEgressRequest.setDestCidrIp("0.0.0.0/0");
    authorizeSecurityGroupEgressRequest.setIpProtocol("all");
    authorizeSecurityGroupEgressRequest.setPortRange("-1/-1");
    iAcsClient.getAcsResponse(authorizeSecurityGroupEgressRequest);
  }

  /**
   * 根据伸缩组名称创建伸缩组 返回伸缩组id
   */
  private String createScalingGroup(String vSwitchId) throws ClientException, InterruptedException {
    String switchStatus = getSwitchStatus(vSwitchId);
    if (!switchStatus.equals("Available")) {
      Thread.sleep(1000);
      return createScalingGroup(vSwitchId);
    }
    CreateScalingGroupRequest createScalingGroupRequest = new CreateScalingGroupRequest();
    createScalingGroupRequest.setMaxSize(ConfigConstants.SCALING_GROUP_MAX_SIZE);
    createScalingGroupRequest.setMinSize(ConfigConstants.SCALING_GROUP_MIN_SIZE);
    createScalingGroupRequest.setVSwitchId(vSwitchId);
    CreateScalingGroupResponse response = iAcsClient.getAcsResponse(createScalingGroupRequest);
    return response.getScalingGroupId();
  }

  private void execRule(String ruleAri) throws ClientException {
    ExecuteScalingRuleRequest executeScalingRuleRequest = new ExecuteScalingRuleRequest();
    executeScalingRuleRequest.setScalingRuleAri(ruleAri);
    iAcsClient.getAcsResponse(executeScalingRuleRequest);
  }

  private KeyPair createPrivateKey() throws ClientException, IOException {
    String pairName = UUID.randomUUID().toString();
    while (!(pairName.charAt(0) < '0' || pairName.charAt(0) > '9')) {
      pairName = UUID.randomUUID().toString();
    }
//    File sshFolder = new File(ConfigConstants.KEY_PATH);
//    if (!sshFolder.exists()) {
//      if (sshFolder.mkdir()) {
//        log.debug("成功创建文件夹");
//      } else {
//        log.error("创建文件失败");
//      }
//    }
    String finalPairPath = String.format("%s.pem", pairName);
    CreateKeyPairRequest request = new CreateKeyPairRequest();
    request.setKeyPairName(pairName);
    String privateKey = iAcsClient.getAcsResponse(request).getPrivateKeyBody();
    DiamondUtils.saveFile(privateKey, finalPairPath);
    KeyPair keyPair = new KeyPair();
    keyPair.setName(pairName);
    keyPair.setPath(finalPairPath);
    return keyPair;
  }


}
