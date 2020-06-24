package org.apache.unomi.operation;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.component.kafka.KafkaConfiguration;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.apache.camel.core.osgi.OsgiDefaultCamelContext;
import org.apache.camel.impl.JndiRegistry;
import org.apache.unomi.api.Event;
import org.apache.unomi.operation.actions.BufferEventProcessingAction;
import org.apache.unomi.operation.router.EventContextProducer;
import org.apache.unomi.operation.segment.SegmentListener;
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
    private static Logger logger = LoggerFactory.getLogger(EventContextProducer.class);

    private Map<String, String> kafkaProps;

//    RedisStateRepository repository;
//    private String redisCluster;
//    private SegmentListener segmentListener;

    final private static String SEGMENT_OPT = "kafka-consume-segmentOpt";

    public void initCamelContext() throws Exception {
        camelContext = new OsgiDefaultCamelContext(bundleContext);
//        repository = new RedisStateRepository(redisCluster);

        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
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
                    KafkaComponent kafka = new KafkaComponent(this.getContext());
                    kafka.setBrokers(uriBuilder.toString());
                    kafka.setConfiguration(kafkaConfiguration);

                    KafkaEndpoint endpoint = new KafkaEndpoint(uriBuilder.toString(), kafka);
                    endpoint.setConfiguration(kafkaConfiguration);
                    from("direct:kafkaRoute").marshal(objectMapper).log("Send to DataOperation: ${body}").to(endpoint);
                }
            });
//            camelContext.addRoutes(new RouteBuilder() {
//                @Override
//                public void configure() throws Exception {
//                    StringBuilder uriBuilder = new StringBuilder("kafka:");
//                    StringBuilder kafkaOptions = new StringBuilder();
//                    KafkaConfiguration kafkaConfiguration = new KafkaConfiguration();
//
//                    for (Map.Entry<String, String> entry : kafkaProps.entrySet()) {
//                        if (entry.getKey().equals("topic")) {
//                            kafkaConfiguration.setTopic("segmentOpt");
//                            uriBuilder.append(entry.getValue());
//                            continue;
//                        }
//                        if (entry.getKey().equals("brokers")) {
//                            kafkaConfiguration.setBrokers(entry.getValue());
//                        }
//                        kafkaOptions.append(entry.getKey()).append("=").append(entry.getValue());
//                    }
//                    kafkaOptions.append("offsetRepository=#offsetRepo");
//                    kafkaOptions.append("groupId=").append(SEGMENT_OPT);
//                    String kafkaEndpointURI = uriBuilder.append("?").append(kafkaOptions.toString()).toString();
//                    KafkaComponent kafka = new KafkaComponent(this.getContext());
//                    kafka.setBrokers(kafkaEndpointURI);
//                    kafka.setConfiguration(kafkaConfiguration);
//                    KafkaEndpoint endpoint = new KafkaEndpoint(kafkaEndpointURI, kafka);
//                    kafkaConfiguration.setOffsetRepository(repository);
//                    endpoint.setConfiguration(kafkaConfiguration);
//                    from(endpoint).routeId("fromDataRouterKafka").process(segmentListener).log("Message received from Kafka : ${body}");
//                }
//            });
            camelContext.start();
            logger.info("KAFKA start context");
        } catch (Exception e) {
            logger.error("KAFKA error", e);
            e.printStackTrace();
        }
    }

    public void preDestroy() throws Exception {
        bundleContext.removeBundleListener(this);
        //This is to shutdown Camel context
        //(will stop all routes/components/endpoints etc and clear internal state/cache)
        this.camelContext.stop();
//        this.repository.stop();
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public void bundleChanged(BundleEvent bundleEvent) {

    }

    public void setKafkaProps(Map<String, String> kafkaProps) {
        this.kafkaProps = kafkaProps;
    }


    public BufferEventProcessingAction.EventBuffer getProducer() {
        return new KafkaEventBufferProducer(camelContext.createProducerTemplate());
    }

    public void setObjectMapper(JacksonDataFormat objectMapper) {
        this.objectMapper = objectMapper;
    }

//    public void setSegmentListener(SegmentListener segmentListener) {
//        this.segmentListener = segmentListener;
//    }
//
//    public void setRedisCluster(String redisCluster) {
//        this.redisCluster = redisCluster;
//    }

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
