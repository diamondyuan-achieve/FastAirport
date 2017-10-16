package ams.api;


import ams.domain.Config;
import ams.domain.GenericException;
import ams.domain.Instance;
import ams.domain.ResultWrapper;
import ams.event.domain.ActionEvent;
import ams.event.domain.ActionEventType;
import ams.services.InstanceService;
import com.aliyuncs.exceptions.ClientException;
import com.google.common.eventbus.EventBus;
import com.jcraft.jsch.JSchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Controller
 *
 * @author DiamondYuan
 */
@RestController
public class Controller {


  @Autowired
  InstanceService instanceService;
  private Config config;
  private EventBus eventBus;
  @Autowired
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
  public ResultWrapper<Instance> aliyunInstance() {
    eventBus.post(new ActionEvent() {{
      setAction(ActionEventType.ALI_INSTANCE_REFRESH);
    }});
    return new ResultWrapper<>(instance);
  }

  /*创建一个阿里云实例*/
  @PostMapping(path = "/api/aliyun/Instance")
  public ResultWrapper<Instance> aliyunCreateInstance() {
    eventBus.post(new ActionEvent() {{
      setAction(ActionEventType.ALI_CREATE_INSTANCE);
    }});
    return new ResultWrapper<>(instance);
  }


  /*阿里云实例进行初始化*/
  @GetMapping(path = "/api/aliyun/Instance/init")
  public ResultWrapper<Instance> installDocker() throws JSchException, IOException, ClientException, InterruptedException {
    eventBus.post(new ActionEvent() {{
      setAction(ActionEventType.ALI_INSTANCE_INIT);
    }});
    return new ResultWrapper<>(instance);
  }

  /*移除阿里云实例*/
  @DeleteMapping(path = "/api/aliyun/Instance")
  public ResultWrapper<Instance> aliyunReleaseInstance() {
    eventBus.post(new ActionEvent() {{
      setAction(ActionEventType.ALI_RELEASE_INSTANCE);
    }});
    return new ResultWrapper<>(instance);
  }


}
