package es.microservices.tests.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@EnableDiscoveryClient
@Profile("!test")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}

}
