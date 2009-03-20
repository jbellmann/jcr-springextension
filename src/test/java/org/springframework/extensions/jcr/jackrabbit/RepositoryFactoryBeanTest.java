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

import org.springframework.extensions.jcr.jackrabbit.RepositoryFactoryBean;
import javax.jcr.Repository;

import junit.framework.TestCase;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

public class RepositoryFactoryBeanTest extends TestCase {

	RepositoryFactoryBean factory;

	protected void setUp() throws Exception {
		super.setUp();
		factory = new RepositoryFactoryBean();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for
	 * 'org.springframework.extensions.jcr.jeceira.RepositoryFactoryBean.resolveConfigurationResource()'
	 */
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
