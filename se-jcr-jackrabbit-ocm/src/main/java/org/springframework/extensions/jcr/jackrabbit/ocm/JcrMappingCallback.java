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
package org.springframework.extensions.jcr.jackrabbit.ocm;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.apache.jackrabbit.ocm.exception.JcrMappingException;

/**
 * Callback interface for Jcr mapping code. To be used with JcrMappingTemplate's execute method, assumably
 * often as anonymous classes within a method implementation. The typical implementation will call
 * PersistenceManager.get/insert/remove/update to perform some operations on the repository.
 * @author Costin Leau
 */
public interface JcrMappingCallback<T> {

    /**
     * Called by {@link JcrMappingTemplate#execute} within an active PersistenceManager
     * {@link org.apache.jackrabbit.ocm.manager.ObjectContentManager}. It is not responsible for logging out
     * of the <code>Session</code> or handling transactions. Allows for returning a result object created
     * within the callback, i.e. a domain object or a collection of domain objects. A thrown
     * {@link RuntimeException} is treated as an application exeception; it is propagated to the caller of the
     * template.
     * @param manager
     * @return
     * @throws org.apache.jackrabbit.ocm.exception.JcrMappingException
     */
    public T doInJcrMapping(ObjectContentManager manager) throws JcrMappingException;
}
