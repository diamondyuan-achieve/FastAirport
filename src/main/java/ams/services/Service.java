package ams.services;


import java.io.IOException;

public interface Service {
  void test();

  void open();

  void close();

  String createNewKeyPair(String pairName);

  void createFile(String pairName) throws IOException;
}
