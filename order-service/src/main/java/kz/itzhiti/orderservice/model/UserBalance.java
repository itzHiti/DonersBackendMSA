package kz.itzhiti.orderservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_balances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "doner_coins", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal donerCoins = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addCoins(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.donerCoins = this.donerCoins.add(amount);
        }
    }

    public void spendCoins(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0 && this.donerCoins.compareTo(amount) >= 0) {
            this.donerCoins = this.donerCoins.subtract(amount);
        }
    }

    public boolean hasEnoughCoins(BigDecimal amount) {
        return this.donerCoins.compareTo(amount) >= 0;
    }
}

