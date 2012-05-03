package org.springframework.extensions.jcr.annotation;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

@EventListenerDefinition(uuids = {"test"})
public class AnnotatedEventListener implements EventListener {

    @Override
    public void onEvent(EventIterator events) {
        // do nothing, just for testing
    }

}
