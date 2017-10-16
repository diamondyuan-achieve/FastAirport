package ams.domain;

import lombok.Data;

import java.util.List;

/**
 * @author DiamondYuan
 */
@Data
public class Instance {
  private Boolean exist;
  private String ip;
  private String id;
  private String status;
  private String regionId;
  private Shadow shadowConf;
  private List<Command> command;

}
