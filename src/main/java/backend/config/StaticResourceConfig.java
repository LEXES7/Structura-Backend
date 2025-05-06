package backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /api/uploads/** URLs to the physical folder where files are stored
        registry
                .addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:src/main/resources/uploads/");
    }
}