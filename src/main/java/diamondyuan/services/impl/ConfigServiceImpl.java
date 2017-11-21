package diamondyuan.services.impl;

import diamondyuan.domain.Config;
import diamondyuan.domain.consts.ConfigConstants;
import diamondyuan.domain.enums.ConfigStatusEnum;
import diamondyuan.services.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


@Component
@Slf4j
public class ConfigServiceImpl implements ConfigService {

  private static final String DEFAULT_ALIYUN_CONFIG_PATH = ConfigConstants.ALIYUN_CONFIG_PATH;

  private Properties loadProperties(String path) throws IOException {
    File file = new File(path);
    if (!file.exists() || !file.isFile()) {
      return null;
    }
    Properties properties = new Properties();
    properties.load(new FileInputStream(path));
    return properties;
  }

  private void saveProperties(Properties properties, String path) throws IOException {
    properties.store(new FileOutputStream(new File(path)), null);

  }


  public Config loadConfig() throws IOException {
    Properties properties = loadProperties(DEFAULT_ALIYUN_CONFIG_PATH);
    if (properties == null) {
      Config config = new Config() {{
        setConfigStatus(ConfigStatusEnum.EMPTY);
      }};
      saveConfig(config);
      return addZone(config);
    }
    return addZone(new Config() {{
      setConfigStatus(ConfigStatusEnum.valueOf(properties.getProperty("configStatus")));
      setVpcId(properties.getProperty("vpcId"));
      setSwitchId(properties.getProperty("switchId"));
      setScalingGroupId(properties.getProperty("scalingGroupId"));
      setSecurityGroupId(properties.getProperty("securityGroupId"));
      setScalingConfigurationId(properties.getProperty("scalingConfigurationId"));
      setScalingAddRuleAri(properties.getProperty("scalingAddRuleAri"));
      setScalingRemoveRuleAri(properties.getProperty("scalingRemoveRuleAri"));
      setPairName(properties.getProperty("pairName"));
      setKeyPairPath(properties.getProperty("keyPairPath"));
      setZoneId(properties.getProperty("zoneId"));
      setRegionId(properties.getProperty("regionId"));
    }});
  }


  private Config addZone(Config config) {
    if (config.getRegionId() == null || config.getZoneId() == null) {
      config.setRegionId(ConfigConstants.DEFAULT_REGION_ID);
      config.setZoneId(ConfigConstants.DEFAULT_ZONE_ID);
    }
    return config;
  }

  public void saveConfig(Config config) throws IOException {
    Properties properties = new Properties() {{
      if (config.getConfigStatus() != null) {
        put("configStatus", config.getConfigStatus().name());
      }
      if (config.getVpcId() != null) {
        put("vpcId", config.getVpcId());
      }
      if (config.getSwitchId() != null) {
        put("switchId", config.getSwitchId());
      }
      if (config.getScalingGroupId() != null) {
        put("scalingGroupId", config.getScalingGroupId());
      }
      if (config.getSecurityGroupId() != null) {
        put("securityGroupId", config.getSecurityGroupId());
      }
      if (config.getScalingConfigurationId() != null) {
        put("scalingConfigurationId", config.getScalingConfigurationId());
      }
      if (config.getScalingAddRuleAri() != null) {
        put("scalingAddRuleAri", config.getScalingAddRuleAri());
      }
      if (config.getScalingRemoveRuleAri() != null) {
        put("scalingRemoveRuleAri", config.getScalingRemoveRuleAri());
      }
      if (config.getZoneId() != null) {
        put("zoneId", config.getZoneId());
      }
      if (config.getRegionId() != null) {
        put("regionId", config.getRegionId());
      }
      if (config.getPairName() != null) {
        put("pairName", config.getPairName());
      }
      if (config.getKeyPairPath() != null) {
        put("keyPairPath", config.getKeyPairPath());
      }
    }};
    saveProperties(properties, DEFAULT_ALIYUN_CONFIG_PATH);
  }


}
