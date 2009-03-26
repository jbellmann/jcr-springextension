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

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import junit.framework.TestCase;

import org.apache.jackrabbit.api.XASession;
import org.easymock.MockControl;
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
 * @author Costin Leau
 * @author Sergio Bossa
 * @author Salvatore Incandela
 */
public class LocalTransactionManagerTest extends TestCase {

    public void testTransactionCommit() throws Exception {
        MockControl sessionFactoryControl = MockControl.createControl(SessionFactory.class);
        final SessionFactory sessionFactory = (SessionFactory) sessionFactoryControl.getMock();
        MockControl sessionControl = MockControl.createControl(XASession.class);
        final XASession xaSession = (XASession) sessionControl.getMock();
        // create nice mock
        MockControl xaResourceControl = MockControl.createControl(XAResource.class);
        XAResource xaResource = (XAResource) xaResourceControl.getMock();

        sessionFactoryControl.expectAndReturn(sessionFactory.getSession(), xaSession);
        sessionControl.expectAndReturn(xaSession.getXAResource(), xaResource);

        xaSession.save();
        xaSession.logout();

        /*
         * MockControl repositoryControl = MockControl.createNiceControl(Repository.class); Repository
         * repository = (Repository) repositoryControl.getMock(); repositoryControl.replay();
         * sessionControl.expectAndReturn(session.getRepository(), repository, MockControl.ONE_OR_MORE); final
         * SessionHolderProviderManager providerManager = new ListSessionHolderProviderManager();
         */

        Xid xidMock = new XidMock();

        xaResource.start(xidMock, XAResource.TMNOFLAGS);
        xaResourceControl.setMatcher(MockControl.ALWAYS_MATCHER);
        xaResource.prepare(xidMock);
        xaResourceControl.setMatcher(MockControl.ALWAYS_MATCHER);
        xaResourceControl.setReturnValue(0);
        xaResource.commit(xidMock, false);
        xaResourceControl.setMatcher(MockControl.ALWAYS_MATCHER);
        xaResource.end(xidMock, XAResource.TMSUCCESS);
        xaResourceControl.setMatcher(MockControl.ALWAYS_MATCHER);

        sessionFactoryControl.replay();
        sessionControl.replay();
        xaResourceControl.replay();

        PlatformTransactionManager tm = new LocalTransactionManager(sessionFactory);
        TransactionTemplate tt = new TransactionTemplate(tm);
        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sessionFactory));
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

        tt.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sessionFactory));
                JcrTemplate template = new JcrTemplate(sessionFactory);
                template.save();
            }
        });

        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sessionFactory));
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

        sessionFactoryControl.verify();
        sessionControl.verify();
        xaResourceControl.verify();
    }

    public void testTransactionRollback() throws Exception {
        MockControl sessionFactoryControl = MockControl.createControl(SessionFactory.class);
        final SessionFactory sf = (SessionFactory) sessionFactoryControl.getMock();
        MockControl sessionControl = MockControl.createControl(XASession.class);
        final XASession session = (XASession) sessionControl.getMock();
        // create nice mock
        MockControl xaResControl = MockControl.createControl(XAResource.class);
        XAResource xaRes = (XAResource) xaResControl.getMock();

        sessionFactoryControl.expectAndReturn(sf.getSession(), session);

        sessionControl.expectAndReturn(session.getXAResource(), xaRes);
        session.save();
        session.logout();
        /*
         * // used for ServiceProvider MockControl repositoryControl =
         * MockControl.createNiceControl(Repository.class); Repository repository = (Repository)
         * repositoryControl.getMock(); repositoryControl.replay();
         * sessionControl.expectAndReturn(session.getRepository(), repository, MockControl.ONE_OR_MORE);
         */

        Xid xidMock = new XidMock();

        xaRes.start(xidMock, XAResource.TMNOFLAGS);
        xaResControl.setMatcher(MockControl.ALWAYS_MATCHER);
        xaRes.end(xidMock, XAResource.TMFAIL);
        xaResControl.setMatcher(MockControl.ALWAYS_MATCHER);
        xaRes.rollback(xidMock);
        xaResControl.setMatcher(MockControl.ALWAYS_MATCHER);

        sessionFactoryControl.replay();
        sessionControl.replay();
        xaResControl.replay();

        PlatformTransactionManager tm = new LocalTransactionManager(sf);
        TransactionTemplate tt = new TransactionTemplate(tm);
        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

        try {
            tt.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
                    JcrTemplate template = new JcrTemplate(sf);
                    template.execute(new JcrCallback() {
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

        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

        sessionFactoryControl.verify();
        sessionControl.verify();
        xaResControl.verify();
    }

    public void testTransactionRollbackOnly() throws Exception {
        MockControl sessionFactoryControl = MockControl.createControl(SessionFactory.class);
        final SessionFactory sf = (SessionFactory) sessionFactoryControl.getMock();
        MockControl sessionControl = MockControl.createControl(XASession.class);
        final XASession session = (XASession) sessionControl.getMock();
        // create nice mock
        MockControl xaResControl = MockControl.createControl(XAResource.class);
        XAResource xaRes = (XAResource) xaResControl.getMock();

        sessionFactoryControl.expectAndReturn(sf.getSession(), session);

        sessionControl.expectAndReturn(session.getXAResource(), xaRes);
        session.save();
        session.logout();

        Xid xidMock = new XidMock();

        xaRes.start(xidMock, XAResource.TMNOFLAGS);
        xaResControl.setMatcher(MockControl.ALWAYS_MATCHER);
        xaRes.end(xidMock, XAResource.TMFAIL);
        xaResControl.setMatcher(MockControl.ALWAYS_MATCHER);
        xaRes.rollback(xidMock);
        xaResControl.setMatcher(MockControl.ALWAYS_MATCHER);

        sessionFactoryControl.replay();
        sessionControl.replay();
        xaResControl.replay();
        PlatformTransactionManager tm = new LocalTransactionManager(sf);
        TransactionTemplate tt = new TransactionTemplate(tm);
        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

        tt.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
                JcrTemplate template = new JcrTemplate(sf);
                template.execute(new JcrCallback() {
                    public Object doInJcr(Session se) throws RepositoryException {
                        se.save();
                        return null;
                    }

                });
                status.setRollbackOnly();
            }
        });

        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

        sessionFactoryControl.verify();
        sessionControl.verify();
        xaResControl.verify();
    }

    public void testInvalidIsolation() throws Exception {
        MockControl sessionFactoryControl = MockControl.createControl(SessionFactory.class);
        final SessionFactory sf = (SessionFactory) sessionFactoryControl.getMock();

        sessionFactoryControl.replay();

        PlatformTransactionManager tm = new LocalTransactionManager(sf);
        TransactionTemplate tt = new TransactionTemplate(tm);

        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

        tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
        try {
            tt.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
                    JcrTemplate template = new JcrTemplate(sf);
                    template.execute(new JcrCallback() {
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

        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

        sessionFactoryControl.verify();
    }

    public void testTransactionCommitWithPrebound() throws Exception {
        MockControl sessionFactoryControl = MockControl.createControl(SessionFactory.class);
        final SessionFactory sf = (SessionFactory) sessionFactoryControl.getMock();
        MockControl sessionControl = MockControl.createControl(XASession.class);
        final XASession session = (XASession) sessionControl.getMock();

        MockControl xaResControl = MockControl.createControl(XAResource.class);
        XAResource xaRes = (XAResource) xaResControl.getMock();

        sessionControl.expectAndReturn(session.getXAResource(), xaRes);
        session.save();

        sessionFactoryControl.replay();
        sessionControl.replay();
        xaResControl.replay();

        PlatformTransactionManager tm = new LocalTransactionManager(sf);
        TransactionTemplate tt = new TransactionTemplate(tm);
        UserTxSessionHolder uTx = new UserTxSessionHolder(session);
        TransactionSynchronizationManager.bindResource(sf, uTx);

        assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));

        tt.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
                JcrTemplate template = new JcrTemplate(sf);
                template.save();
            }
        });

        assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
        TransactionSynchronizationManager.unbindResource(sf);
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

        sessionFactoryControl.verify();
        sessionControl.verify();
        xaResControl.verify();
    }

    public void testTransactionRollbackOnlyWithPrebound() throws Exception {
        MockControl sessionFactoryControl = MockControl.createControl(SessionFactory.class);
        final SessionFactory sf = (SessionFactory) sessionFactoryControl.getMock();
        MockControl sessionControl = MockControl.createControl(XASession.class);
        final XASession session = (XASession) sessionControl.getMock();

        MockControl xaResourceControl = MockControl.createControl(XAResource.class);
        XAResource xaResource = (XAResource) xaResourceControl.getMock();

        sessionControl.expectAndReturn(session.getXAResource(), xaResource);
        session.save();

        sessionFactoryControl.replay();
        sessionControl.replay();
        xaResourceControl.replay();

        PlatformTransactionManager tm = new LocalTransactionManager(sf);
        TransactionTemplate tt = new TransactionTemplate(tm);
        UserTxSessionHolder uTx = new UserTxSessionHolder(session);
        TransactionSynchronizationManager.bindResource(sf, uTx);

        assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
        uTx.setRollbackOnly();

        try {
            tt.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
                    JcrTemplate template = new JcrTemplate(sf);
                    template.save();
                }
            });

        } catch (UnexpectedRollbackException e) {
            System.out.println(e);
        }

        assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
        TransactionSynchronizationManager.unbindResource(sf);
        assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

        sessionFactoryControl.verify();
        sessionControl.verify();
        xaResourceControl.verify();
    }

    /**
     * Simple mock which overrides equals.
     * @author Costin Leau
     * @author Sergio Bossa
     * @author Salvatore Incandela
     */
    protected class XidMock implements Xid {
        /**
         * @see javax.transaction.xa.Xid#getBranchQualifier()
         */
        public byte[] getBranchQualifier() {
            return null;
        }

        /**
         * @see javax.transaction.xa.Xid#getFormatId()
         */
        public int getFormatId() {
            return 0;
        }

        /**
         * @see javax.transaction.xa.Xid#getGlobalTransactionId()
         */
        public byte[] getGlobalTransactionId() {
            return null;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            return true;
        }

    }

}
