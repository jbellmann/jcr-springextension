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

import java.util.List;

import javax.jcr.Repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.jcr.SessionHolderProvider;
import org.springframework.extensions.jcr.SessionHolderProviderManager;

/**
 * Base implementation for SessionHolderProviderManager that adds most of the
 * functionality needed by the interface. Usually interface implementations will
 * extends this class.
 * 
 * @author Costin Leau
 * @author Sergio Bossa 
 * @author Salvatore Incandela
 * 
 */
public abstract class AbstractSessionHolderProviderManager implements
		SessionHolderProviderManager {

	protected final Log log = LogFactory.getLog(getClass());

	protected SessionHolderProvider defaultProvider = new GenericSessionHolderProvider();

	/**
	 * Returns all the providers for this class. Subclasses have to implement
	 * this method.
	 * 
	 * @return sessionHolderProviders
	 */
	public abstract List getProviders();

	/**
	 * @see org.springframework.extensions.jcr.SessionHolderProviderManager#getSessionProvider(Repository)
	 */
	public SessionHolderProvider getSessionProvider(Repository repository) {
		// graceful fallback
		if (repository == null)
			return defaultProvider;

		String key = repository.getDescriptor(Repository.REP_NAME_DESC);
		List providers = getProviders();

		// search the provider
		for (int i = 0; i < providers.size(); i++) {
			SessionHolderProvider provider = (SessionHolderProvider) providers
					.get(i);
			if (provider.acceptsRepository(key)) {
				if (log.isDebugEnabled())
					log
							.debug("specific SessionHolderProvider found for repository "
									+ key);
				return provider;
			}
		}

		// no provider found - return the default one
		if (log.isDebugEnabled())
			log.debug("no specific SessionHolderProvider found for repository "
					+ key + "; using the default one");
		return defaultProvider;
	}
}
