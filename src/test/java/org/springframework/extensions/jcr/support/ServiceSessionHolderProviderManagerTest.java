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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.jcr.SessionHolderProvider;
import org.springframework.extensions.jcr.jackrabbit.support.JackRabbitSessionHolderProvider;

/**
 * @author Costin Leau
 * @author Sergio Bossa
 * @author Salvatore Incandela
 */
public class ServiceSessionHolderProviderManagerTest {

    ServiceSessionHolderProviderManager providerManager;

    @Before
    public void setUp() throws Exception {

        providerManager = new ServiceSessionHolderProviderManager();
    }

    /*
     * Test method for
     * 'org.springframework.extensions.jcr.support.ServiceSessionHolderProviderManager.getProviders()'
     */
    @Test
    public void testGetProviders() {
        List<SessionHolderProvider> providers = providerManager.getProviders();
        assertEquals(1, providers.size());
        assertTrue(providers.get(0) instanceof JackRabbitSessionHolderProvider);
    }

}
