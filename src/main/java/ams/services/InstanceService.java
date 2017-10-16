package ams.services;


import com.aliyuncs.exceptions.ClientException;
import com.jcraft.jsch.JSchException;

import java.io.IOException;

/**
 * @author DiamondYuan
 */
public interface InstanceService {

  void serviceInit() throws IOException, ClientException, InterruptedException;

  void createInstance() throws ClientException;

  void releaseInstance() throws ClientException;

  void refreshInstance() throws ClientException;

  void instanceInit() throws JSchException, IOException, ClientException, InterruptedException;


}
