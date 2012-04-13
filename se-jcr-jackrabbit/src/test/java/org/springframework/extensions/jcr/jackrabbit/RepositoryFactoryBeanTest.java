/**
 * Copyright 2009-2012 the original author or authors
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

import javax.jcr.Repository;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

public class RepositoryFactoryBeanTest {

    RepositoryFactoryBean factory;

    @Before
    public void setUp() throws Exception {
        factory = new RepositoryFactoryBean();
    }

    /*
     * Test method for
     * 'org.springframework.extensions.jcr.jeceira.RepositoryFactoryBean.resolveConfigurationResource()'
     */
    @Test
    public void testResolveConfigurationResource() throws Exception {

        factory.resolveConfigurationResource();
        DefaultResourceLoader loader = new DefaultResourceLoader();
        Resource res = loader.getResource("/repository.xml");
        assertEquals(res, factory.getConfiguration());
        assertEquals(".", factory.getHomeDir().getFilename());

    }

    /*
     * Test method for
     * 'org.springframework.extensions.jcr.jeceira.RepositoryFactoryBean.createRepository()'
     */
    @Test
    public void testCreateRepository() throws Exception {
        factory.afterPropertiesSet();
        Repository rep = (Repository) factory.getObject();
        assertEquals(rep.getDescriptor("jcr.repository.name"), "Jackrabbit");

        assertEquals(true, factory.getObject() instanceof Repository);
        assertEquals(true, factory.isSingleton());
        assertEquals(Repository.class, factory.getObjectType());
        factory.destroy();

    }
}
