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

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;

/**
 * @author Costin Leau
 * @author Sergio Bossa
 * @author Salvatore Incandela
 */
public class TxStressTest extends AbstractTransactionalSpringContextTests {

    private JcrTemplate template;

    protected String[] getConfigLocations() {
        return new String[] { "txStressTestApplicationContext.xml" };
    }

    public void testMultipleCommits() {
        for (int i = 0; i < 100; i++) {
            endTransaction();
            startNewTransaction();
            template.execute(new JcrCallback() {

                public Object doInJcr(Session session) throws IOException, RepositoryException {
                    Node rootNode = session.getRootNode();
                    Node one = rootNode.addNode("bla-bla-bla");
                    one.setProperty("some prop", false);
                    Node two = one.addNode("foo");
                    two.setProperty("boo", "hoo");
                    Node three = two.addNode("bar");
                    three.setProperty("whitehorse", new String[] { "super", "ultra", "mega" });
                    session.save();
                    return null;
                }
            });
            setComplete();
            endTransaction();

        }

    }

    public void setTemplate(JcrTemplate template) {
        this.template = template;
    }
}
