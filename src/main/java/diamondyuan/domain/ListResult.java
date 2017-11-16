package diamondyuan.domain;

import lombok.Data;

import java.util.List;

@Data
public class ListResult<T> {
  private List<T> list;
  public static <T> ResultWrapper<ListResult<T>> of(List<T> list) {
    return new ResultWrapper<>(1000000, "ok", new ListResult<T>() {{
      setList(list);
    }});
  }

}
