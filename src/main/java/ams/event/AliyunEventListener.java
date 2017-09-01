package ams.event;

import ams.domain.Config;
import ams.domain.Instance;
import ams.event.domain.ActionEvent;
import ams.services.InstanceService;
import ams.services.impl.AliyunInstanceService;
import com.aliyuncs.exceptions.ClientException;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AliyunEventListener {

  private EventBus eventBus;
  private InstanceService instanceService;
  private Instance instance;
  private Config config;


  @Autowired
  public void setInstance(Instance instance) {
    this.instance = instance;
  }

  @Autowired
  public void setConfig(Config config) {
    this.config = config;
  }

  @Autowired
  public void setService(AliyunInstanceService instanceService) {
    this.instanceService = instanceService;
  }

  @Autowired
  public AliyunEventListener(EventBus configEventBus) {
    this.eventBus = configEventBus;
    eventBus.register(this); // register this instance with the event bus so it receives any events
  }


  @Subscribe
  public void messageSubscriber(ActionEvent e) throws ClientException, IOException, InterruptedException {
    switch (e.getAction()) {
      case ALI_INIT: {
        if (config.isFirstLoad()) {
          config.setFirstLoad(false);
          instanceService.serviceInit();
        }
        break;
      }
      case ALI_CREATE_INSTANCE: {
        if (!instance.isExist()) {
          instanceService.createInstance();
        }
        break;
      }
      case ALI_RELEASE_INSTANCE: {
        if (instance.isExist()) {
          instanceService.releaseInstance();
        }
        break;
      }
      case ALI_INSTANCE_REFRESH: {
        instanceService.refreshInstance();
        break;
      }
      default:
        break;
    }

  }


}
