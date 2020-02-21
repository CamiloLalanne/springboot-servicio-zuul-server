package com.formacionbdi.springboot.app.zuul.swager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
//con esta anotacion habilitamos swagger para que documente nuestra aplicacion
@EnableSwagger2
public class SwaggerConfig {
//http://localhost:8005/v2/api-docs
	
	@Bean
  public Docket api() { 
      return new Docket(DocumentationType.SWAGGER_2)  
        .select()
        //con any hacemos una documentacion sin filtro, osea es a nivel general, de igual manera
        //podemos limitar la documentacion a solo servicios del tipo RestService u otros
        .apis(RequestHandlerSelectors.any())
        .build().pathMapping("/").apiInfo(apiInfo());                                           
  }
	
	//con este metodo pasamos informacion adicional al servi
	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("Zuul-swagger").description("api zuul swagger").version("1.0.0")
				.build();
	}
	
//	@Bean
//  public Docket api() { 
//      return new Docket(DocumentationType.SWAGGER_2)  
//        .select()                                  
//        .apis(RequestHandlerSelectors.any())              
//        .paths(PathSelectors.any())                          
//        .build();                                           
//  }
	
	
}