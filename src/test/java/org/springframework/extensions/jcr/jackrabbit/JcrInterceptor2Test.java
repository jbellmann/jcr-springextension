package org.springframework.extensions.jcr.jackrabbit;

import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
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
 */
public class JcrInterceptor2Test {

    private final SessionFactory sessionFactory = Mockito.mock(SessionFactory.class);
    private final Session session = Mockito.mock(Session.class, Mockito.withSettings()
            .extraInterfaces(XAResource.class));

    @Before
    public void setUp() throws RepositoryException, XAException {
        Mockito.when(sessionFactory.getSession()).thenReturn(session);
    }

    @Test
    public void testCreateSessionHolder() {
        JcrInterceptor interceptor = new JcrInterceptor();
        ListSessionHolderProviderManager manager = new ListSessionHolderProviderManager();
        List<SessionHolderProvider> providers = new ArrayList<SessionHolderProvider>();
        SessionHolderProvider provider = new JackRabbitSessionHolderProvider();
        providers.add(provider);
        manager.setProviders(providers);
        interceptor.setSessionFactory(sessionFactory);
        interceptor.afterPropertiesSet();

        SessionHolder holder = null;

        holder = provider.createSessionHolder(session);

        assertSame(session, holder.getSession());
    }

}
