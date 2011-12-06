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
package org.springframework.extensions.jcr;

import static org.junit.Assert.assertSame;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * 
 * @author Joerg Bellmann
 *
 */
public class LockManagerTest {

    private final Session session = Mockito.mock(Session.class);

    private final Workspace workspace = Mockito.mock(Workspace.class);

    private final LockManager lockManager = Mockito.mock(LockManager.class);

    private final SessionFactory sessionFactory = Mockito.mock(SessionFactory.class);

    private JcrTemplate jcrTemplate;

    @Before
    public void setUp() throws UnsupportedRepositoryOperationException, RepositoryException {
        Mockito.when(sessionFactory.getSession()).thenReturn(session);
        Mockito.when(session.getWorkspace()).thenReturn(workspace);
        Mockito.when(workspace.getLockManager()).thenReturn(lockManager);
        jcrTemplate = new JcrTemplate(sessionFactory);
        jcrTemplate.setAllowCreate(true);
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.addLockToken(String)'
     */
    @Test
    public void testAddLockToken() throws LockException, RepositoryException {
        jcrTemplate.addLockToken("TEST_LOCK");
        Mockito.verify(lockManager, Mockito.times(1)).addLockToken(Mockito.matches("TEST_LOCK"));
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.getNodeByUUID(String)'
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testGetNodeByUUID() throws RepositoryException {
        String uuid = "uuid";
        Node result = Mockito.mock(Node.class);
        Mockito.when(session.getNodeByIdentifier(uuid)).thenReturn(result);

        assertSame(jcrTemplate.getNodeByUUID(uuid), result);
        assertSame(jcrTemplate.getNodeByIdentifier(uuid), result);
        Mockito.verify(session, Mockito.times(2)).getNodeByIdentifier(Mockito.matches(uuid));
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.removeLockToken(String)'
     */
    @Test
    public void testRemoveLockToken() throws LockException, RepositoryException {
        String lock = "lock";
        jcrTemplate.removeLockToken(lock);
        Mockito.verify(lockManager, Mockito.times(1)).removeLockToken(Mockito.matches(lock));
    }

    /*
     * Test method for 'org.springframework.extensions.jcr.JcrTemplate.getLockTokens()'
     */
    @Test
    public void testGetLockTokens() throws RepositoryException {
        String result[] = { "lock1", "lock2" };
        Mockito.when(lockManager.getLockTokens()).thenReturn(result);
        assertSame(jcrTemplate.getLockTokens(), result);
        Mockito.verify(lockManager, Mockito.times(1)).getLockTokens();
    }
}
