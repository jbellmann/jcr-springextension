package org.springframework.extensions.jcr.annotation;

import java.util.Map;

import javax.jcr.observation.EventListener;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.extensions.jcr.annotation.AnnotatedEventListenerTest.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {TestConfiguration.class})
public class AnnotatedEventListenerTest {

    @Autowired
    private AnnotatedEventListener annotatedEventListener;

    @Autowired
    private AbstractApplicationContext applicationContext;

    /**
     * JcrSessionFactory should be ApplicationContextAware and then it should be possible
     * to get all EventListener from ApplicationContext and register them into the created
     * session.
     */
    @Test
    public void testAnnotatedEventListnerAutowired() {
        Assert.assertNotNull(annotatedEventListener);
        Assert.assertNotNull(applicationContext);
        Map<String, ? extends EventListener> eventListenerBeans = applicationContext.getBeansOfType(EventListener.class);
        Assert.assertFalse(eventListenerBeans.isEmpty());
        Assert.assertTrue(eventListenerBeans.size() == 1);
        Assert.assertEquals(annotatedEventListener, eventListenerBeans.get("annotatedEventListener"));

        EventListenerDefinition eventlistenerDefinition = AnnotationUtils.findAnnotation(eventListenerBeans.get("annotatedEventListener").getClass(),
                EventListenerDefinition.class);
        Assert.assertNotNull(eventlistenerDefinition);
        Assert.assertTrue(eventlistenerDefinition.deep());
        Assert.assertArrayEquals(new String[] {"test"}, eventlistenerDefinition.uuids());

    }

    @Configuration
    @EnableJcrEventListener
    @ComponentScan(basePackages = "org.springframework.extensions.jcr")
    static class TestConfiguration {
    }

}
