package ams.domain;

import lombok.Data;

@Data
public class Shadow {
  private int port;
  private String method;
  private String password;
  private String base;
}
