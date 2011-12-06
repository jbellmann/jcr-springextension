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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAResource;

import org.apache.jackrabbit.api.XASession;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.extensions.jcr.JcrInterceptor;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.SessionHolder;
import org.springframework.extensions.jcr.SessionHolderProvider;
import org.springframework.extensions.jcr.jackrabbit.support.JackRabbitSessionHolderProvider;
import org.springframework.extensions.jcr.support.ListSessionHolderProviderManager;

/**
 * 
 * @author Joerg Bellmann
 * 
 * see {@link JcrInterceptor2Test#testCreateSessionHolder()}
 *
 */
@Ignore
public class JcrInterceptorTest {

    /*
     * Test method for
     * 'org.springframework.extensions.jcr.jackrabbit.JcrInterceptor.createSessionHolder(Session)'
     */
    @Test
    public void testCreateSessionHolder() throws Exception {
        final SessionFactory sessionFactory = createMock(SessionFactory.class);
        final XASession xaSession = createMock(XASession.class);
        XAResource xaResource = createMock(XAResource.class);

        expect(xaSession.getXAResource()).andReturn(xaResource);

        replay(sessionFactory);
        replay(xaSession);
        replay(xaResource);

        JcrInterceptor interceptor = new JcrInterceptor();
        ListSessionHolderProviderManager manager = new ListSessionHolderProviderManager();
        List<SessionHolderProvider> providers = new ArrayList<SessionHolderProvider>();
        SessionHolderProvider provider = new JackRabbitSessionHolderProvider();
        providers.add(provider);
        manager.setProviders(providers);
        interceptor.setSessionFactory(sessionFactory);
        interceptor.afterPropertiesSet();

        SessionHolder holder = null;

        holder = provider.createSessionHolder(xaSession);

        assertSame(xaSession, holder.getSession());

        verify(sessionFactory);
        verify(xaSession);
        verify(xaResource);
    }
}
