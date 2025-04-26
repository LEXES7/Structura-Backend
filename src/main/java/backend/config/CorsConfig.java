package backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.logging.Logger;

@Configuration
public class CorsConfig {
    private static final Logger LOGGER = Logger.getLogger(CorsConfig.class.getName());

    @Bean
    public CorsFilter corsFilter() {
        LOGGER.info("Configuring CORS for http://localhost:5173");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173"); // Allow frontend origin
        config.addAllowedHeader("*"); // Allow all headers
        config.addAllowedMethod("*"); // Allow all HTTP methods
        config.setMaxAge(3600L); // Cache preflight response for 1 hour
        source.registerCorsConfiguration("/**", config);
        LOGGER.info("CORS configuration applied for all endpoints");
        return new CorsFilter(source);
    }
}