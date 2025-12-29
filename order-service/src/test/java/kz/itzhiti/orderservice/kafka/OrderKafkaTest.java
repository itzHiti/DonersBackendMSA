package kz.itzhiti.orderservice.kafka;

import kz.itzhiti.orderservice.dto.OrderDTO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.kafka.test.EmbeddedKafkaBroker;


import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = "order-events"
)
class OrderKafkaTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    void sendOrderEvent_shouldBeConsumed() {

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setCustomerId("cust123");


        kafkaTemplate.send("order-events", orderDTO);


        Map<String, Object> props =
                KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderDTO.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        var consumer =
                new org.apache.kafka.clients.consumer.KafkaConsumer<String, OrderDTO>(props);

        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "order-events");

        ConsumerRecord<String, OrderDTO> record =
                KafkaTestUtils.getSingleRecord(consumer, "order-events");

        assertNotNull(record);
        assertEquals(1L, record.value().getId());
        assertEquals("cust123", record.value().getCustomerId());

        consumer.close();
    }
}
