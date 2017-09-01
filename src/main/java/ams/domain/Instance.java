package ams.domain;

import lombok.Data;

@Data
public class Instance {
  private boolean exist;
  private String ip;
  private String id;
  private String status;
  private String regionId;
}
