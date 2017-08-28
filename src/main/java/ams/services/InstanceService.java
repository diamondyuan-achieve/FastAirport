package ams.services;


import ams.domain.GenericException;

public interface InstanceService {


  /*创建一个实例*/
  String createInstance();


  void test();

  void open();

  void close();

  void createPrivateKey() throws GenericException;
}
