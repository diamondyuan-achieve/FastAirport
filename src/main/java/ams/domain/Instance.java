package ams.domain;

import lombok.Data;

@Data
public class Instance {
  private String ip;
  private String id;
  private String name;
  private String status;
  private String regionId;
}
