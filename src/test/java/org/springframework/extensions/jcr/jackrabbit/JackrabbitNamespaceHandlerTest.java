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
package org.springframework.extensions.jcr.jackrabbit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Costin Leau
 * @author Sergio Bossa
 * @author Salvatore Incandela
 */
public class JackrabbitNamespaceHandlerTest {

    private DefaultListableBeanFactory beanFactory;
    private XmlBeanDefinitionReader beanDefinitionReader;

    @Before
    public void setUp() throws Exception {
        beanFactory = new DefaultListableBeanFactory();
        beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        beanDefinitionReader.loadBeanDefinitions(new ClassPathResource("jackrabbitNamespaceHandlerTest.xml"));
    }

    private void assertPropertyValue(BeanDefinition beanDefinition, String propertyName, Object expectedValue) {
        assertEquals("Property [" + propertyName + "] incorrect.", expectedValue, beanDefinition.getPropertyValues()
                .getPropertyValue(propertyName).getValue());
    }

    @Test
    public void testMinimalDefinition() throws Exception {
        BeanDefinition beanDefinition = this.beanFactory.getBeanDefinition("minimal");
        assertSame(RepositoryFactoryBean.class.getName(), beanDefinition.getBeanClassName());
        assertPropertyValue(beanDefinition, "configuration", "classpath:config.xml");
    }

    @Test
    public void testExtendedDefinition() throws Exception {
        BeanDefinition beanDefinition = this.beanFactory.getBeanDefinition("extended");
        assertSame(RepositoryFactoryBean.class.getName(), beanDefinition.getBeanClassName());
        assertPropertyValue(beanDefinition, "configuration", "file:config.xml");
        assertPropertyValue(beanDefinition, "homeDir", "file:///homeDir");
    }

    @Test
    public void testFullDefinition() throws Exception {
        BeanDefinition beanDefinition = this.beanFactory.getBeanDefinition("full");
        assertSame(RepositoryFactoryBean.class.getName(), beanDefinition.getBeanClassName());
        assertPropertyValue(beanDefinition, "homeDir", "file:///homeDir");
        assertPropertyValue(beanDefinition, "repositoryConfig", "repoCfg");
    }

    @Test
    public void testTransactionManager() throws Exception {
        BeanDefinition beanDefinition = this.beanFactory.getBeanDefinition("transactionManager");
        assertSame(LocalTransactionManager.class.getName(), (beanDefinition.getBeanClassName()));
        assertPropertyValue(beanDefinition, "sessionFactory", "jcrSessionFactory");
    }

}
