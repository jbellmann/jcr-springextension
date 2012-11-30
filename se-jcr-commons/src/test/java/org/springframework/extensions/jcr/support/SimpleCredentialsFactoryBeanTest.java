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
package org.springframework.extensions.jcr.support;

import static org.junit.Assert.*;

import javax.jcr.SimpleCredentials;

import org.junit.Test;


/**
 * @author Mirko Zeibig
 *
 */
public class SimpleCredentialsFactoryBeanTest {

    @Test
    public void testGetObject() throws Exception {
        SimpleCredentialsFactoryBean factoryBean = new SimpleCredentialsFactoryBean();
        factoryBean.setUserID("testuser");
        factoryBean.setPassword("secret");
        SimpleCredentials sc = factoryBean.getObject();
        assertNotNull(sc);
        assertEquals(factoryBean.getObjectType(), sc.getClass());
        assertEquals("testuser", sc.getUserID());
        assertEquals("secret", new String(sc.getPassword()));
    }

}
