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
package org.springframework.extensions.jcr.jackrabbit.ocm.components;

import java.util.Collection;

import org.springframework.extensions.jcr.jackrabbit.ocm.model.News;
import org.springframework.transaction.annotation.Transactional;

/**
 * VERY Simple news management
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Lombart Christophe </a>
 */
public interface NewsService {

    void createNews(News news);

    @Transactional(readOnly = true)
    Collection<News> getNews();
}
