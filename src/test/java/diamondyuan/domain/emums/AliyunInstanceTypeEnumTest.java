package diamondyuan.domain.emums;

import diamondyuan.domain.enums.AliyunInstanceTypeEnum;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;


public class AliyunInstanceTypeEnumTest {

  @Test
  public void getNameAndStringTest() throws Exception {

    Assert.assertThat(AliyunInstanceTypeEnum.ECS_XN4_SMALL.name().equals("ECS_XN4_SMALL"), is(true));
    Assert.assertThat(AliyunInstanceTypeEnum.ECS_XN4_SMALL.toString().equals("ECS_XN4_SMALL"), is(false));
    Assert.assertThat(AliyunInstanceTypeEnum.ECS_XN4_SMALL.toString().equals("ecs.xn4.small"), is(true));

  }


}
