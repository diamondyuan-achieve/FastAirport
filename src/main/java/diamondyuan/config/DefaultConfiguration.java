package diamondyuan.config;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import diamondyuan.domain.WebSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * @author DiamondYuan
 */
@Configuration
@EnableAsync
public class DefaultConfiguration {


  @Bean
  IAcsClient iAcsClient(
    @Value("${accessKey}") String accessKey,
    @Value("${accessSecret}") String accessSecret,
    @Value("${regionId}") String regionId
  ) {
    return new DefaultAcsClient(DefaultProfile.getProfile(regionId, accessKey, accessSecret));
  }


  @Bean
  public EventBus eventBus() {
    return new AsyncEventBus(Executors.newFixedThreadPool(6));
  }


  @Bean
  public WebSession websession() {
    return new WebSession();
  }


}
