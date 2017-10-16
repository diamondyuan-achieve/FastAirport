package ams.services;


import com.aliyuncs.exceptions.ClientException;
import com.jcraft.jsch.JSchException;

import java.io.IOException;

/**
 * @author DiamondYuan
 */
public interface InstanceService {

  /**
   * 所有配置初始化
   */
  void serviceInit() throws IOException, ClientException, InterruptedException;


  /**
   * 创建实例
   *
   */
  void createInstance() throws ClientException;

  /**
   * 释放实例
   *
   */
  void releaseInstance() throws ClientException;

  /**
   * 刷新实例状态
   *
   */
  void refreshInstance() throws ClientException;

  /**
   * 实例初始化
   *
   */
  void instanceInit() throws JSchException, IOException, ClientException, InterruptedException;


}
