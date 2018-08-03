package com.example.scim;

import com.example.scim.scimple.ScimGroupProvider;
import com.example.scim.scimple.ScimUserProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@SpringBootApplication
public class SpringInitializer extends SpringBootServletInitializer implements ApplicationContextAware {

    // Need to save the Spring Context to a static var so we can use it in the SpringToCdiBridge below.
    private static ApplicationContext APPLICATION_CONTEXT = null;

    public void setApplicationContext(ApplicationContext applicationContext) {
        APPLICATION_CONTEXT = applicationContext;
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SpringInitializer.class);
    }

    @Bean
    public ScimGroupProvider inMemoryGroupService() {
        return new ScimGroupProvider();
    }

    @Bean
    public ScimUserProvider inMemoryUserService() {
        return new ScimUserProvider();
    }

    /**
     * The security configuration is defined here, but we also need to configure the authentication options in
     * {@link org.apache.directory.scim.server.configuration.ServerConfiguration ServerConfiguration} via
     * {@link com.example.scim.scimple.ScimRegistryConfigurator ScimRegistryConfigurator}
     * @return Spring security config
     */
    @Bean
    public WebSecurityConfigurerAdapter webSecurityConfigurerAdapter() {
        return new WebSecurityConfigurerAdapter() {
            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http
                    .csrf().disable()
                    .sessionManagement().disable()
                    .authorizeRequests()
                    .anyRequest()
                        .authenticated().and()
                    .httpBasic();
            }
        };
    }

    /**
     * Bridge the needed InMemoryGroupService and InMemoryUserService from Spring's context into the CDI context.
     */
    @ApplicationScoped
    public static class SpringToCdiBridge {

        @Named
        @Produces
        public ScimGroupProvider inMemoryGroupService() {
            return APPLICATION_CONTEXT.getBean(ScimGroupProvider.class);
        }

        @Named
        @Produces
        public ScimUserProvider inMemoryUserService() {
            return APPLICATION_CONTEXT.getBean(ScimUserProvider.class);
        }
    }
}
