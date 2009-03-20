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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.extensions.jcr.SessionHolderProvider;

import sun.misc.Service;
import sun.misc.ServiceConfigurationError;

/**
 * Implementation of SessionHolderProviderManager which does dynamic discovery
 * of the providers using the JDK 1.3+ <a href=
 * "http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html#Service%20Provider">
 * 'Service Provider' specification</a>.
 * 
 * The class will look for
 * org.springframework.extensions.jcr.SessionHolderProvider property files in
 * META-INF/services directories.
 * 
 * @author Costin Leau
 * @author Sergio Bossa
 * @author Salvatore Incandela
 * 
 */
public class ServiceSessionHolderProviderManager extends
		CacheableSessionHolderProviderManager {

	/**
	 * Loads the service providers using the discovery mechanism.
	 * 
	 * @return the list of service providers found.
	 */
	public List getProviders() {
		Iterator i = Service.providers(SessionHolderProvider.class, Thread
				.currentThread().getContextClassLoader());
		List providers = new ArrayList();
		for (; i.hasNext();) {
			try {
				providers.add(i.next());
			} catch (ServiceConfigurationError sce) {
				if (!(sce.getCause() instanceof SecurityException))
					throw sce;
			}
		}
		return Collections.unmodifiableList(providers);
	}
}
