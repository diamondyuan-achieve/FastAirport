package ams.services;


import ams.domain.GenericException;

public interface Service {
  void test();

  void open();

  void close();


  void createFile() throws GenericException;
}
