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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.jackrabbit.support.UserTxSessionHolder;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 
 * @author Joerg Bellmann
 *
 */
public class LocalTransactionManager2Test {

    private final SessionFactory sessionFactory = Mockito.mock(SessionFactory.class);
    private final Session session = Mockito.mock(Session.class, Mockito.withSettings()
            .extraInterfaces(XAResource.class));

    @Before
    public void setUp() throws RepositoryException, XAException {
        Mockito.when(sessionFactory.getSession()).thenReturn(session);
    }

    @Test
    public void testTransactionCommit() throws RepositoryException, XAException {
        Mockito.when(((XAResource) session).prepare(Mockito.any(Xid.class))).thenReturn(0);

        //do not know why the following two methods on session are called, it is a mock, nobody cares
        session.save();
        session.logout();

        Xid xidMock = new XidMock();

        ((XAResource) session).start(xidMock, XAResource.TMNOFLAGS);
        ((XAResource) session).commit(xidMock, false);
        ((XAResource) session).end(xidMock, XAResource.TMSUCCESS);

        PlatformTransactionManager tm = new LocalTransactionManager(sessionFactory);
        TransactionTemplate tt = new TransactionTemplate(tm);
        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sessionFactory));
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

        tt.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sessionFactory));
                JcrTemplate template = new JcrTemplate(sessionFactory);
                template.save();
            }
        });
    }

    @Test
    public void testTransactionRollback() throws RepositoryException, XAException {
        //do not know why the following two methods on session are called, it is a mock, nobody cares
        session.save();
        session.logout();

        Xid xidMock = new XidMock();

        ((XAResource) session).start(xidMock, XAResource.TMNOFLAGS);
        ((XAResource) session).end(xidMock, XAResource.TMFAIL);
        ((XAResource) session).rollback(xidMock);

        PlatformTransactionManager tm = new LocalTransactionManager(sessionFactory);
        TransactionTemplate tt = new TransactionTemplate(tm);
        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sessionFactory));
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

        try {
            tt.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sessionFactory));
                    JcrTemplate template = new JcrTemplate(sessionFactory);
                    template.execute(new JcrCallback<Object>() {
                        @Override
                        public Object doInJcr(Session se) throws RepositoryException {
                            se.save();
                            throw new RuntimeException();
                        }
                    });
                }
            });
        } catch (RuntimeException e) {
            // it's okay
        }

        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sessionFactory));
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

    }

    @Test
    public void testTransactionRollbackOnly() throws Exception {
        session.save();
        session.logout();

        Xid xidMock = new XidMock();

        ((XAResource) session).start(xidMock, XAResource.TMNOFLAGS);
        ((XAResource) session).end(xidMock, XAResource.TMFAIL);
        ((XAResource) session).rollback(xidMock);

        PlatformTransactionManager transactionManager = new LocalTransactionManager(sessionFactory);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sessionFactory));
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sessionFactory));
                JcrTemplate template = new JcrTemplate(sessionFactory);
                template.execute(new JcrCallback<Object>() {
                    @Override
                    public Object doInJcr(Session se) throws RepositoryException {
                        se.save();
                        return null;
                    }

                });
                status.setRollbackOnly();
            }
        });

        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sessionFactory));
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
    }

    @Test
    public void testInavlidIsolation() {
        PlatformTransactionManager transactionManager = new LocalTransactionManager(sessionFactory);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sessionFactory));
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
        try {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sessionFactory));
                    JcrTemplate template = new JcrTemplate(sessionFactory);
                    template.execute(new JcrCallback<Object>() {
                        @Override
                        public Object doInJcr(Session session) throws RepositoryException {
                            return null;
                        }
                    });
                }
            });
            fail("Should have thrown InvalidIsolationLevelException");

        } catch (InvalidIsolationLevelException e) {
            // it's okay
        }

        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sessionFactory));
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
    }

    @Test
    public void testTransactionCommitWithPrebound() throws Exception {
        PlatformTransactionManager transactionManager = new LocalTransactionManager(sessionFactory);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        UserTxSessionHolder userTransaction = new UserTxSessionHolder(session);
        TransactionSynchronizationManager.bindResource(sessionFactory, userTransaction);

        assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sessionFactory));

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sessionFactory));
                JcrTemplate template = new JcrTemplate(sessionFactory);
                template.save();
            }
        });

        assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sessionFactory));
        TransactionSynchronizationManager.unbindResource(sessionFactory);
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
    }

    @Test
    public void testTransactionRollbackOnlyWithPrebound() throws Exception {
        PlatformTransactionManager transactionManager = new LocalTransactionManager(sessionFactory);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        UserTxSessionHolder userTransaction = new UserTxSessionHolder(session);
        TransactionSynchronizationManager.bindResource(sessionFactory, userTransaction);

        assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sessionFactory));
        userTransaction.setRollbackOnly();

        try {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sessionFactory));
                    JcrTemplate template = new JcrTemplate(sessionFactory);
                    template.save();
                }
            });

        } catch (UnexpectedRollbackException e) {
            System.out.println(e);
        }

        assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sessionFactory));
        TransactionSynchronizationManager.unbindResource(sessionFactory);
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
    }
}
