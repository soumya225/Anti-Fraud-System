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
                .mvcMatchers("/api/auth/user/**", "api/auth/access", "api/auth/role").hasRole(RoleType.ADMINISTRATOR.toString())
                .mvcMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole(RoleType.ADMINISTRATOR.toString(), RoleType.SUPPORT.toString())
                .mvcMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole(RoleType.MERCHANT.toString())
                .mvcMatchers(HttpMethod.PUT, "/api/antifraud/transaction").hasRole(RoleType.SUPPORT.toString())
                .mvcMatchers(HttpMethod.POST, "/api/antifraud/access").hasRole(RoleType.ADMINISTRATOR.toString())
                .mvcMatchers("/api/antifraud/suspicious-ip/**", "/api/antifraud/stolencard/**", "/api/antifraud/history/**").hasRole(RoleType.SUPPORT.toString())
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // no session
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}