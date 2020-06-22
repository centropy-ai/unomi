package org.apache.unomi.operation.router;

import org.apache.unomi.api.Event;
import org.apache.unomi.operation.EventProducer;
import org.apache.unomi.operation.actions.BufferEventProcessingAction;

public class EventContextProducer implements EventProducer {
    private BufferEventProcessingAction.EventBuffer producer;

    public EventContextProducer(BufferEventProcessingAction.EventBuffer producer) {
        this.producer = producer;
    }
    @Override
    public void send(Event e) {
        this.getProducer().sendBody("direct:kafkaRoute", e);
    }

    private BufferEventProcessingAction.EventBuffer getProducer() {
        return producer;
    }
}
