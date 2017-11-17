package diamondyuan.api;

import diamondyuan.config.HelloMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class GreetingController {

  @Autowired
  //使用SimpMessagingTemplate 向浏览器发送消息
  private SimpMessagingTemplate template;

  @MessageMapping("/{id}/command")
  public void greeting(@DestinationVariable("id") String id,Message message) throws Exception {
    System.out.println(id+" "+message);
  }

}
