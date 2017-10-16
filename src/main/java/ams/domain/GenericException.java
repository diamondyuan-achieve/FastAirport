package ams.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 *
 * @author DiamondYuan
 *
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
