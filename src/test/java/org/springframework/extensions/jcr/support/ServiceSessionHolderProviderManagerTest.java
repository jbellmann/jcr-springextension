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
package org.springframework.extensions.jcr.support;

import org.springframework.extensions.jcr.support.ServiceSessionHolderProviderManager;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.extensions.jcr.jackrabbit.support.JackRabbitSessionHolderProvider;

/**
 * 
 * @author Costin Leau
 * @author Sergio Bossa
 * @author Salvatore Incandela
 * 
 */
public class ServiceSessionHolderProviderManagerTest extends TestCase {

	ServiceSessionHolderProviderManager providerManager;

	protected void setUp() throws Exception {
		super.setUp();
		providerManager = new ServiceSessionHolderProviderManager();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for
	 * 'org.springframework.extensions.jcr.support.ServiceSessionHolderProviderManager.getProviders()'
	 */
	public void testGetProviders() {
		List providers = providerManager.getProviders();
		assertEquals(1, providers.size());
		assertTrue(providers.get(0) instanceof JackRabbitSessionHolderProvider);
	}

}
