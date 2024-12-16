package vn.edu.iuh.fit.config;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;import org.springframework.security.config.annotation.web.builders.HttpSecurity;import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;import org.springframework.security.config.http.SessionCreationPolicy;import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;import org.springframework.security.crypto.password.PasswordEncoder;import org.springframework.security.web.SecurityFilterChain;import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;import org.springframework.web.cors.CorsConfiguration;import org.springframework.web.cors.CorsConfigurationSource;import org.springframework.web.cors.UrlBasedCorsConfigurationSource;import org.springframework.web.filter.CorsFilter;import org.springframework.web.servlet.config.annotation.CorsRegistry;import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;import vn.edu.iuh.fit.service.token.JwtAuthenticationFilter;import java.util.Arrays;import java.util.Collections;import java.util.List;@Configuration@EnableWebSecuritypublic class SecurityConfig implements WebMvcConfigurer {    @Autowired    private JwtAuthenticationFilter jwtAuthFilter;    // Mã hóa mật khẩu    @Bean    public PasswordEncoder passwordEncoder() {        return new BCryptPasswordEncoder();    }    // Cấu hình Security Filter Chain    @Bean    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {        http                // Bật CORS và tắt CSRF                .cors(cors -> cors.configurationSource(corsConfigurationSource()))                .csrf(csrf -> csrf.disable())                // Cấu hình phân quyền                .authorizeHttpRequests(authz -> authz                        .requestMatchers(                                "/api/**",                                "/ws/**",                                "/swagger-ui/**",                                "/v3/api-docs/**",                                "/swagger-resources/**",                                "/webjars/**"                        ).permitAll()                        .anyRequest().authenticated()                )                // Quản lý phiên                .sessionManagement(session -> session                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)                )                // Thêm JWT Filter                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);        return http.build();    }    // Cấu hình CORS    @Bean    public CorsConfigurationSource corsConfigurationSource() {        CorsConfiguration configuration = new CorsConfiguration();        // Danh sách các origin được phép        configuration.setAllowedOrigins(Arrays.asList(                "https://two2-webtour.onrender.com",                "http://localhost:10000",                "https://tourtravelbackend-production.up.railway.app",                "http://localhost:3000",                "https://a9ba-1-54-116-255.ngrok-free.app"        ));        // Các phương thức HTTP được phép        configuration.setAllowedMethods(Arrays.asList(                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"        ));        // Các header được phép        configuration.setAllowedHeaders(Arrays.asList(                "Authorization",                "Cache-Control",                "Content-Type",                "Accept",                "X-Requested-With",                "Access-Control-Allow-Origin",                "Access-Control-Allow-Headers",                "Origin"        ));        // Cho phép credentials        configuration.setAllowCredentials(true);        // Các header được expose        configuration.setExposedHeaders(Arrays.asList(                "Access-Control-Allow-Origin",                "Access-Control-Allow-Credentials",                "Authorization",                "Set-Cookie"        ));        // Thời gian cache CORS        configuration.setMaxAge(3600L);        // Áp dụng cấu hình cho tất cả các đường dẫn        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();        source.registerCorsConfiguration("/**", configuration);        return source;    }    // Cấu hình CORS cho WebMvc    @Override    public void addCorsMappings(CorsRegistry registry) {        registry.addMapping("/**")                .allowedOrigins(                        "https://two2-webtour.onrender.com",                        "http://localhost:10000",                        "https://tourtravelbackend-production.up.railway.app",                        "http://localhost:3000",                        "https://a9ba-1-54-116-255.ngrok-free.app"                )                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")                .allowedHeaders("*")                .allowCredentials(true);    }    // Thêm CORS Filter (tuỳ chọn)    @Bean    public CorsFilter corsFilter() {        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();        CorsConfiguration config = new CorsConfiguration();        config.setAllowCredentials(true);        config.setAllowedOrigins(Arrays.asList(                "https://two2-webtour.onrender.com",                "http://localhost:10000",                "https://tourtravelbackend-production.up.railway.app",                "http://localhost:3000",                "https://a9ba-1-54-116-255.ngrok-free.app"        ));        config.setAllowedHeaders(Arrays.asList("*"));        config.setAllowedMethods(Arrays.asList(                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"        ));        source.registerCorsConfiguration("/**", config);        return new CorsFilter(source);    }}