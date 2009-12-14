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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NamespaceException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionException;

import junit.framework.TestCase;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.xml.sax.ContentHandler;

/**
 * @author Costin Leau
 * @author Sergio Bossa
 * @author Salvatore Incandela
 */
public class JcrTemplateTest {

    private SessionFactory sessionFactory;
    private Repository repository;
    private Session session;
    private JcrTemplate jcrTemplate;

    @Before
    public void setUp() throws RepositoryException {

        sessionFactory = createMock(SessionFactory.class);
        session = createMock(Session.class);
        repository = createNiceMock(Repository.class);

        replay(repository);
        expect(session.getRepository()).andReturn(repository).anyTimes();
        expect(sessionFactory.getSession()).andReturn(session);

        replay(sessionFactory);
        replay(session);

        jcrTemplate = new JcrTemplate(sessionFactory);
        jcrTemplate.setAllowCreate(true);

        reset(sessionFactory);
        reset(session);

        session.logout();
        expect(sessionFactory.getSession()).andReturn(session).anyTimes();

    }

    @After
    public void tearDown() {
        try {
            verify(session);
            verify(sessionFactory);
            verify(repository);
        } catch (IllegalStateException ex) {
            // ignore: test method didn't call replay
        }
    }

    @Test
    public void testAfterPropertiesSet() {

        try {
            jcrTemplate.setSessionFactory(null);
            jcrTemplate.afterPropertiesSet();
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            // expected
        }

    }

    @Test
    public void testInvocationHandler() {
        expect(session.getAttribute("smth")).andReturn(null);
        replay(session);
        replay(sessionFactory);

        jcrTemplate.setAllowCreate(true);
        jcrTemplate.setExposeNativeSession(true);

        jcrTemplate.execute(new JcrCallback<Void>() {
            public Void doInJcr(Session sess) throws RepositoryException {
                assertFalse(sess.hashCode() == session.hashCode());
                assertEquals(sess, sess);
                assertFalse(sess.equals(null));
                assertFalse(sess.equals(session));
                sess.getAttribute("smth");
                // logout is proxied so it will not reach our mock
                sess.logout();
                return null;
            }
        }, false);

    }

