package diamondyuan.api;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import diamondyuan.domain.Config;
import diamondyuan.domain.Instance;
import diamondyuan.domain.WebSession;
import diamondyuan.services.ConfigService;
import diamondyuan.services.DiamondUtils;
import diamondyuan.services.InstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
public class GreetingController {

  @Autowired
  //使用SimpMessagingTemplate 向浏览器发送消息
  private SimpMessagingTemplate template;
  @Autowired
  private DiamondUtils diamondUtils;

  @Autowired
  WebSession webSession;
  @Autowired
  InstanceService instanceService;
  @Autowired
  ConfigService configService;


  @MessageMapping("/{id}/command")
  public void greeting(@DestinationVariable("id") String id, Message message) throws Exception {
    Config config = configService.loadConfig();
    Instance instance = instanceService.getInstances().stream().filter(o -> Objects.equals(o.getId(), id)).findFirst().orElse(new Instance());
    if (!Objects.equals(webSession.getInstanceId(), id) || !webSession.getSession().isConnected()) {
      webSession.setSession(DiamondUtils.openSession("root", instance.getIp(), 22, config.getKeyPairPath()));
      webSession.setInstanceId(id);
    }
    diamondUtils.execCommand(id, webSession.getSession(), message.getContent());

  }

}
