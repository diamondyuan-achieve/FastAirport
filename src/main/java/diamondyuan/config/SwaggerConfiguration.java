package diamondyuan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

  private ApiInfo initApiInfo() {
    Contact contact = new Contact("DiamondYuan", "", "");
    return new ApiInfo(
      "FastAirport",
      "FastAirport",
      "1.0.0",
      "",
      contact,
      "",
      ""
    );
  }

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
      .apiInfo(initApiInfo())
      .select()
      .apis(RequestHandlerSelectors.basePackage("diamondyuan.api"))
      .paths(PathSelectors.any())
      .build();
  }

  @Bean
  public OncePerRequestFilter swaggerCorsFilter() {
    return new OncePerRequestFilter() {
      @Override
      protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getHeader("Referer") != null && request.getHeader("Referer").contains("swagger-ui.diamondyuan.com")) {
          response.addHeader(
            "Access-Control-Allow-Origin",
            "http://swagger-ui.diamondyuan.com"
          );
          response.addHeader(
            "Access-Control-Allow-Headers",
            "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Content-Range,Range,Authorization"
          );
          response.addHeader(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, PATCH, OPTIONS"
          );
        }
        filterChain.doFilter(request, response);
      }
    };
  }
}

