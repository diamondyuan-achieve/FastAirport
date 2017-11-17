package diamondyuan.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {
  @RequestMapping("/instance")
  public String page(){
    return "instance";
  }

  @RequestMapping("/config")
  public String config(){
    return "config";
  }
}
