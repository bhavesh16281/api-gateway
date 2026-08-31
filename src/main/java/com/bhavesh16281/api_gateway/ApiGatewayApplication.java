package com.bhavesh16281.api_gateway;

import java.time.LocalDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import reactor.core.publisher.Mono;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(r -> r.path("/mbbank/accounts/**")
				.filters(f -> f.rewritePath("/mbbank/accounts/(?<segment>.*)","/${segment}")
				.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
				.circuitBreaker(c -> c.setName("accountsCircuitBreaker")
				.setFallbackUri("forward:/contactSupport")))
						.uri("lb://ACCOUNTS"))

				.route(r -> r.path("/mbbank/loans/**")
				.filters(f -> f.rewritePath("/mbbank/loans/(?<segment>.*)","/${segment}")
				.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
						.uri("lb://LOANS"))
						
				.route(r -> r.path("/mbbank/cards/**")
				.filters(f -> f.rewritePath("/mbbank/cards/(?<segment>.*)","/${segment}")
				.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
				.requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()).setKeyResolver(userKeyResolver())))
						.uri("lb://CARDS"))
				.build();
	}

	@Bean
	public RedisRateLimiter redisRateLimiter(){
		return new RedisRateLimiter(1, 1, 1);
	}

	@Bean
	KeyResolver userKeyResolver(){
		return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("user"))
				.defaultIfEmpty("anonymous");
	}
}
