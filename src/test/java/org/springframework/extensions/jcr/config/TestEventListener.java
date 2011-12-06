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
public class TestEventListener implements EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(TestEventListener.class);

    @Override
    public void onEvent(EventIterator events) {
        LOG.info("GOT EVENTS: " + events.getSize());
    }

}