    @Test
    public void testTemplateExecuteWithNotAllowCreate() {
        jcrTemplate.setAllowCreate(false);
        try {
            jcrTemplate.execute(new JcrCallback<Void>() {
                public Void doInJcr(Session session) {
                    return null;
                }
            });
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTemplateExecuteWithNotAllowCreateAndThreadBound() {
        reset(sessionFactory);
        reset(session);

        replay(sessionFactory, session);

        jcrTemplate.setAllowCreate(false);
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
        final List<String> testList = new ArrayList<String>();
        testList.add("test");
        List<String> result = jcrTemplate.execute(new JcrCallback<List<String>>() {
            public List<String> doInJcr(Session session) {
                return testList;
            }
        });
        assertTrue("Correct result list", result == testList);
        TransactionSynchronizationManager.unbindResource(sessionFactory);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTemplateExecuteWithNewSession() {
        replay(sessionFactory);
        replay(session);

        jcrTemplate.setAllowCreate(true);

        final List<String> testList = new ArrayList<String>();
        testList.add("test");
        List<String> result = (List<String>) jcrTemplate.execute(new JcrCallback<List<String>>() {
            public List<String> doInJcr(Session session) {
                return testList;
            }
        });
        assertTrue("Correct result list", result == testList);
    }

    public void testTemplateExceptions() throws RepositoryException {

        try {
            createTemplate().execute(new JcrCallback<Object>() {
                public Object doInJcr(Session session) throws RepositoryException {
                    throw new AccessDeniedException();
                }
            });
            fail("Should have thrown DataRetrievalFailureException");
        } catch (DataRetrievalFailureException ex) {
            // expected
        }

        try {
            createTemplate().execute(new JcrCallback<Object>() {
                public Object doInJcr(Session session) throws RepositoryException {
                    throw new ConstraintViolationException();
                }
            });
            fail("Should have thrown DataIntegrityViolationException");
        } catch (DataIntegrityViolationException ex) {
            // expected
        }

        try {
            createTemplate().execute(new JcrCallback<Object>() {
                public Object doInJcr(Session session) throws RepositoryException {
                    throw new InvalidItemStateException();
                }
            });
            fail("Should have thrown ConcurrencyFailureException");
        } catch (ConcurrencyFailureException ex) {
            // expected
        }

        try {
            createTemplate().execute(new JcrCallback<Object>() {
                public Object doInJcr(Session session) throws RepositoryException {
                    throw new InvalidQueryException();
                }
            });
            fail("Should have thrown DataRetrievalFailureException");
        } catch (DataRetrievalFailureException ex) {
            // expected
        }

        try {
            createTemplate().execute(new JcrCallback<Object>() {
                public Object doInJcr(Session session) throws RepositoryException {
                    throw new ItemExistsException();
                }
            });
            fail("Should have thrown DataIntegrityViolationException");
        } catch (DataIntegrityViolationException ex) {
            // expected
        }

        try {
            createTemplate().execute(new JcrCallback<Object>() {
                public Object doInJcr(Session session) throws RepositoryException {
                    throw new ItemNotFoundException();
                }
            });
            fail("Should have thrown DataRetrievalFailureException");
        } catch (DataRetrievalFailureException ex) {
            // expected
        }

        try {
            createTemplate().execute(new JcrCallback<Object>() {
                public Object doInJcr(Session session) throws RepositoryException {
                    throw new LockException();
                }
            });
            fail("Should have thrown ConcurrencyFailureException");
        } catch (ConcurrencyFailureException ex) {
            // expected
        }
        try {
            createTemplate().execute(new JcrCallback<Object>() {
                public Object doInJcr(Session session) throws RepositoryException {
                    throw new NamespaceException();
                }
            });
            fail("Should have thrown InvalidDataAccessApiUsageException");
        } catch (InvalidDataAccessApiUsageException ex) {
            // expected
        }
        try {
            createTemplate().execute(new JcrCallback<Object>() {
                public Object doInJcr(Session session) throws RepositoryException {
                    throw new NoSuchNodeTypeException();
                }
            });
            fail("Should have thrown InvalidDataAccessApiUsageException");
        } catch (InvalidDataAccessApiUsageException ex) {
            // expected
        }
        try {
            createTemplate().execute(new JcrCallback<Object>() {
                public Object doInJcr(Session session) throws RepositoryException {
                    throw new NoSuchWorkspaceException();
                }
            });
            fail("Should have thrown DataAccessResourceFailureException");
        } catch (DataAccessResourceFailureException ex) {
            // expected
        }
        try {
            createTemplate().execute(new JcrCallback<Object>() {
                public Object doInJcr(Session session) throws RepositoryException {
                    throw new PathNotFoundException();
                }
            });
            fail("Should have thrown DataRetrievalFailureException");
        } catch (DataRetrievalFailureException ex) {
            // expected
        }
        try {
            createTemplate().execute(new JcrCallback<Object>() {
                public Object doInJcr(Session session) throws RepositoryException {
                    throw new ReferentialIntegrityException();
                }
            });
            fail("Should have thrown DataIntegrityViolationException");
        } catch (DataIntegrityViolationException ex) {
            // expected
        }
        try {
            createTemplate().execute(new JcrCallback<Object>() {
                public Object doInJcr(Session session) throws RepositoryException {
                    throw new UnsupportedRepositoryOperationException();
                }
            });
            fail("Should have thrown InvalidDataAccessApiUsageException");
        } catch (InvalidDataAccessApiUsageException ex) {
            // expected
        }

        try {
            createTemplate().execute(new JcrCallback<Object>() {
                public Object doInJcr(Session session) throws RepositoryException {
                    throw new ValueFormatException();
                }
            });
            fail("Should have thrown InvalidDataAccessApiUsageException");
        } catch (InvalidDataAccessApiUsageException ex) {
            // expected
        }

        try {
            createTemplate().execute(new JcrCallback<Object>() {
                public Object doInJcr(Session session) throws RepositoryException {
                    throw new VersionException();
                }
            });
            fail("Should have thrown DataIntegrityViolationException");
        } catch (DataIntegrityViolationException ex) {
            // expected
        }

        try {
            createTemplate().execute(new JcrCallback<Object>() {
                public Object doInJcr(Session session) throws RepositoryException {
                    throw new RepositoryException();
                }
            });
            fail("Should have thrown JcrSystemException");
        } catch (JcrSystemException ex) {
            // expected
        }

    }

    private JcrOperations createTemplate() throws RepositoryException {
        reset(sessionFactory);
        reset(session);

        expect(sessionFactory.getSession()).andReturn(session);
        session.logout();

        replay(sessionFactory);
        replay(session);

        JcrTemplate template = new JcrTemplate(sessionFactory);
        template.setAllowCreate(true);
        return template;
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.addLockToken(String)'
     */
    @Test
    public void testAddLockToken() {

        String lock = "some lock";
        session.addLockToken(lock);

        replay(session);
        replay(sessionFactory);

        jcrTemplate.addLockToken(lock);

    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.getAttribute(String)'
     */
    @Test
    public void testGetAttribute() {
        String attr = "attribute";
        Object result = new Object();

        expect(session.getAttribute(attr)).andReturn(result);
        replay(session);
        replay(sessionFactory);

        assertSame(jcrTemplate.getAttribute(attr), result);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.getAttributeNames()'
     */
    @Test
    public void testGetAttributeNames() {
        String result[] = {"some node"};
        expect(session.getAttributeNames()).andReturn(result);
        replay(session);
        replay(sessionFactory);

        assertSame(jcrTemplate.getAttributeNames(), result);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.getImportContentHandler(String, int)'
     */
    @Test
    public void testGetImportContentHandler() throws RepositoryException {
        String path = "path";
        ContentHandler result = createMock(ContentHandler.class);

        expect(session.getImportContentHandler(path, 0)).andReturn(result);
        replay(session);
        replay(sessionFactory);

        assertSame(jcrTemplate.getImportContentHandler(path, 0), result);

    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.getItem(String)'
     */
    @Test
    public void testGetItem() throws RepositoryException {
        String path = "path";
        Item result = createMock(Item.class);

        expect(session.getItem(path)).andReturn(result);
        replay(session);
        replay(sessionFactory);

        assertSame(jcrTemplate.getItem(path), result);

    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.getLockTokens()'
     */
    @Test
    public void testGetLockTokens() {
        String result[] = {"lock1", "lock2"};

        expect(session.getLockTokens()).andReturn(result);
        replay(session);
        replay(sessionFactory);

        assertSame(jcrTemplate.getLockTokens(), result);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.getNamespacePrefix(String)'
     */
    @Test
    public void testGetNamespacePrefix() throws RepositoryException {
        String result = "namespace";
        String uri = "prefix";

        expect(session.getNamespacePrefix(uri)).andReturn(result);
        replay(session);
        replay(sessionFactory);

        assertSame(jcrTemplate.getNamespacePrefix(uri), result);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.getNamespacePrefixes()'
     */
    @Test
    public void testGetNamespacePrefixes() throws RepositoryException {
        String result[] = {"prefix1", "prefix2"};

        expect(session.getNamespacePrefixes()).andReturn(result);
        replay(session);
        replay(sessionFactory);

        assertSame(jcrTemplate.getNamespacePrefixes(), result);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.getNamespaceURI(String)'
     */
    @Test
    public void testGetNamespaceURI() throws RepositoryException {
        String result = "namespace";
        String prefix = "prefix";

        expect(session.getNamespaceURI(prefix)).andReturn(result);
        replay(session);
        replay(sessionFactory);

        assertSame(jcrTemplate.getNamespaceURI(prefix), result);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.getNodeByUUID(String)'
     */
    @Test
    public void testGetNodeByUUID() throws RepositoryException {
        Node result = createMock(Node.class);

        String uuid = "uuid";

        expect(session.getNodeByUUID(uuid)).andReturn(result);
        replay(session);
        replay(sessionFactory);

        assertSame(jcrTemplate.getNodeByUUID(uuid), result);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.getUserID()'
     */
    @Test
    public void testGetUserID() {
        String result = "userid";

        expect(session.getUserID()).andReturn(result);
        replay(session);
        replay(sessionFactory);

        assertSame(jcrTemplate.getUserID(), result);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.getValueFactory()'
     */
    @Test
    public void testGetValueFactory() throws RepositoryException {
        ValueFactory result = createMock(ValueFactory.class);

        expect(session.getValueFactory()).andReturn(result);
        replay(session);
        replay(sessionFactory);

        assertSame(jcrTemplate.getValueFactory(), result);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.hasPendingChanges()'
     */
    @Test
    public void testHasPendingChanges() throws RepositoryException {
        boolean result = true;

        expect(session.hasPendingChanges()).andReturn(result);
        replay(session);
        replay(sessionFactory);

        assertEquals(jcrTemplate.hasPendingChanges(), result);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.importXML(String, InputStream, int)'
     */
    @Test
    public void testImportXML() throws RepositoryException, IOException {
        String path = "path";
        InputStream stream = new ByteArrayInputStream(new byte[0]);
        session.importXML(path, stream, 0);

        replay(session);
        replay(sessionFactory);

        jcrTemplate.importXML(path, stream, 0);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.refresh(boolean)'
     */
    @Test
    public void testRefresh() throws RepositoryException {
        boolean refreshMode = true;

        session.refresh(refreshMode);

        replay(session);
        replay(sessionFactory);

        jcrTemplate.refresh(refreshMode);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.removeLockToken(String)'
     */
    @Test
    public void testRemoveLockToken() {
        String lock = "lock";
        session.removeLockToken(lock);

        replay(session);
        replay(sessionFactory);

        jcrTemplate.removeLockToken(lock);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.setNamespacePrefix(String, String)'
     */
    @Test
    public void testSetNamespacePrefix() throws RepositoryException {
        String prefix = "prefix";
        String uri = "uri";
        session.setNamespacePrefix(prefix, uri);

        replay(session);
        replay(sessionFactory);

        jcrTemplate.setNamespacePrefix(prefix, uri);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.isLive()'
     */
    @Test
    public void testIsLive() {
        boolean result = true;

        expect(session.isLive()).andReturn(result);
        replay(session);
        replay(sessionFactory);

        assertEquals(jcrTemplate.isLive(), result);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.itemExists(String)'
     */
    @Test
    public void testItemExists() throws RepositoryException {
        boolean result = true;
        String path = "path";

        expect(session.itemExists(path)).andReturn(result);
        replay(session);
        replay(sessionFactory);

        assertEquals(jcrTemplate.itemExists(path), result);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.move(String, String)'
     */
    @Test
    public void testMove() throws RepositoryException {
        String src = "src";
        String dest = "dest";

        session.move(src, dest);
        replay(session);
        replay(sessionFactory);

        jcrTemplate.move(src, dest);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.save()'
     */
    @Test
    public void testSave() throws RepositoryException {
        session.save();
        replay(session);
        replay(sessionFactory);

        jcrTemplate.save();
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.dump(Node)'
     */
    @Test
    public void testDumpNode() throws RepositoryException {

        Node node = createNiceMock(Node.class);
        PropertyIterator iterator = createMock(PropertyIterator.class);
        NodeIterator iter = createMock(NodeIterator.class);

        expect(node.getPath()).andReturn("path");
        expect(node.getProperties()).andReturn(iterator);
        expect(iterator.hasNext()).andReturn(false);
        expect(node.getNodes()).andReturn(iter);
        expect(iter.hasNext()).andReturn(false);

        expect(session.getRootNode()).andReturn(node);

        replay(session);
        replay(sessionFactory);
        replay(node);

        jcrTemplate.dump(null);

        verify(node);
    }

    @Test
    public void testQueryNode() throws RepositoryException {
        try {
            jcrTemplate.query((Node) null);
            fail("should have thrown exception");
        } catch (IllegalArgumentException e) {
            // it's okay
        }

        Node nd = createMock(Node.class);

        Workspace ws = createMock(Workspace.class);

        QueryManager qm = createMock(QueryManager.class);

        Query query = createMock(Query.class);

        QueryResult result = createMock(QueryResult.class);

        expect(session.getWorkspace()).andReturn(ws);
        expect(ws.getQueryManager()).andReturn(qm);
        expect(qm.getQuery(nd)).andReturn(query);
        expect(query.execute()).andReturn(result);

        replay(sessionFactory, session, ws, qm, query, result);

        assertSame(result, jcrTemplate.query(nd));

        verify(sessionFactory, session, ws, qm, query, result);
    }

    @Test
    public void testExecuteQuery() throws RepositoryException {
        try {
            jcrTemplate.query(null, null);
            fail("should have thrown exception");
        } catch (IllegalArgumentException e) {
            // it's okay
        }

        Workspace workspace = createMock(Workspace.class);
        QueryManager queryManager = createMock(QueryManager.class);
        Query query = createMock(Query.class);
        QueryResult result = createMock(QueryResult.class);

        String stmt = "//*/@bogus:title";
        String language = Query.XPATH;

        expect(session.getWorkspace()).andReturn(workspace);
        expect(workspace.getQueryManager()).andReturn(queryManager);
        expect(queryManager.createQuery(stmt, language)).andReturn(query);
        expect(query.execute()).andReturn(result);

        replay(sessionFactory, session, workspace, queryManager, query, result);

        assertSame(result, jcrTemplate.query(stmt, null));

        verify(sessionFactory, session, workspace, queryManager, query, result);
    }

    @Test
    public void testExecuteQuerySimple() throws RepositoryException {
        try {
            jcrTemplate.query((String) null);
            fail("should have thrown exception");
        } catch (IllegalArgumentException e) {
            // it's okay
        }
        // the rest of the test is covered by testExecuteQuery

    }

    @Test
    public void testGetTree() throws RepositoryException {
        try {
            jcrTemplate.query((List) null);
            fail("should have thown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // it's okay
        }

        List<String> list = new ArrayList<String>();
        String stmt1 = "//*/@bogus:title";
        String stmt2 = "//*";

        list.add(stmt1);
        list.add(stmt2);
        boolean silent = false;

        String language = Query.XPATH;

        Workspace workspace = createMock(Workspace.class);
        QueryManager queryManager = createMock(QueryManager.class);
        Query query = createMock(Query.class);
        QueryResult result = createMock(QueryResult.class);

        expect(session.getWorkspace()).andReturn(workspace);
        expect(workspace.getQueryManager()).andReturn(queryManager);
        expect(queryManager.createQuery(stmt1, language)).andReturn(query);
        expect(queryManager.createQuery(stmt2, language)).andReturn(query);
        expect(query.execute()).andReturn(result);
        expect(query.execute()).andReturn(result);

        replay(sessionFactory, session, workspace, queryManager, query, result);

        Map<String, QueryResult> tree = jcrTemplate.query(list, null, silent);

        assertSame("Results are not the same", result, tree.get("//*"));
        assertSame("Results are not the same", result, tree.get("//*/@bogus:title"));

        verify(sessionFactory, session, workspace, queryManager, query, result);
    }
}
