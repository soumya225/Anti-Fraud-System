package antifraud;

import antifraud.models.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigurerImpl extends WebSecurityConfigurerAdapter {

    @Autowired
    RestAuthenticationEntryPoint restAuthenticationEntryPoint;


    public void configure(HttpSecurity http) throws Exception {
        http.httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint) // Handles auth error
                .and()
                .csrf().disable().headers().frameOptions().disable() // for Postman, the H2 console
                .and()
                .authorizeRequests() // manage access
                .mvcMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                .mvcMatchers("/actuator/shutdown").permitAll() // needs to run test
                .mvcMatchers("/api/auth/user/**", "api/auth/access", "api/auth/role").hasRole(RoleType.ROLE_ADMINISTRATOR.getRoleName())
                .mvcMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole(RoleType.ROLE_ADMINISTRATOR.getRoleName(), RoleType.ROLE_SUPPORT.getRoleName())
                .mvcMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole(RoleType.ROLE_MERCHANT.getRoleName())
                .mvcMatchers(HttpMethod.POST, "/api/antifraud/access").hasRole(RoleType.ROLE_ADMINISTRATOR.getRoleName())
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // no session
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}