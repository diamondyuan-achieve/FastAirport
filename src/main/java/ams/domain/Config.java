package ams.domain;

import lombok.Data;

@Data
public class Config {
  private boolean firstLoad;
  private String status;
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
