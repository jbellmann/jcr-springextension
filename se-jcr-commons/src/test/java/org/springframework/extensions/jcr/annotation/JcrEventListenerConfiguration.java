package org.springframework.extensions.jcr.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;

@Configuration
public class JcrEventListenerConfiguration {

    @Bean
    public BeanPostProcessor jcrEventListenerPostprocessor() {
        return new JcrEventListenerBeanPostProcessor();
    }

    static class JcrEventListenerBeanPostProcessor implements BeanPostProcessor {

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            // nothing to do here
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            EventListenerDefinition annotation = AnnotationUtils.findAnnotation(bean.getClass(), EventListenerDefinition.class);
            if (annotation != null) {
                System.out.println("Found annotation on bean " + beanName);
            }
            return bean;
        }

    }

}
