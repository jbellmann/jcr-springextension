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

import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.SessionFactoryUtils;
import org.springframework.extensions.jcr.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Spring web HandlerInterceptor that binds a JCR Session to the thread for the entire processing of the
 * request. Intended for the "Open Session in View" pattern, i.e. to allow for lazy loading in web views
 * despite the original transactions already being completed.
 * <p>
 * This filter works similar to the AOP JcrInterceptor: It just makes JCR Sessions available via the thread.
 * It is suitable for non-transactional execution but also for middle tier transactions via
 * JcrTransactionManager or JtaTransactionManager. In the latter case, Sessions pre-bound by this filter will
 * automatically be used for the transactions.
 * <p>
 * In contrast to OpenSessionInViewFilter, this interceptor is set up in a Spring application context and can
 * thus take advantage of bean wiring. It derives from JcrAccessor to inherit common JCR configuration
 * properties.
 * @author Costin Leau
 * @author Sergio Bossa
 * @author Salvatore Incandela
 */
public class OpenSessionInViewInterceptor extends HandlerInterceptorAdapter implements InitializingBean {
    /**
     * Suffix that gets appended to the SessionFactory toString representation for the
     * "participate in existing persistence manager handling" request attribute.
     * @see #getParticipateAttributeName
     */
    public static final String PARTICIPATE_SUFFIX = ".PARTICIPATE";

    protected static final Logger LOG = LoggerFactory.getLogger(OpenSessionInViewInterceptor.class);

    private SessionFactory sessionFactory;

    /**
     * Set the JCR JcrSessionFactory that should be used to create Sessions.
     */
    public void setSessionFactory(SessionFactory sf) {
        this.sessionFactory = sf;
    }

    /**
     * Return the JCR JcrSessionFactory that should be used to create Sessions.
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws DataAccessException {

        if (TransactionSynchronizationManager.hasResource(getSessionFactory())) {
            // do not modify the Session: just mark the request
            // accordingly
            String participateAttributeName = getParticipateAttributeName();
            Integer count = (Integer) request.getAttribute(participateAttributeName);
            int newCount = (count != null) ? count + 1 : 1;
            request.setAttribute(getParticipateAttributeName(), newCount);
        }

        else {
            LOG.debug("Opening JCR session in OpenSessionInViewInterceptor");
            Session s = SessionFactoryUtils.getSession(getSessionFactory(), true);
            TransactionSynchronizationManager.bindResource(getSessionFactory(), getSessionFactory().getSessionHolder(s));
        }

        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws DataAccessException {

        String participateAttributeName = getParticipateAttributeName();
        Integer count = (Integer) request.getAttribute(participateAttributeName);
        if (count != null) {
            // do not modify the Session: just clear the marker
            if (count > 1) {
                request.setAttribute(participateAttributeName, count - 1);
            } else {
                request.removeAttribute(participateAttributeName);
            }
        }

        else {
            SessionHolder sesHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(getSessionFactory());
            LOG.debug("Closing JCR session in OpenSessionInViewInterceptor");
            SessionFactoryUtils.releaseSession(sesHolder.getSession(), getSessionFactory());
        }
    }

    /**
     * Return the name of the request attribute that identifies that a request is already filtered. Default
     * implementation takes the toString representation of the JcrSessionFactory instance and appends
     * ".FILTERED".
     * @see #PARTICIPATE_SUFFIX
     */
    protected String getParticipateAttributeName() {
        return getSessionFactory().toString() + PARTICIPATE_SUFFIX;
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (sessionFactory == null)
            throw new IllegalArgumentException("sessionFactory is required");
    }
}
