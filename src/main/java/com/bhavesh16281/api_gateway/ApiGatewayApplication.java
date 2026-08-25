package com.bhavesh16281.api_gateway;

import java.time.LocalDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(r -> r.path("/mbbank/accounts/**")
				.filters(f -> f.rewritePath("mbbank/accounts/(?<segment>.*)", "/${segment}")
				.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
						.uri("lb://ACCOUNTS"))
				.route(r -> r.path("/mbbank/loans/**")
				.filters(f -> f.rewritePath("mbbank/loans/(?<segment>.*)", "/${segment}")
				.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
						.uri("lb://LOANS"))
				.route(r -> r.path("/mbbank/cards/**").filters(f -> f.rewritePath("mbbank/cards/(?<segment>.*)", "/${segment}")
				.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
						.uri("lb://CARDS"))
				.build();
	}

}
