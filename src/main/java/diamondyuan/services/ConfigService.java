package diamondyuan.services;

import diamondyuan.domain.Config;

import java.io.IOException;

public interface ConfigService {


  /**
   * @return 配置文件
   * @throws IOException 当文件读取错误时候报错
   */
  Config loadConfig() throws IOException;

  /**
   * 保存配置
   *
   * @param config 返回的配置类
   * @throws IOException 当文件读取错误时候报错
   */
  void saveConfig(Config config) throws IOException;

}
