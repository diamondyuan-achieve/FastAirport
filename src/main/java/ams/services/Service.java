package ams.services;



public interface Service {
  void test();
  void open();
  void close();
  String createNewKeyPair(String pairName);
}
