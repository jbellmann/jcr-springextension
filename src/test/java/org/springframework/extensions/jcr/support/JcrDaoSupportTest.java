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

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;

/**
 * @author Costin Leau
 * @author Sergio Bossa
 * @author Salvatore Incandela
 */
public class JcrDaoSupportTest {

    private SessionFactory sessionFactory;
    private Session session;
    private Repository repository;

    @Before
    public void setUp() throws Exception {

        sessionFactory = createMock(SessionFactory.class);

        session = createMock(Session.class);
        repository = createMock(Repository.class);

    }

    @After
    public void tearDown() throws Exception {
        try {
            verify(session, sessionFactory, repository);
        } catch (IllegalStateException ex) {
            // ignore: test method didn't call replay
        }
    }

    @Test
    public void testJcrDaoSupportWithSessionFactory() throws Exception {

        replay(sessionFactory, session);

        JcrDaoSupport dao = new JcrDaoSupport() {
        };

        dao.setSessionFactory(sessionFactory);
        dao.afterPropertiesSet();
        assertEquals("Correct SessionFactory", sessionFactory, dao.getSessionFactory());
        verify(sessionFactory);
    }

    @Test
    public void testJcrDaoSupportWithJcrTemplate() throws Exception {

        JcrTemplate template = new JcrTemplate();
        JcrDaoSupport dao = new JcrDaoSupport() {
        };

        dao.setTemplate(template);
        dao.afterPropertiesSet();
        assertEquals("Correct JcrTemplate", template, dao.getTemplate());
    }

    @Test
    public void testAfterPropertiesSet() {
        JcrDaoSupport dao = new JcrDaoSupport() {
        };

        try {
            dao.afterPropertiesSet();
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            //
        }
    }

    @Test
    public void testSetSessionFactory() throws RepositoryException {
        // sessCtrl.expectAndReturn(sess.getRepository(), repository,
        // MockControl.ONE_OR_MORE);
        // sfCtrl.expectAndReturn(sf.getSession(), sess);
        replay(sessionFactory);
        replay(session);

        JcrDaoSupport dao = new JcrDaoSupport() {
        };

        dao.setSessionFactory(sessionFactory);

        assertEquals(dao.getSessionFactory(), sessionFactory);
    }

    @Test
    public void testGetSession() throws RepositoryException {
        JcrDaoSupport dao = new JcrDaoSupport() {
        };
        // used for service provider

        expect(sessionFactory.getSession()).andReturn(session);
        replay(sessionFactory);
        replay(session);

        dao.setSessionFactory(sessionFactory);
        dao.afterPropertiesSet();
        try {
            dao.getSession();
            fail("expected exception");
        } catch (IllegalStateException e) {
            // it's okay
        }
        assertEquals(dao.getSession(true), session);
    }

    @Test
    public void testReleaseSession() {
        JcrDaoSupport dao = new JcrDaoSupport() {
        };

        dao.releaseSession(null);

        session.logout();

        replay(sessionFactory);
        replay(session);

        dao.setSessionFactory(sessionFactory);
        dao.afterPropertiesSet();
        dao.releaseSession(session);
    }

    @Test
    public void testConvertException() {
        JcrDaoSupport dao = new JcrDaoSupport() {
        };
        JcrTemplate jcrTemplate = createMock(JcrTemplate.class);

        RepositoryException ex = new RepositoryException();

        expect(jcrTemplate.convertJcrAccessException(ex)).andReturn(null);
        dao.setTemplate(jcrTemplate);
        dao.convertJcrAccessException(ex);
    }

}
