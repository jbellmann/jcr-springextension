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
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.jcr.Session;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.Test;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.SessionHolder;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;

/**
 * @author Costin Leau
 * @author Sergio Bossa
 * @author Salvatore Incandela
 */
public class OpenSessionInViewTest {

    @Test
    public void testOpenSessionInViewInterceptor() throws Exception {
        final SessionFactory sessionFactory = createMock(SessionFactory.class);
        final Session session = createMock(Session.class);

        OpenSessionInViewInterceptor interceptor = new OpenSessionInViewInterceptor();

        MockServletContext sc = new MockServletContext();
        MockHttpServletRequest request = new MockHttpServletRequest(sc);
        MockHttpServletResponse response = new MockHttpServletResponse();

        expect(sessionFactory.getSession()).andReturn(session);
        SessionHolder holder = new SessionHolder(session);
        expect(sessionFactory.getSessionHolder(session)).andReturn(holder);
        replay(sessionFactory);
        replay(session);

        interceptor.setSessionFactory(sessionFactory);
        interceptor.afterPropertiesSet();

        interceptor.preHandle(request, response, "handler");
        assertTrue(TransactionSynchronizationManager.hasResource(sessionFactory));
        assertSame(holder, TransactionSynchronizationManager.getResource(sessionFactory));

        // check that further invocations simply participate
        interceptor.preHandle(request, response, "handler");

        interceptor.preHandle(request, response, "handler");
        interceptor.postHandle(request, response, "handler", null);
        interceptor.afterCompletion(request, response, "handler", null);

        interceptor.postHandle(request, response, "handler", null);
        interceptor.afterCompletion(request, response, "handler", null);

        interceptor.preHandle(request, response, "handler");
        interceptor.postHandle(request, response, "handler", null);
        interceptor.afterCompletion(request, response, "handler", null);

        verify(sessionFactory);
        verify(session);

        reset(sessionFactory);
        reset(session);

        replay(sessionFactory);
        replay(session);

        interceptor.postHandle(request, response, "handler", null);
        assertTrue(TransactionSynchronizationManager.hasResource(sessionFactory));
        assertSame(holder, TransactionSynchronizationManager.getResource(sessionFactory));

        verify(sessionFactory);
        verify(session);

        reset(sessionFactory);
        reset(session);

        session.logout();
        expectLastCall().once();

        replay(sessionFactory);
        replay(session);
        interceptor.afterCompletion(request, response, "handler", null);
        assertFalse(TransactionSynchronizationManager.hasResource(sessionFactory));
        verify(sessionFactory);
        verify(session);

    }

    public void testOpenSessionInViewFilter() throws Exception {
        final SessionFactory sessionFactory = createMock(SessionFactory.class);
        final Session session = createMock(Session.class);

        // set up the session factory
        expect(sessionFactory.getSession()).andReturn(session);
        final SessionHolder holder = new SessionHolder(session);
        expect(sessionFactory.getSessionHolder(session)).andReturn(holder);

        session.logout();
        expectLastCall().once();

        replay(sessionFactory);
        replay(session);

        // set up the second session factory
        final SessionFactory sessionFactory2 = createMock(SessionFactory.class);
        final Session session2 = createMock(Session.class);

        expect(sessionFactory2.getSession()).andReturn(session2);
        final SessionHolder holder2 = new SessionHolder(session2);
        expect(sessionFactory2.getSessionHolder(session2)).andReturn(holder2);
        session2.logout();
        expectLastCall().once();

        replay(sessionFactory2);
        replay(session2);

        MockServletContext sc = new MockServletContext();
        StaticWebApplicationContext wac = new StaticWebApplicationContext();
        wac.setServletContext(sc);
        wac.getDefaultListableBeanFactory().registerSingleton("sessionFactory", sessionFactory);
        wac.getDefaultListableBeanFactory().registerSingleton("mySessionFactory", sessionFactory2);
        wac.refresh();
        sc.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
        MockHttpServletRequest request = new MockHttpServletRequest(sc);
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterConfig filterConfig = new MockFilterConfig(wac.getServletContext(), "filter");
        MockFilterConfig filterConfig2 = new MockFilterConfig(wac.getServletContext(), "filter2");
        filterConfig2.addInitParameter("sessionFactoryBeanName", "mySessionFactory");

        MockFilterConfig filterConfig3 = new MockFilterConfig(wac.getServletContext(), "filter3");

        final OpenSessionInViewFilter filter = new OpenSessionInViewFilter();
        filter.init(filterConfig);
        final OpenSessionInViewFilter filter2 = new OpenSessionInViewFilter();
        filter2.init(filterConfig2);
        final OpenSessionInViewFilter filter3 = new OpenSessionInViewFilter();
        filter3.init(filterConfig3);

        final FilterChain filterChain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException,
                    ServletException {
                assertTrue(TransactionSynchronizationManager.hasResource(sessionFactory));
                // check sf-related things
                assertSame(holder, TransactionSynchronizationManager.getResource(sessionFactory));
                assertSame(session, holder.getSession());

                servletRequest.setAttribute("invoked", Boolean.TRUE);
            }
        };

        final FilterChain filterChain2 = new FilterChain() {
            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException,
                    ServletException {
                assertTrue(TransactionSynchronizationManager.hasResource(sessionFactory));
                // check sf-related things
                assertSame(holder, TransactionSynchronizationManager.getResource(sessionFactory));
                assertSame(session, holder.getSession());

                filter3.doFilter(servletRequest, servletResponse, filterChain);
            }
        };

        final FilterChain filterChain3 = new FilterChain() {
            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException,
                    ServletException {
                assertTrue(TransactionSynchronizationManager.hasResource(sessionFactory2));
                // check sf2-related things
                assertSame(holder2, TransactionSynchronizationManager.getResource(sessionFactory2));
                assertSame(session2, holder2.getSession());

                filter.doFilter(servletRequest, servletResponse, filterChain2);
            }
        };

        FilterChain filterChain4 = new FilterChain() {
            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException,
                    ServletException {
                filter2.doFilter(servletRequest, servletResponse, filterChain3);
            }
        };

        assertFalse(TransactionSynchronizationManager.hasResource(sessionFactory));
        assertFalse(TransactionSynchronizationManager.hasResource(sessionFactory2));
        filter2.doFilter(request, response, filterChain4);
        assertFalse(TransactionSynchronizationManager.hasResource(sessionFactory));
        assertFalse(TransactionSynchronizationManager.hasResource(sessionFactory2));
        assertNotNull(request.getAttribute("invoked"));

        verify(sessionFactory, session, sessionFactory2, session2);

        wac.close();
    }

}
