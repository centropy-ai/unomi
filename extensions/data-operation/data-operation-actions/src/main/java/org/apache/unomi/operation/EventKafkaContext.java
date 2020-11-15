package org.apache.unomi.operation;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.component.kafka.KafkaConfiguration;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.apache.camel.core.osgi.OsgiDefaultCamelContext;
import org.apache.unomi.api.Event;
import org.apache.unomi.operation.actions.BufferEventProcessingAction;
import org.apache.unomi.operation.processor.KafkaEventInjectorListener;
import org.apache.unomi.persistence.spi.CustomObjectMapper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EventKafkaContext implements SynchronousBundleListener {
    private OsgiDefaultCamelContext camelContext;

    private BundleContext bundleContext;
    private JacksonDataFormat objectMapper;
    private String nodeType;
    private static Logger logger = LoggerFactory.getLogger(EventKafkaContext.class);

    private Map<String, String> kafkaProps;

    private KafkaEventInjectorListener eventFromKafkaListener;
    private boolean initialized = false;

    public void initCamelContext() throws Exception {
        camelContext = new OsgiDefaultCamelContext(bundleContext);
        bundleContext.addBundleListener(this);
    }

    public void preDestroy() throws Exception {
        bundleContext.removeBundleListener(this);
        this.camelContext.stop();
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
        if (bundleEvent.getType() == BundleEvent.STARTED && !this.initialized) {
            this.initialized = true;
            StringBuilder uriBuilder = new StringBuilder("kafka:");
            StringBuilder kafkaOptions = new StringBuilder();
            KafkaConfiguration kafkaConfiguration = new KafkaConfiguration();
            for (Map.Entry<String, String> entry : kafkaProps.entrySet()) {
                if (entry.getKey().equals("topic")) {
                    kafkaConfiguration.setTopic(entry.getValue());
                    uriBuilder.append(entry.getValue());
                    continue;
                }
                if (entry.getKey().equals("brokers")) {
                    kafkaConfiguration.setBrokers(entry.getValue());
                }
                kafkaOptions.append(entry.getKey()).append("=").append(entry.getValue());
            }
            uriBuilder.append("?").append(kafkaOptions.toString());
            try {
                camelContext.addRoutes(new RouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        KafkaComponent kafka = new KafkaComponent(this.getContext());
                        kafka.setBrokers(uriBuilder.toString());
                        kafka.setConfiguration(kafkaConfiguration);

                        KafkaEndpoint endpoint = new KafkaEndpoint(uriBuilder.toString(), kafka);
                        endpoint.setConfiguration(kafkaConfiguration);
                        this.from("direct:kafkaRoute").marshal(objectMapper).to(endpoint);
                    }
                });
                logger.info("Node Type {}", nodeType);
                if (!nodeType.toUpperCase().equals("MASTER")) {
                    camelContext.addRoutes(new RouteBuilder() {
                        @Override
                        public void configure() throws Exception {
                            KafkaComponent kafka = new KafkaComponent(this.getContext());
                            kafka.setBrokers(uriBuilder.toString());
                            kafka.setConfiguration(kafkaConfiguration);
                            KafkaEndpoint endpoint = new KafkaEndpoint(uriBuilder.toString(), kafka);
                            endpoint.setConfiguration(kafkaConfiguration);
                            this.from(endpoint).unmarshal(objectMapper)
                                    .process(eventFromKafkaListener)
                            ;
                        }
                    });
                }
                camelContext.start();
                logger.info("KAFKA start context");
            } catch (Exception e) {
                logger.error("KAFKA error", e);
                e.printStackTrace();
            }
        }
    }

    public void setKafkaProps(Map<String, String> kafkaProps) {
        this.kafkaProps = kafkaProps;
    }


    public BufferEventProcessingAction.EventBuffer getBuffer() {
        return new KafkaEventBufferProducer(camelContext.createProducerTemplate());
    }

    public void setObjectMapper(JacksonDataFormat objectMapper) {
        this.objectMapper = objectMapper;
        ObjectMapper mapper = CustomObjectMapper.getObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.setObjectMapper(mapper);
    }

    public void setEventFromKafkaListener(KafkaEventInjectorListener eventFromKafkaListener) {
        this.eventFromKafkaListener = eventFromKafkaListener;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public static class KafkaEventBufferProducer implements BufferEventProcessingAction.EventBuffer {
        private ProducerTemplate producer;

        public KafkaEventBufferProducer(ProducerTemplate p) {
            this.producer = p;
        }

        @Override
        public void sendBody(String to, Event data) {
            this.producer.sendBody(to, data);
        }
    }
}
