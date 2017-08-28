package ams.services;


import ams.domain.GenericException;

public interface Service {
  void test();

  void open();

  void close();

  void createPrivateKey() throws GenericException;
}
