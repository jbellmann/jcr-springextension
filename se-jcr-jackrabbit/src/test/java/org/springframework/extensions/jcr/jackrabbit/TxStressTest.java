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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;

/**
 * @author Costin Leau
 * @author Sergio Bossa
 * @author Salvatore Incandela
 */
@TestExecutionListeners(TransactionalTestExecutionListener.class)
@ContextConfiguration(locations = {"classpath:txStressTestApplicationContext.xml"})
public class TxStressTest extends AbstractJUnit4SpringContextTests {

    private JcrTemplate template;


    @Test
    public void testMultipleCommits() {
        for (int i = 0; i < 100; i++) {
//            endTransaction();
//            startNewTransaction();
            transactionalMethod();
//            setComplete();
//            endTransaction();

        }
    }

    @Transactional
    private void transactionalMethod() {

        template.execute(new JcrCallback<Void>() {

            public Void doInJcr(Session session) throws IOException, RepositoryException {
                Node rootNode = session.getRootNode();
                Node one = rootNode.addNode("bla-bla-bla");
                one.setProperty("some prop", false);
                Node two = one.addNode("foo");
                two.setProperty("boo", "hoo");
                Node three = two.addNode("bar");
                three.setProperty("whitehorse", new String[]{"super", "ultra", "mega"});
                session.save();
                return null;
            }
        });
    }

    @Autowired
    public void setTemplate(JcrTemplate template) {
        this.template = template;
    }
}
