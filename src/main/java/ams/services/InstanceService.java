package ams.services;


import com.aliyuncs.exceptions.ClientException;

import java.io.IOException;

public interface InstanceService {

  void serviceInit() throws IOException, ClientException, InterruptedException;

  void createInstance() throws ClientException;

  void releaseInstance() throws ClientException;

  void refreshInstance() throws ClientException;


}
