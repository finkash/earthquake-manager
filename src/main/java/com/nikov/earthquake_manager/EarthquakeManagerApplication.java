package com.nikov.earthquake_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication: This is a convenience annotation that adds all of the following:
// @Configuration: This annotation indicates that the class can be used by the Spring IoC container as a source of bean definitions.
// @EnableAutoConfiguration: This annotation tells Spring Boot to start adding beans based on classpath settings, other beans, and various property settings. 
// For example, if spring webmvc is on the classpath, this annotation flags the application as a web application and activates key behaviors such as setting up a DispatcherServlet.
@SpringBootApplication
public class EarthquakeManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EarthquakeManagerApplication.class, args);
	}

}
