package diamondyuan.event.domain;

import diamondyuan.domain.enums.ActionEventTypeEnum;
import lombok.Data;

/**
 * @author DiamondYuan
 */
@Data
public class ActionEvent {
  private ActionEventTypeEnum action;
}
