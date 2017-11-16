package diamondyuan.domain;

import diamondyuan.domain.enums.ConfigStatusEnum;
import lombok.Data;

/**
 * @author DiamondYuan
 */
@Data
public class Config {
  private ConfigStatusEnum configStatus;
  private String vpcId;
  private String switchId;
  private String scalingGroupId;
  private String securityGroupId;
  private String scalingConfigurationId;
  private String scalingAddRuleAri;
  private String scalingRemoveRuleAri;
  private String pairName;
  private String keyPairPath;
}
