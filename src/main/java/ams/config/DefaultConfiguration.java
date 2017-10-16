package ams.config;

import ams.domain.Config;
import ams.domain.Instance;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * @author DiamondYuan
 */
@Configuration
@EnableAsync
public class DefaultConfiguration {


  @Bean
  IAcsClient iAcsClient(
    @Value("${accessKey}") String accessKey,
    @Value("${accessSecret}") String accessSecret,
    @Value("${regionId}") String regionId
  ) {
    return new DefaultAcsClient(DefaultProfile.getProfile(regionId, accessKey, accessSecret));
  }

  @Bean
  Instance instance() {
    return new Instance() {{
      setExist(false);
    }};
  }

  @Bean
  Config getConfig(@Value("${default.conf.path}") String confPath) throws IOException {
    File confFile = new File(confPath);
    if (confFile.exists() && confFile.isFile()) {
      Properties properties = new Properties();
      properties.load(new FileInputStream(confPath));
      return new Config() {{
        setFirstLoad(false);
        setVpcId(properties.getProperty("vpcId"));
        setSwitchId(properties.getProperty("switchId"));
        setScalingGroupId(properties.getProperty("scalingGroupId"));
        setSecurityGroupId(properties.getProperty("securityGroupId"));
        setScalingConfigurationId(properties.getProperty("scalingConfigurationId"));
        setScalingAddRuleAri(properties.getProperty("scalingAddRuleAri"));
        setScalingRemoveRuleAri(properties.getProperty("scalingRemoveRuleAri"));
        setPairName(properties.getProperty("pairName"));
        setKeyPairPath(properties.getProperty("keyPairPath"));
        setStatus("ok");
      }};
    }
    return new Config() {{
      setFirstLoad(true);
    }};
  }

  @Bean
  public EventBus eventBus() {
    return new AsyncEventBus(Executors.newFixedThreadPool(6));
  }


}
