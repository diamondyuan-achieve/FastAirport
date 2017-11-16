package diamondyuan.event;

import com.aliyuncs.exceptions.ClientException;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.jcraft.jsch.JSchException;
import diamondyuan.domain.Config;
import diamondyuan.domain.enums.ActionEventTypeEnum;
import diamondyuan.domain.enums.ConfigStatusEnum;
import diamondyuan.event.domain.ActionEvent;
import diamondyuan.services.ConfigService;
import diamondyuan.services.InstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * @author DiamondYuan
 */
@Component
public class AliyunEventListener {

  private final InstanceService instanceService;
  private final ConfigService configService;


  @Autowired
  public AliyunEventListener(EventBus configEventBus, ConfigService configService, InstanceService instanceService) {
    configEventBus.register(this);
    this.configService = configService;
    this.instanceService = instanceService;
  }


  @Subscribe
  public void AliyunInitEventSubscriber(ActionEvent e) throws JSchException, ClientException, IOException, InterruptedException {
    if (e == null || !e.getAction().equals(ActionEventTypeEnum.ALI_INIT)) {
      return;
    }
    Config config = configService.loadConfig();
    if (!config.getConfigStatus().equals(ConfigStatusEnum.EMPTY)) {
      return;
    }
    config.setConfigStatus(ConfigStatusEnum.PENDING);
    configService.saveConfig(config);
    instanceService.serviceInit();
  }


  @Subscribe
  public void createInstanceEventSubscriber(ActionEvent e) throws JSchException, ClientException, IOException, InterruptedException {
    if (e == null || !e.getAction().equals(ActionEventTypeEnum.ALI_CREATE_INSTANCE)) {
      return;
    }
    instanceService.createInstance();
  }

  @Subscribe
  public void releaseInstanceEventSubscriber(ActionEvent e) throws JSchException, ClientException, IOException, InterruptedException {
    if (e == null || !e.getAction().equals(ActionEventTypeEnum.ALI_RELEASE_INSTANCE)) {
      return;
    }
    instanceService.releaseInstance();
  }
}
