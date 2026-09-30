package com.bhavesh16281.api_gateway;

import java.time.LocalDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableDiscoveryClient // Enable server-side service discovery for this application
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
						//.uri("lb://ACCOUNTS")) // used for client-side load balancing, lb://<service-name> is used to route requests to the service registered with the given name in the service registry (Eureka)
						.uri("http://accounts:8080")) // used for server side load balancing, http://<service-name>:<port> is used to route requests to the service registered with the given name in the service registry (Eureka)
				.route(r -> r.path("/mbbank/loans/**")
				.filters(f -> f.rewritePath("/mbbank/loans/(?<segment>.*)","/${segment}")
				.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
						//.uri("lb://LOANS")) //used for client-side load balancing, lb://<service-name> is used to route requests to the service registered with the given name in the service registry (Eureka)
						.uri("http://loans:8090")) // used for server side load balancing, http://<service-name>:<port> is used to route requests to the service registered with the given name in the service registry (Eureka)
				.route(r -> r.path("/mbbank/cards/**")
				.filters(f -> f.rewritePath("/mbbank/cards/(?<segment>.*)","/${segment}")
				.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
				.requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()).setKeyResolver(userKeyResolver())))
						//.uri("lb://CARDS")) //used for client-side load balancing, lb://<service-name> is used to route requests to the service registered with the given name in the service registry (Eureka)
						.uri("http://cards:9000")) // used for server side load balancing, http://<service-name>:<port> is used to route requests to the service registered with the given name in the service registry (Eureka)
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
