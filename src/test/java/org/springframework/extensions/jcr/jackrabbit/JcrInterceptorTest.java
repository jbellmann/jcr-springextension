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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Session;
import javax.transaction.xa.XAResource;

import junit.framework.TestCase;

import org.apache.jackrabbit.api.XASession;
import org.easymock.MockControl;
import org.springframework.extensions.jcr.JcrInterceptor;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.SessionHolder;
import org.springframework.extensions.jcr.SessionHolderProvider;
import org.springframework.extensions.jcr.jackrabbit.support.JackRabbitSessionHolderProvider;
import org.springframework.extensions.jcr.support.ListSessionHolderProviderManager;

public class JcrInterceptorTest extends TestCase {

    /*
     * Test method for
     * 'org.springframework.extensions.jcr.jackrabbit.JcrInterceptor.createSessionHolder(Session)'
     */
    public void testCreateSessionHolder() throws Exception {
        MockControl sfCtrl = MockControl.createControl(SessionFactory.class);
        SessionFactory sf = (SessionFactory) sfCtrl.getMock();
        MockControl sessionControl = MockControl.createControl(Session.class);

        MockControl xaSessionControl = MockControl.createControl(XASession.class);
        XASession xaSession = (XASession) xaSessionControl.getMock();

        MockControl xaResCtrl = MockControl.createControl(XAResource.class);
        XAResource xaRes = (XAResource) xaResCtrl.getMock();

        xaSessionControl.expectAndReturn(xaSession.getXAResource(), xaRes);
        xaSessionControl.replay();

        sfCtrl.replay();
        sessionControl.replay();
        xaResCtrl.replay();

        JcrInterceptor interceptor = new JcrInterceptor();
        ListSessionHolderProviderManager manager = new ListSessionHolderProviderManager();
        List<SessionHolderProvider> providers = new ArrayList<SessionHolderProvider>();
        SessionHolderProvider provider = new JackRabbitSessionHolderProvider();
        providers.add(provider);
        manager.setProviders(providers);
        interceptor.setSessionFactory(sf);
        interceptor.afterPropertiesSet();

        SessionHolder holder = null;

        holder = provider.createSessionHolder(xaSession);

        assertSame(xaSession, holder.getSession());

        xaSessionControl.verify();
        sessionControl.verify();
        xaResCtrl.verify();
        sfCtrl.verify();
    }
}
