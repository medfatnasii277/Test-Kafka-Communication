package crud_student.demo.service;

import com.example.product.protobuf.ProductEventProto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductEventPublisher {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    @Value("${kafka.topic.product-events}")
    private String topic;

    public void publishProductEvent(ProductEventProto.ProductEvent event) {
        kafkaTemplate.send(topic, event.toByteArray());
    }
} 