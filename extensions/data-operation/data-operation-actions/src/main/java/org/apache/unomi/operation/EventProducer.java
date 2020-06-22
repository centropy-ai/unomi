package org.apache.unomi.operation;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.unomi.api.Event;

public interface EventProducer {
    void send(Event e);
}
