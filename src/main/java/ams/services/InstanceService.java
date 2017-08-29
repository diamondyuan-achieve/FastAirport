package ams.services;


import ams.domain.GenericException;
import com.aliyuncs.exceptions.ClientException;

public interface InstanceService {


  /*创建一个实例*/
  String createInstance();


  void test();

  void open();

  void close();

  void createPrivateKey() throws GenericException;


  String createSecurityGroup(String securityGroupName,String vpvId) throws ClientException;

  String createVpc(String vpcName) throws ClientException;

  String createVSwitch(String vSwitchName,String VpcId) throws ClientException;

  void authorizeSecurityGroup(String securityGroupId) throws ClientException;

  void AuthorizeSecurityGroupEgress(String securityGroupId) throws ClientException;





}
