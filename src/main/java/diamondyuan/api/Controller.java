package diamondyuan.api;


import com.aliyuncs.exceptions.ClientException;
import com.google.common.eventbus.EventBus;
import com.jcraft.jsch.JSchException;
import diamondyuan.domain.*;
import diamondyuan.domain.enums.ActionEventTypeEnum;
import diamondyuan.event.domain.ActionEvent;
import diamondyuan.services.ConfigService;
import diamondyuan.services.InstanceService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * Controller
 *
 * @author DiamondYuan
 */
@RestController
public class Controller {


  private final InstanceService instanceService;
  private final EventBus eventBus;
  private final ConfigService configService;

  @Autowired
  public Controller(InstanceService instanceService, ConfigService configService, EventBus eventBus) {
    this.instanceService = instanceService;
    this.configService = configService;
    this.eventBus = eventBus;
  }

  /*阿里云初始化*/
  @GetMapping(path = "/api/v1/aliyun/config")
  public ResultWrapper<Config> config() throws GenericException, ClientException, InterruptedException, IOException {
    return new ResultWrapper<>(configService.loadConfig());
  }


  /*阿里云初始化*/
  @GetMapping(path = "/api/v1/aliyun")
  public ResultWrapper<Config> init() throws GenericException, ClientException, InterruptedException, IOException {
    eventBus.post(new ActionEvent() {{
      setAction(ActionEventTypeEnum.ALI_INIT);
    }});
    return new ResultWrapper<>(configService.loadConfig());
  }

  /*获得阿里云实例状态*/
  @GetMapping(path = "/api/v1/aliyun/Instances")
  public ResultWrapper<ListResult<Instance>> aliyunInstance() throws GenericException, ClientException, InterruptedException, IOException {
    return ListResult.of(instanceService.getInstances());
  }

  /*创建一个阿里云实例*/
  @PostMapping(path = "/api/v1/aliyun/Instance")
  public ResultWrapper<ListResult<Instance>> aliyunCreateInstance() throws IOException, ClientException {
    eventBus.post(new ActionEvent() {{
      setAction(ActionEventTypeEnum.ALI_CREATE_INSTANCE);
    }});
    return ListResult.of(instanceService.getInstances());
  }


  /*阿里云实例进行初始化*/
  @PutMapping(path = "/api/v1/aliyun/Instance/{instanceId}/keyPair")
  @ApiOperation(value = "为实例绑定SSH密钥", produces = "application/json")
  public ResultWrapper<ListResult<Instance>> setKeyPair(
    @ApiParam(value = "实例Id", required = true) @PathVariable(value = "instanceId") String instanceId
  ) throws JSchException, IOException, ClientException, InterruptedException {
    List<Instance> instanceList = instanceService.getInstances();
    if (instanceList == null || instanceList.size() == 0 ||
      instanceList.stream().filter((Instance o) -> o.getId().equals(instanceId) && o.getKeyPairName() == null).count() == 0) {
      return ListResult.of(instanceList);
    }
    instanceService.attachKeyPair(instanceId);
    return ListResult.of(instanceList);
  }

  /*移除阿里云实例*/
  @DeleteMapping(path = "/api/v1/aliyun/Instance")
  public ResultWrapper<ListResult<Instance>> aliyunReleaseInstance() throws IOException, ClientException, InterruptedException {
    eventBus.post(new ActionEvent() {{
      setAction(ActionEventTypeEnum.ALI_RELEASE_INSTANCE);
    }});
    return ListResult.of(instanceService.getInstances());
  }


}
