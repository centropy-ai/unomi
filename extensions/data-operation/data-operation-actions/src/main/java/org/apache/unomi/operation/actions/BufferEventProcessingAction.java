package org.apache.unomi.operation.actions;

import org.apache.unomi.api.Event;
import org.apache.unomi.api.actions.Action;
import org.apache.unomi.api.actions.ActionExecutor;
import org.apache.unomi.api.services.EventService;
import org.apache.unomi.operation.EventKafkaContext;
import org.apache.unomi.operation.EventProducer;
import org.apache.unomi.operation.router.EventContextProducer;

public class BufferEventProcessingAction implements ActionExecutor {
    private EventProducer producer;

    private EventKafkaContext context;

    public interface EventBuffer {
        void sendBody(String to, Event data);
    }

    @Override
    public int execute(Action action, Event event) {
        this.getProducer().send(event);
        return EventService.NO_CHANGE;
    }

    public EventProducer getProducer() {
        if (this.producer == null)
            this.producer = new EventContextProducer(this.context.getProducer());
        return this.producer;
    }

    public void setContext(EventKafkaContext context) {
        this.context = context;
    }
}
