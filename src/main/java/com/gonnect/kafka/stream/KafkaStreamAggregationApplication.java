package com.gonnect.kafka.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class KafkaStreamAggregationApplication {

    @Autowired
    private InteractiveQueryService queryService;

    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamAggregationApplication.class, args);
    }

    @EnableBinding(KafkaStreamsProcessorX.class)
    public static class KafkaStreamsAggregateSampleApplication {

        @StreamListener("input")
        public void process(KStream<Object, DomainEvent> input) {
            ObjectMapper mapper = new ObjectMapper();
            Serde<DomainEvent> domainEventSerde = new JsonSerde<>(DomainEvent.class, mapper);

            input
                    .groupBy(
                            (s, domainEvent) -> domainEvent.boardUuid,
                            Serialized.with(null, domainEventSerde))
                    .aggregate(
                            String::new,
                            (s, domainEvent, board) -> board.concat(domainEvent.eventType),
                            Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("test-events-snapshots").withKeySerde(Serdes.String()).
                                    withValueSerde(Serdes.String())
                    );
        }
    }

    @RestController
    public class FooController {

        @RequestMapping("/events")
        public String events() {

            final ReadOnlyKeyValueStore<String, String> topFiveStore =
                    queryService.getQueryableStore("test-events-snapshots", QueryableStoreTypes.<String, String>keyValueStore());
            return topFiveStore.get("12345");
        }
    }

    interface KafkaStreamsProcessorX {

        @Input("input")
        KStream<?, ?> input();
    }

}
