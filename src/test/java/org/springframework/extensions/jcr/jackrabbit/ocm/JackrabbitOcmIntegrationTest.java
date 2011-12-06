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

import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.jcr.jackrabbit.ocm.components.ArticleService;
import org.springframework.extensions.jcr.jackrabbit.ocm.components.NewsService;
import org.springframework.extensions.jcr.jackrabbit.ocm.model.Article;
import org.springframework.extensions.jcr.jackrabbit.ocm.model.News;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Test Mapper
 *
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Christophe Lombart</a>
 */
@ContextConfiguration
public class JackrabbitOcmIntegrationTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private NewsService newsService;

    @Test
    public void testComponents() {
        try {
            logger.info("Add article");
            Article article = new Article();
            article.setPath("/article");
            article.setAuthor("Christophe");
            article.setContent("This is an interesting content");
            article.setCreationDate(new Date());
            article.setDescription("This is the article description");
            article.setTitle("Article Title");

            articleService.createArticle(article);

            logger.info("Check News");
            for (News newsFound : newsService.getNews()) {
                logger.info("News found : " + newsFound.getContent());
            }
        } catch (Exception e) {
            logger.error("Failed", e);
            fail(e.getMessage());

        }

    }
}