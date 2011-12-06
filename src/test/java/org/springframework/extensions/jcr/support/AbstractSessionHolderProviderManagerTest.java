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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Repository;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.SessionHolder;
import org.springframework.extensions.jcr.SessionHolderProvider;

/**
 * @author Costin Leau
 * @author Sergio Bossa
 * @author Salvatore Incandela
 */
public class AbstractSessionHolderProviderManagerTest {

    private AbstractSessionHolderProviderManager providerManager;
    private List<SessionHolderProvider> providers;
    private String repositoryName;
    private Session session;
    private Repository repository;
    private SessionFactory sessionfactory;
    private SessionHolderProvider customProvider;

    @Before
    public void setUp() throws Exception {

        providers = new ArrayList<SessionHolderProvider>();
        repositoryName = "dummyRepository";

        providerManager = new AbstractSessionHolderProviderManager() {
            @Override
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
            @Override
            public boolean acceptsRepository(String repo) {
                return repositoryName.equals(repo);
            }

            /**
             * @see org.springframework.extensions.jcr.SessionHolderProvider#createSessionHolder(javax.jcr.Session)
             */
            @Override
            public SessionHolder createSessionHolder(Session session) {
                return null;
            }

        };
    }

    protected void tearDown() throws Exception {
        verify(sessionfactory, session, repository);
    }

    /*
     * Default provider is used even on empty list. Test method for
     * 'org.springframework.extensions.jcr.support.AbstractSessionHolderProviderManager.getSessionProvider(SessionFactory)'
     */
    @Test
    public void testDefaultSessionProvider() {
        // sanity check
        assertSame(providers, providerManager.getProviders());

        replay(sessionfactory, session, repository);

        SessionHolderProvider provider = providerManager.getSessionProvider(repository);
        assertSame(GenericSessionHolderProvider.class, provider.getClass());
    }

    /*
     * Make sure that the approapriate provider is selected Test method for
     * 'org.springframework.extensions.jcr.support.AbstractSessionHolderProviderManager.getSessionProvider(SessionFactory)'
     */
    @Test
    public void testCustomSessionProvider() {
        // sanity check
        providers = new ArrayList<SessionHolderProvider>();
        providers.add(customProvider);

        replay(sessionfactory, session, repository);

        assertSame(customProvider, providerManager.getSessionProvider(repository));
    }

    /*
     * Make sure that we fallback to default provider Test method for
     * 'org.springframework.extensions.jcr.support.AbstractSessionHolderProviderManager.getSessionProvider(SessionFactory)'
     */
    @Test
    public void testDifferentSessionProvider() {
        // sanity check

        customProvider = new SessionHolderProvider() {

            /**
             * @see org.springframework.extensions.jcr.SessionHolderProvider#acceptsRepository(java.lang.String)
             */
            @Override
            public boolean acceptsRepository(String repo) {
                return false;
            }

            /**
             * @see org.springframework.extensions.jcr.SessionHolderProvider#createSessionHolder(javax.jcr.Session)
             */
            @Override
            public SessionHolder createSessionHolder(Session session) {
                return null;
            }

        };
        providers = new ArrayList<SessionHolderProvider>();
        providers.add(customProvider);

        replay(sessionfactory, session, repository);

        assertSame(GenericSessionHolderProvider.class, providerManager.getSessionProvider(repository).getClass());
    }
}
