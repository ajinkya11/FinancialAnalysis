package com.financialanalysis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import picocli.CommandLine;

/**
 * Application configuration
 */
@Configuration
public class AppConfig {

    /**
     * PicocliSpringFactory for dependency injection in Picocli commands
     */
    @Bean
    public CommandLine.IFactory picocliFactory(org.springframework.context.ApplicationContext context) {
        return new PicocliSpringFactory(context);
    }

    /**
     * Factory that retrieves bean instances from Spring ApplicationContext
     */
    private static class PicocliSpringFactory implements CommandLine.IFactory {
        private final org.springframework.context.ApplicationContext applicationContext;

        public PicocliSpringFactory(org.springframework.context.ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        public <K> K create(Class<K> clazz) throws Exception {
            try {
                return applicationContext.getBean(clazz);
            } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
                return CommandLine.defaultFactory().create(clazz);
            }
        }
    }
}
