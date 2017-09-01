package ams.api;


import ams.domain.Config;
import ams.domain.GenericException;
import ams.domain.Instance;
import ams.domain.ResultWrapper;
import ams.event.domain.ActionEvent;
import ams.event.domain.ActionEventType;
import com.aliyuncs.exceptions.ClientException;
import com.google.common.eventbus.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class Controller {


  String INSTALL_DOCKER = "curl -s https://get.docker.com/ | sudo sh";

  String INSTALL_SS = "docker pull mritd/shadowsocks";

  String SS_RUN = "docker run -dt --name ss -p %s:%s mritd/shadowsocks -s \"-s 0.0.0.0 -p 6443 -m aes-256-cfb -k test123 --fast-open\"";

  private Config config;
  private EventBus eventBus;
  private Instance instance;

  @Autowired
  private void setConfig(Config config, EventBus eventBus, Instance instance) {
    this.eventBus = eventBus;
    this.config = config;
    this.instance = instance;
  }


  /*阿里云初始化*/
  @GetMapping(path = "/api/aliyun")
  public ResultWrapper<Config> init() throws GenericException, ClientException, InterruptedException, IOException {
    eventBus.post(new ActionEvent() {{
      setAction(ActionEventType.ALI_INIT);
    }});
    return new ResultWrapper<>(config);
  }

  /*获得阿里云实例状态*/
  @GetMapping(path = "/api/aliyun/Instance")
  public ResultWrapper<Instance> AliyunInstance() {
    eventBus.post(new ActionEvent() {{
      setAction(ActionEventType.ALI_INSTANCE_REFRESH);
    }});
    return new ResultWrapper<>(instance);
  }

  /*创建一个阿里云实例*/
  @PostMapping(path = "/api/aliyun/Instance")
  public ResultWrapper<Instance> AliyunCreateInstance() {
    eventBus.post(new ActionEvent() {{
      setAction(ActionEventType.ALI_CREATE_INSTANCE);
    }});
    return new ResultWrapper<>(instance);
  }

  /*移除阿里云实例*/
  @DeleteMapping(path = "/api/aliyun/Instance")
  public ResultWrapper<Instance> AliyunReleaseInstance() {
    eventBus.post(new ActionEvent() {{
      setAction(ActionEventType.ALI_RELEASE_INSTANCE);
    }});
    return new ResultWrapper<>(instance);
  }


}
