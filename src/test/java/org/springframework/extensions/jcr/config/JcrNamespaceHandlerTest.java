/**
 * Copyright 2009 the original author or authors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.springframework.extensions.jcr.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.extensions.jcr.EventListenerDefinition;
import org.springframework.extensions.jcr.JcrSessionFactory;
import org.springframework.util.ObjectUtils;

public class JcrNamespaceHandlerTest {

    private DefaultListableBeanFactory beanFactory;
    private XmlBeanDefinitionReader beanDefinitionReader;

    @Before
    public void setUp() throws Exception {
        beanFactory = new DefaultListableBeanFactory();
        beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        beanDefinitionReader.loadBeanDefinitions(new ClassPathResource("jcrNamespaceHandlerTest.xml"));
    }

    private void assertPropertyValue(BeanDefinition beanDefinition, String propertyName, Object expectedValue) {
        assertEquals("Property [" + propertyName + "] incorrect.", expectedValue,
                getPropertyValue(beanDefinition, propertyName));
    }

    private Object getPropertyValue(BeanDefinition beanDefinition, String propertyName) {
        return beanDefinition.getPropertyValues().getPropertyValue(propertyName).getValue();
    }

    @Test
    public void testEventListenerDefinition() throws Exception {
        BeanDefinition beanDefinition = this.beanFactory.getBeanDefinition("eventListenerFull");
        assertSame(EventListenerDefinition.class.getName(), beanDefinition.getBeanClassName());
        assertPropertyValue(beanDefinition, "absPath", "/somePath");
        assertPropertyValue(beanDefinition, "deep", "true");
        assertPropertyValue(beanDefinition, "noLocal", "false");
        assertPropertyValue(beanDefinition, "eventTypes", 17);
        assertTrue(ObjectUtils.nullSafeEquals(new String[] { "123" }, getPropertyValue(beanDefinition, "uuid")));
        assertTrue(ObjectUtils.nullSafeEquals(new String[] { "foo", "bar" },
                getPropertyValue(beanDefinition, "nodeTypeName")));
    }

    @Test
    public void testSessionFactory() throws Exception {
        BeanDefinition beanDefinition = this.beanFactory.getBeanDefinition("sessionFactory");
        assertSame(JcrSessionFactory.class.getName(), beanDefinition.getBeanClassName());

    }
}
