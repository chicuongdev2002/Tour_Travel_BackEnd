package vn.edu.iuh.fit.config;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;import org.springframework.data.web.config.EnableSpringDataWebSupport;import org.springframework.web.client.RestTemplate;import org.springframework.web.servlet.config.annotation.CorsRegistry;import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;@Configuration@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)public class WebConfig implements WebMvcConfigurer {    @Bean    public RestTemplate restTemplate() {        return new RestTemplate();    }    @Override    public void addCorsMappings(CorsRegistry registry) {        registry.addMapping("/**")                .allowedOrigins(                        "https://two2-webtour.onrender.com",                        "http://localhost:5173",                        "https://a9ba-1-54-116-255.ngrok-free.app"                )                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")                .allowedHeaders("*")                .allowCredentials(true)                .maxAge(3600);    }    //@Bean    //public <T> PagedResourcesAssembler<T> pagedResourcesAssembler() {    //    return new PagedResourcesAssembler<>(null, null);    //}}