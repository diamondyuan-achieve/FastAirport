package diamondyuan.utils;

import lombok.Data;
import java.util.Locale;

@Data
public class SSRConfig {
  private String host;
  private int remotePort;
  private String method;
  private String obfs;
  private String obfs_param;
  private String protocol;
  private String protocol_param;
  private String password;
  private String remarks;


  @Override
  public String toString() {
    String en_password = UrlSafeBase64.encodeToString(password);
    String en_obfs_param = UrlSafeBase64.encodeToString(obfs_param);
    String en_protocol_param = UrlSafeBase64.encodeToString(protocol_param);
    String en_remarks = UrlSafeBase64.encodeToString(remarks);
    return String.format("ssr://%s", UrlSafeBase64.encodeToString(String.format(Locale.ENGLISH,
      "%s:%d:%s:%s:%s:%s/?obfsparam=%s&protoparam=%s&remarks=%s",
      host, remotePort, protocol, method, obfs,
      en_password,
      en_obfs_param,
      en_protocol_param,
      en_remarks)));
  }



}
