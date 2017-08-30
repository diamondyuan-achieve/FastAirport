package ams.services;


import ams.domain.GenericException;
import ams.domain.ScalingRule;
import com.aliyuncs.exceptions.ClientException;

import java.io.IOException;

public interface InstanceService {

  void serviceInit() throws IOException,GenericException,ClientException,InterruptedException;

  String createPrivateKey() throws ClientException, IOException;

  String getInstanceId(String scalingGroupId, String activeScalingConfigurationId) throws ClientException;

  String createSecurityGroup(String securityGroupName, String vpvId) throws ClientException;

  String createScalingConfiguration(String scalingGroupId, String securityGroupId) throws ClientException;

  String createVpc(String vpcName) throws ClientException;

  String createVSwitch(String vSwitchName, String VpcId) throws ClientException;

  void authorizeSecurityGroup(String securityGroupId) throws ClientException;

  String createScalingGroup(String vSwitchID) throws ClientException;

  void AuthorizeSecurityGroupEgress(String securityGroupId) throws ClientException;

  ScalingRule createAddScalingRule(String ScalingGroupId) throws ClientException;

  ScalingRule createRemoveScalingRule(String ScalingGroupId) throws ClientException;

  void enableScalingGroup(String scalingGroupId,String activeScalingConfigurationId) throws ClientException;
}
