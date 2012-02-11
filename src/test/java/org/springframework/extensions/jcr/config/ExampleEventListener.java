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
package org.springframework.extensions.jcr.config;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple EventListener for Testing.
 * 
 * @author Joerg Bellmann
 *
 */
public class ExampleEventListener implements EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleEventListener.class);

    @Override
    public void onEvent(EventIterator events) {
        LOG.info("GOT EVENTS: " + events.getSize());
    }

}
