package backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.logging.Logger;

@Configuration
public class CorsConfig {
    private static final Logger LOGGER = Logger.getLogger(CorsConfig.class.getName());

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        LOGGER.info("Configuring CORS for http://localhost:5173");
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("Authorization", "Content-Type", "*")
                        .allowCredentials(true)
                        .maxAge(3600);
                LOGGER.info("CORS mapping added for /api/**");
            }
        };
    }
}