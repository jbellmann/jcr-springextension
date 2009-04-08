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

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Repository;
import javax.jcr.Session;

import junit.framework.TestCase;

import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.SessionHolder;
import org.springframework.extensions.jcr.SessionHolderProvider;

/**
 * @author Costin Leau
 * @author Sergio Bossa
 * @author Salvatore Incandela
 */
public class AbstractSessionHolderProviderManagerTest extends TestCase {

    AbstractSessionHolderProviderManager providerManager;
    List<SessionHolderProvider> providers;
    String repositoryName;
    Session session;
    Repository repository;
    SessionFactory sessionfactory;
    SessionHolderProvider customProvider;

    protected void setUp() throws Exception {
        super.setUp();

        providers = new ArrayList<SessionHolderProvider>();
        repositoryName = "dummyRepository";

        providerManager = new AbstractSessionHolderProviderManager() {
            public List<SessionHolderProvider> getProviders() {
                return providers;
            }
        };
        // build crazy mock hierarchy
        sessionfactory = createMock(SessionFactory.class);
        session = createMock(Session.class);
        repository = createMock(Repository.class);

        expect(repository.getDescriptor(Repository.REP_NAME_DESC)).andReturn(repositoryName);

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
        verify(sessionfactory);
        verify(session);
        verify(repository);

        super.tearDown();
    }

    /*
     * Default provider is used even on empty list. Test method for
     * 'org.springframework.extensions.jcr.support.AbstractSessionHolderProviderManager.getSessionProvider(SessionFactory)'
     */
    public void testDefaultSessionProvider() {
        // sanity check
        assertSame(providers, providerManager.getProviders());

        replay(sessionfactory);
        replay(session);
        replay(repository);

        SessionHolderProvider provider = providerManager.getSessionProvider(repository);
        assertSame(GenericSessionHolderProvider.class, provider.getClass());
    }

    /*
     * Make sure that the approapriate provider is selected Test method for
     * 'org.springframework.extensions.jcr.support.AbstractSessionHolderProviderManager.getSessionProvider(SessionFactory)'
     */
    public void testCustomSessionProvider() {
        // sanity check
        providers = new ArrayList<SessionHolderProvider>();
        providers.add(customProvider);

        replay(sessionfactory);
        replay(session);
        replay(repository);

        assertSame(customProvider, providerManager.getSessionProvider(repository));
    }

    /*
     * Make sure that we fallback to default provider Test method for
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
        providers = new ArrayList<SessionHolderProvider>();
        providers.add(customProvider);

        replay(sessionfactory);
        replay(session);
        replay(repository);

        assertSame(GenericSessionHolderProvider.class, providerManager.getSessionProvider(repository).getClass());
    }
}
