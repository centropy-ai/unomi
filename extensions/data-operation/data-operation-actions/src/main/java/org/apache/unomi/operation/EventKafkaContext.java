package org.apache.unomi.operation;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
    String producerTopics = "";
    String consumerTopics = "";

    public void initCamelContext() throws Exception {
        camelContext = new OsgiDefaultCamelContext(bundleContext);
        bundleContext.addBundleListener(this);

        StringBuilder kafkaOptions = new StringBuilder();
        KafkaConfiguration kafkaConfiguration = new KafkaConfiguration();
        for (Map.Entry<String, String> entry : kafkaProps.entrySet()) {
            if (entry.getKey().equals("producerTopics")) {
                producerTopics = entry.getValue();
                continue;
            }
            if (entry.getKey().equals("consumerTopics")) {
                consumerTopics = entry.getValue();
                kafkaConfiguration.setTopic(entry.getValue());
                continue;
            }
            if (entry.getKey().equals("brokers")) {
                kafkaConfiguration.setBrokers(entry.getValue());
                continue;
            }
            if (entry.getKey().equals("groupId") && entry.getValue().length() > 0) {
                kafkaConfiguration.setGroupId(entry.getValue());
                continue;
            }
            if (entry.getValue().length() > 0) {
                kafkaOptions.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        kafkaOptions.append("enableIdempotence=true");

        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    StringBuilder producerURIBuilder = new StringBuilder("kafka:" + producerTopics);

                    kafkaConfiguration.setTopic(producerTopics);
                    producerURIBuilder.append("?").append(kafkaOptions.toString());
                    KafkaComponent kafka = new KafkaComponent(this.getContext());
                    kafka.setConfiguration(kafkaConfiguration);
                    KafkaEndpoint endpoint = new KafkaEndpoint(producerURIBuilder.toString(), kafka);
                    endpoint.setConfiguration(kafkaConfiguration);
                    this.from("direct:kafkaRoute").marshal(objectMapper).to(endpoint).log("Send to Kafka: ${body}");
                }
            });
            if (!nodeType.toUpperCase().equals("MASTER")) {
                camelContext.addRoutes(new RouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        StringBuilder consumerURIBuilder = new StringBuilder("kafka:" + consumerTopics);

                        kafkaConfiguration.setTopic(consumerTopics);
                        consumerURIBuilder.append("?").append(kafkaOptions.toString());
                        KafkaComponent kafka = new KafkaComponent(this.getContext());
                        kafka.setConfiguration(kafkaConfiguration);
                        KafkaEndpoint endpoint = new KafkaEndpoint(consumerURIBuilder.toString(), kafka);
                        endpoint.setConfiguration(kafkaConfiguration);
                        this.from(endpoint).unmarshal(objectMapper)
                                .process(eventFromKafkaListener)
                        ;
                    }
                });
            }
            camelContext.start();
            logger.info("KAFKA started");
        } catch (Exception e) {
            logger.error("KAFKA error", e);
            e.printStackTrace();
        }
    }

    public void preDestroy() throws Exception {
        bundleContext.removeBundleListener(this);
        this.camelContext.stop();
        this.initialized = false;
        logger.info("Destroy EventKafkaBundle");
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
        if (!this.initialized) {
            this.initialized = true;
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
        objectMapper.setEnableFeatures(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS.name());
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
