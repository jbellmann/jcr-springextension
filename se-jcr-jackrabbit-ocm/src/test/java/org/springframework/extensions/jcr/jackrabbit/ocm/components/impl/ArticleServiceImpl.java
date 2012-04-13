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
package org.springframework.extensions.jcr.jackrabbit.ocm.components.impl;

import java.util.Collection;
import java.util.Date;

import org.apache.jackrabbit.ocm.query.Filter;
import org.apache.jackrabbit.ocm.query.Query;
import org.apache.jackrabbit.ocm.query.QueryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.jcr.jackrabbit.ocm.JcrMappingTemplate;
import org.springframework.extensions.jcr.jackrabbit.ocm.components.ArticleService;
import org.springframework.extensions.jcr.jackrabbit.ocm.components.NewsService;
import org.springframework.extensions.jcr.jackrabbit.ocm.model.Article;
import org.springframework.extensions.jcr.jackrabbit.ocm.model.News;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation for
 * {@link org.springframework.extensions.jcr.jackrabbit.ocm.components.ArticleService}
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Lombart Christophe </a>
 */
@Service
@Transactional(propagation = Propagation.REQUIRED)
public class ArticleServiceImpl implements ArticleService {
    private static final Logger logger = LoggerFactory.getLogger(ArticleServiceImpl.class);

    private final JcrMappingTemplate jcrMappingtemplate;
    private final NewsService newsService;

    @Autowired
    public ArticleServiceImpl(NewsService newsService, JcrMappingTemplate template) {
        this.jcrMappingtemplate = template;
        this.newsService = newsService;
    }

    @Override
    public void createArticle(Article article) {

        jcrMappingtemplate.insert(article);
        jcrMappingtemplate.save();
        logger.debug("A new article has been created by " + article.getAuthor());
        News news = new News();
        news.setContent("A new article has been created by " + article.getAuthor());
        news.setCreationDate(new Date());
        news.setPath("/news-" + System.currentTimeMillis());
        newsService.createNews(news);

    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public Collection<Article> getArticles() {

        QueryManager queryManager = jcrMappingtemplate.createQueryManager();
        Filter filter = queryManager.createFilter(Article.class);

        Query query = queryManager.createQuery(filter);
        // NOTE: To get around casts and unchecked warning use execute with callback.
        return jcrMappingtemplate.getObjects(query);
    }
}
