package diamondyuan.domain;

import com.jcraft.jsch.Session;
import lombok.Data;

@Data
public class WebSession {
  private Session session;
  private String instanceId;
}
