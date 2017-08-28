package ams.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Diamondyuan on 2017/3/15.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GenericException extends Exception {
  private String code;

  public GenericException(String code, String message) {
    super(message);
    this.setCode(code);
  }
}
