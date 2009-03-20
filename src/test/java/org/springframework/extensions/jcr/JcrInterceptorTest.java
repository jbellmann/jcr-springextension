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
package org.springframework.extensions.jcr;

import org.springframework.extensions.jcr.SessionHolder;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.JcrInterceptor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import junit.framework.TestCase;

import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.Invocation;
import org.aopalliance.intercept.MethodInvocation;
import org.easymock.MockControl;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author Costin Leau
 * @author Sergio Bossa
 * @author Salvatore Incandela
 * 
 */
public class JcrInterceptorTest extends TestCase {

	public void testInterceptor() throws RepositoryException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();

		sfControl.expectAndReturn(sf.getSession(), session);
		session.logout();
		sessionControl.setVoidCallable(1);
		sfControl.expectAndReturn(sf.getSessionHolder(session),
				new SessionHolder(session));
		sfControl.replay();
		sessionControl.replay();

		JcrInterceptor interceptor = new JcrInterceptor();
		interceptor.setSessionFactory(sf);
		interceptor.afterPropertiesSet();
		try {
			interceptor.invoke(new TestInvocation(sf));
		} catch (Throwable t) {
			fail("Should not have thrown Throwable: " + t);
		}

		sfControl.verify();
		sessionControl.verify();
	}

	public void testInterceptorWithPrebound() {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl repoControl = MockControl
				.createNiceControl(Repository.class);

		sfControl.replay();
		sessionControl.replay();
		repoControl.replay();

		TransactionSynchronizationManager.bindResource(sf, new SessionHolder(
				session));
		JcrInterceptor interceptor = new JcrInterceptor();
		interceptor.setSessionFactory(sf);
		interceptor.afterPropertiesSet();
		try {
			interceptor.invoke(new TestInvocation(sf));
		} catch (Throwable t) {
			fail("Should not have thrown Throwable: " + t.getMessage());
		} finally {
			TransactionSynchronizationManager.unbindResource(sf);
		}

		sfControl.verify();
		sessionControl.verify();
	}

	private static class TestInvocation implements MethodInvocation {

		private SessionFactory sessionFactory;

		public TestInvocation(SessionFactory sessionFactory) {
			this.sessionFactory = sessionFactory;
		}

		public Object proceed() throws Throwable {
			if (!TransactionSynchronizationManager
					.hasResource(this.sessionFactory)) {
				throw new IllegalStateException("Session not bound");
			}
			return null;
		}

		public Object[] getArguments() {
			return null;
		}

		public int getCurrentInterceptorIndex() {
			return 0;
		}

		public int getNumberOfInterceptors() {
			return 0;
		}

		public Interceptor getInterceptor(int i) {
			return null;
		}

		public Method getMethod() {
			return null;
		}

		public AccessibleObject getStaticPart() {
			return getMethod();
		}

		public Object getArgument(int i) {
			return null;
		}

		public void setArgument(int i, Object handler) {
		}

		public int getArgumentCount() {
			return 0;
		}

		public Object getThis() {
			return null;
		}

		public Object getProxy() {
			return null;
		}

		public Invocation cloneInstance() {
			return null;
		}

		public void release() {
		}
	}

}
