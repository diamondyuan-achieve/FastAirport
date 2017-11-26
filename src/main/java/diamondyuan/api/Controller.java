package diamondyuan.api;


import com.aliyuncs.exceptions.ClientException;
import com.google.common.eventbus.EventBus;
import com.google.zxing.WriterException;
import com.jcraft.jsch.JSchException;
import diamondyuan.domain.*;
import diamondyuan.domain.aliyun.Region;
import diamondyuan.domain.aliyun.Zone;
import diamondyuan.domain.enums.ActionEventTypeEnum;
import diamondyuan.event.domain.ActionEvent;
import diamondyuan.services.ConfigService;
import diamondyuan.services.InstanceService;
import diamondyuan.utils.Qrcode;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

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

  @GetMapping(path = "/api/v1/aliyun/config")
  @ApiOperation(value = "读取阿里云配置", produces = "application/json")
  public ResultWrapper<Config> config() throws GenericException, ClientException, InterruptedException, IOException {
    return new ResultWrapper<>(configService.loadConfig());
  }


  @GetMapping(path = "/api/v1/aliyun")
  @ApiOperation(value = "阿里云配置初始化", produces = "application/json")
  public ResultWrapper<Config> init() throws GenericException, ClientException, InterruptedException, IOException {
    eventBus.post(new ActionEvent() {{
      setAction(ActionEventTypeEnum.ALI_INIT);
    }});
    return new ResultWrapper<>(configService.loadConfig());
  }

  @GetMapping(path = "/api/v1/aliyun/Instances")
  @ApiOperation(value = "获得当前配置下阿里云实例列表", produces = "application/json")
  public ResultWrapper<ListResult<Instance>> aliyunInstance() throws GenericException, ClientException, InterruptedException, IOException {
    return ListResult.of(instanceService.getInstances());
  }


  @GetMapping(path = "/api/v1/aliyun/Instances/{instanceId}")
  @ApiOperation(value = "根据实例ID获取实例", produces = "application/json")
  public ResultWrapper<Instance> aliyunInstance(
    @ApiParam(value = "实例Id", required = true) @PathVariable(value = "instanceId") String instanceId
  ) throws GenericException, ClientException, InterruptedException, IOException {
    Instance instance = instanceService.getInstances().stream().filter(o -> Objects.equals(o.getId(), instanceId)).findFirst().orElse(new Instance());
    return new ResultWrapper<>(instance);
  }


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
  public ResultWrapper<ListResult<Instance>> aliyunReleaseInstance() throws IOException, ClientException {
    eventBus.post(new ActionEvent() {{
      setAction(ActionEventTypeEnum.ALI_RELEASE_INSTANCE);
    }});
    return ListResult.of(instanceService.getInstances());
  }


  @GetMapping(path = "/api/v1/aliyun/zones/{regionId}")
  @ApiOperation(value = "根据可用区Id获取区域列表", produces = "application/json")
  public ResultWrapper<ListResult<Zone>> describeZones(
    @ApiParam(value = "可用区Id", required = true) @PathVariable(value = "regionId") String regionId
  ) throws ClientException {
    return ListResult.of(instanceService.getZones(regionId));
  }


  @GetMapping(path = "/api/v1/aliyun/regions")
  @ApiOperation(value = "获取阿里云可用区列表", produces = "application/json")
  public ResultWrapper<ListResult<Region>> describeRegions(
  ) throws ClientException {
    return ListResult.of(instanceService.getRegions());
  }

  @GetMapping("/api/vi/qrcode/{contentText}")
  public ResponseEntity<byte[]> newGetOperateQrcode(
    @ApiParam(value = "二维码内容文本", required = true) @PathVariable("contentText") String contentText
  ) throws IOException, GenericException, WriterException {
    if (contentText == null) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(Qrcode.result(contentText));
  }


}
