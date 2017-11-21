package diamondyuan.services.impl;

import diamondyuan.config.DefaultConfiguration;
import diamondyuan.domain.Config;
import diamondyuan.domain.enums.ConfigStatusEnum;
import diamondyuan.services.ConfigService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.core.Is.is;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(DefaultConfiguration.class)
public class ConfigServiceImplTest {

  @Autowired
  ConfigService configService;

  /**
   * Test Config load and Save
   *
   * @throws IOException IOException
   */
  @Test
  public void saveLoadConfig() throws IOException {
    Config config = new Config() {{
      setConfigStatus(ConfigStatusEnum.EMPTY);
      setVpcId(UUID.randomUUID().toString());
      setSwitchId(UUID.randomUUID().toString());
      setScalingGroupId(UUID.randomUUID().toString());
      setSecurityGroupId(UUID.randomUUID().toString());
      setScalingConfigurationId(UUID.randomUUID().toString());
      setScalingAddRuleAri(UUID.randomUUID().toString());
      setScalingRemoveRuleAri(UUID.randomUUID().toString());
      setKeyPairPath(UUID.randomUUID().toString());
      setPairName(UUID.randomUUID().toString());
      setZoneId(UUID.randomUUID().toString());
      setRegionId(UUID.randomUUID().toString());
    }};
    configService.saveConfig(config);
    Config loadConfig = configService.loadConfig();
    Assert.assertThat(config.equals(loadConfig), is(true));
  }
}