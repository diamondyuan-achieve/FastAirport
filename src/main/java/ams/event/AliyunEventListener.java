package ams.event;

import ams.domain.Config;
import ams.domain.Instance;
import ams.event.domain.ActionEvent;
import ams.services.InstanceService;
import ams.services.impl.AliyunInstanceServiceImpl;
import com.aliyuncs.exceptions.ClientException;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.jcraft.jsch.JSchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * @author DiamondYuan
 */
@Component
public class AliyunEventListener {

  private EventBus eventBus;
  private InstanceService instanceService;
  private Instance instance;
  private Config config;


  @Autowired
  public AliyunEventListener(EventBus configEventBus) {
    this.eventBus = configEventBus;
    /**
     * register this instance with the event bus so it receives any events
     */
    eventBus.register(this);
  }

  @Autowired
  public void setInstance(Instance instance) {
    this.instance = instance;
  }

  @Autowired
  public void setConfig(Config config) {
    this.config = config;
  }

  @Autowired
  public void setService(AliyunInstanceServiceImpl instanceService) {
    this.instanceService = instanceService;
  }

  @Subscribe
  public void messageSubscriber(ActionEvent e) throws JSchException, ClientException, IOException, InterruptedException {
    switch (e.getAction()) {
      case ALI_INIT: {
        if (config.isFirstLoad()) {
          config.setFirstLoad(false);
          instanceService.serviceInit();
        }
        break;
      }
      case ALI_CREATE_INSTANCE: {
        if (!instance.getExist()) {
          instanceService.createInstance();
        }
        instanceService.refreshInstance();

        break;
      }
      case ALI_RELEASE_INSTANCE: {
        if (instance == null || instance.getExist()) {
          instanceService.releaseInstance();
        }
        instanceService.refreshInstance();
        break;
      }
      case ALI_INSTANCE_REFRESH: {
        instanceService.refreshInstance();
        break;
      }
      case ALI_INSTANCE_INIT: {
        if (instance.getCommand() == null || instance.getCommand().size() < 1) {
          instanceService.instanceInit();
        }
        break;
      }
      default:
        break;
    }

  }


}
