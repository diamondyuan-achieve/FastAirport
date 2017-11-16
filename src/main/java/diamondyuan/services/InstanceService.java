package diamondyuan.services;


import diamondyuan.domain.Instance;
import com.aliyuncs.exceptions.ClientException;

import java.io.IOException;
import java.util.List;

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
  void createInstance() throws ClientException,IOException;

  /**
   * 释放实例
   *
   */
  void releaseInstance() throws ClientException,IOException;





  List<Instance> getInstances() throws ClientException,IOException;

//
//  /**
//   * 实例初始化
//   *
//   */
//  void instanceInit() throws JSchException, IOException, ClientException, InterruptedException;


}
