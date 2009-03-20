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

import org.springframework.extensions.jcr.support.AbstractSessionHolderProviderManager;
import org.springframework.extensions.jcr.support.GenericSessionHolderProvider;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Repository;
import javax.jcr.Session;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.SessionHolder;
import org.springframework.extensions.jcr.SessionHolderProvider;

/**
 * 
 * @author Costin Leau
 * @author Sergio Bossa
 * @author Salvatore Incandela
 * 
 */
public class AbstractSessionHolderProviderManagerTest extends TestCase {

	AbstractSessionHolderProviderManager providerManager;
	List providers;
	String repositoryName;
	MockControl sfCtrl, sessCtrl, repoCtrl;
	Repository repo;
	Session sess;
	SessionFactory sf;
	SessionHolderProvider customProvider;

	protected void setUp() throws Exception {
		super.setUp();

		providers = new ArrayList();
		repositoryName = "dummyRepository";

		providerManager = new AbstractSessionHolderProviderManager() {
			/**
			 * @see org.springframework.extensions.jcr.support.AbstractSessionHolderProviderManager#getProviders()
			 */
			public List getProviders() {
				return providers;
			}
		};
		// build crazy mock hierarchy
		sfCtrl = MockControl.createControl(SessionFactory.class);
		sf = (SessionFactory) sfCtrl.getMock();
		sessCtrl = MockControl.createControl(Session.class);
		sess = (Session) sessCtrl.getMock();
		repoCtrl = MockControl.createControl(Repository.class);
		repo = (Repository) repoCtrl.getMock();

		// sfCtrl.expectAndReturn(sf.getSession(), sess);
		// sessCtrl.expectAndReturn(sess.getRepository(), repo);
		repoCtrl.expectAndReturn(repo.getDescriptor(Repository.REP_NAME_DESC),
				repositoryName);

		customProvider = new SessionHolderProvider() {

			/**
			 * @see org.springframework.extensions.jcr.SessionHolderProvider#acceptsRepository(java.lang.String)
			 */
			public boolean acceptsRepository(String repo) {
				return repositoryName.equals(repo);
			}

			/**
			 * @see org.springframework.extensions.jcr.SessionHolderProvider#createSessionHolder(javax.jcr.Session)
			 */
			public SessionHolder createSessionHolder(Session session) {
				return null;
			}

		};
	}

	protected void tearDown() throws Exception {
		sfCtrl.verify();
		sessCtrl.verify();
		repoCtrl.verify();

		super.tearDown();
	}

	/*
	 * Default provider is used even on empty list.
	 * 
	 * Test method for
	 * 'org.springframework.extensions.jcr.support.AbstractSessionHolderProviderManager.getSessionProvider(SessionFactory)'
	 */
	public void testDefaultSessionProvider() {
		// sanity check
		assertSame(providers, providerManager.getProviders());

		sfCtrl.replay();
		sessCtrl.replay();
		repoCtrl.replay();

		SessionHolderProvider provider = providerManager
				.getSessionProvider(repo);
		assertSame(GenericSessionHolderProvider.class, provider.getClass());
	}

	/*
	 * Make sure that the approapriate provider is selected Test method for
	 * 'org.springframework.extensions.jcr.support.AbstractSessionHolderProviderManager.getSessionProvider(SessionFactory)'
	 */
	public void testCustomSessionProvider() {
		// sanity check

		providers = new ArrayList();
		providers.add(customProvider);

		sfCtrl.replay();
		sessCtrl.replay();
		repoCtrl.replay();

		assertSame(customProvider, providerManager.getSessionProvider(repo));
	}

	/*
	 * Make sure that we fallback to default provider
	 * 
	 * Test method for
	 * 'org.springframework.extensions.jcr.support.AbstractSessionHolderProviderManager.getSessionProvider(SessionFactory)'
	 */
	public void testDifferentSessionProvider() {
		// sanity check

		customProvider = new SessionHolderProvider() {

			/**
			 * @see org.springframework.extensions.jcr.SessionHolderProvider#acceptsRepository(java.lang.String)
			 */
			public boolean acceptsRepository(String repo) {
				return false;
			}

			/**
			 * @see org.springframework.extensions.jcr.SessionHolderProvider#createSessionHolder(javax.jcr.Session)
			 */
			public SessionHolder createSessionHolder(Session session) {
				return null;
			}

		};
		providers = new ArrayList();
		providers.add(customProvider);

		sfCtrl.replay();
		sessCtrl.replay();
		repoCtrl.replay();

		assertSame(GenericSessionHolderProvider.class, providerManager
				.getSessionProvider(repo).getClass());
	}
}
