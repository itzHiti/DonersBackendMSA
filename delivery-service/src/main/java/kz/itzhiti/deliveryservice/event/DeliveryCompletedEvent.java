package kz.itzhiti.deliveryservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryCompletedEvent {
    private Long orderId;
    private Long deliveryId;
    private LocalDateTime completedAt;
}

