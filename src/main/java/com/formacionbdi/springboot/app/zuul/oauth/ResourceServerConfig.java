package com.formacionbdi.springboot.app.zuul.oauth;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@RefreshScope
@Configuration
//se necesita habilitar la configuracion del servidor de recurso con la anotacion de abajo
@EnableResourceServer
//extendemos de ResourceServerConfigurerAdapter e implementamos dos metodos
public class ResourceServerConfig extends ResourceServerConfigurerAdapter{
	
	private static final String ADMIN="ADMIN";
	private static final String CAMILIN="CAMILIN";
	private static final String USER="USER";
	
	//se necesita configurar la misma llave que en el servidor oauth
	// esta llave es la misma que se utiliza en el servicio oauth
	//para centralizar la configuracion de esta llave se hace mediante un application.properties
	//teniendo este archivo en el repositorio git pasa a ser parte de cada proyecto que este apuntando
	//al config server file, para esto se necesita agregar la dependencia de config cloud en los proyectos
	//donde queramos esta configuracion agregada
	@Value("${config.security.oauth.jwt.key}")
	private String jwtKey;
	
	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		//con este metodo configuramos el tokenStore
		resources.tokenStore(tokenStore());
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		//asignando a los endpoint el metodo permitAll, estamos dando permiso a que todos los usuarios
		//puedan consultar este endpoint, en cambio si las otras rutas estamos asignando roles,
		//lo cual limita el acceso a determinados roles de usuario logeado
		//importante, siempre nuestras rutas especificas se ingresan al principio
		// luego al final se ingresan las reglas mas genericas con permiso de administrador
		http.authorizeRequests().antMatchers("/api/security/oauth/**").permitAll()
		.antMatchers(HttpMethod.GET, "/api/productos/listar", "/api/items/listar", "/api/usuarios/usuarios").permitAll()
		.antMatchers(HttpMethod.GET, "/api/productos/ver/{id}", 
				"/api/items/ver/{id}/cantidad/{cantidad}", 
				//con has role se puede colocar varios roles separados por coma, no se ingresa
				// ROLE_ ya que esto lo hace de manera interna automaticamente
				"/api/usuarios/usuarios/{id}").hasAnyRole(ADMIN, USER)
		
		//hacer la linea de codigo de abajo es lo mismo que hacer las 3 lineas con los metodos http
//		.antMatchers("/api/productos/**", "/api/items/**", "/api/usuarios/**").hasRole("ADMIN")
		
		.antMatchers(HttpMethod.POST, "/api/productos/crear", "/api/items/crear", "/api/usuarios/usuarios/**").hasAnyRole(ADMIN,CAMILIN)
		.antMatchers(HttpMethod.PUT, "/api/productos/editar/{id}", "/api/items/editar/{id}", "/api/usuarios/usuarios/{id}").hasAnyRole(ADMIN,CAMILIN)
		.antMatchers(HttpMethod.DELETE, "/api/productos/eliminar/{id}", "/api/items/eliminar/{id}", "/api/usuarios/usuarios/{id}").hasAnyRole(ADMIN,CAMILIN)

		//con anyRequest().authenticated() cualquier ruta que no se haya configurado
		//le solicitara que el usuario este autenticado		
		.anyRequest().authenticated()
		//configurar Cross Origin Requests para que una aplicacion externa como angular, pueda acceder a nuestros
		//endpoints
		//agregamos el metodo cors().configurationSource y creamos el metodo corsConfigurationSource
		//con el fin de poder intercambiar recursos con otros dominios como angular, jquery, react
		.and().cors().configurationSource(corsConfigurationSource());
	}
	
	//dejar el metodo como publico y anotarlo como bean
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		//crear el objeto corsConfiguration
		CorsConfiguration corsConfig = new CorsConfiguration();
		//con el metodo addAllowedOrigin agregamos las ip que pueden consultar nuestros endpoint
		//al ingresar * le damos acceso a todas las aplicaciones
		//importante, para agregar mas dominions se puede hacer llamando nuevamente el metodo addAllowedOrigin
		//o usar el metodo .setAllowedOrigins que recibe una lista de dominios
		corsConfig.addAllowedOrigin("*");
//		corsConfig.setAllowedOrigins(Arrays.asList("*",""));
		//con el metodo allowedMethod le damos acceso a que consulte los siguientes metodos http
		//importante agregar el metodo OPTIONS ya que por debajo lo utilia oauth2
		corsConfig.setAllowedMethods(Arrays.asList("POST","PUT","DELETE","GET","OPTIONS"));
		corsConfig.setAllowCredentials(true);
		//permitir las cabeceras
		corsConfig.setAllowedHeaders(Arrays.asList("Authorization","Content-Type"));
		
		//pasar la configuracion del corsConfig a nuestras rutas url
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);
				return source;
	}
	
	//con este bean estamos configurando un filtro para que la configuracion de cors se aplique a nivel global
	// y no solo a spring security
	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter(){
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<CorsFilter>
		(new CorsFilter(corsConfigurationSource()));
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return bean;
	}

	@Bean
	public JwtTokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter tokenConverter = new JwtAccessTokenConverter();
		tokenConverter.setSigningKey(jwtKey);
		return tokenConverter;
	}

}
