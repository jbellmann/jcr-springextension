package org.springframework.extensions.jcr.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.jcr.observation.Event;

import org.springframework.stereotype.Component;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface EventListenerDefinition {

    int eventTypes() default Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED;

    String absPath() default "/";

    boolean deep() default true;

    String[] uuids() default {};

    String[] nodeTypeNames() default {};

    boolean noLocal() default false;

}
