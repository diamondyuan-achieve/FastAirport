package diamondyuan.api;

import diamondyuan.config.HelloMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class GreetingController {

  @Autowired
  //使用SimpMessagingTemplate 向浏览器发送消息
  private SimpMessagingTemplate template;

  @MessageMapping("/hello")
  public void greeting(HelloMessage message) throws Exception {
    Thread.sleep(1000); // simulated delay
    int i = 0;
    while (i < 10) {
      i++;
      Thread.sleep(1000); // simulated delay
      template.convertAndSend("/topic/greetings", new Message(){{
        setContent("Wee");
      }});
    }
  }

}
