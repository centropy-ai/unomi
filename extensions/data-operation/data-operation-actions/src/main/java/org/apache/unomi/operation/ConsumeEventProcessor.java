package org.apache.unomi.operation;

import org.apache.unomi.api.Event;

public interface ConsumeEventProcessor {
    void process(Event e);
}
