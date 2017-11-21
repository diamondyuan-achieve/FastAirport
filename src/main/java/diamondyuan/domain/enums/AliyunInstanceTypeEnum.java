package diamondyuan.domain.enums;

public enum AliyunInstanceTypeEnum {

  ECS_XN4_SMALL("ecs.xn4.small");

  private String date;

  AliyunInstanceTypeEnum(String date) {
    this.date = date;
  }

  @Override
  public String toString() {
    return date;
  }

}
