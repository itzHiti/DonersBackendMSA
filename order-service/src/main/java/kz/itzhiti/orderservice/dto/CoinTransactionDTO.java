package kz.itzhiti.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinTransactionDTO {

    private Long id;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("order_id")
    private Long orderId;

    private BigDecimal amount;

    private String type;

    private String description;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}

