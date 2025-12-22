package kz.itzhiti.orderservice.repository;

import kz.itzhiti.orderservice.model.CoinTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoinTransactionRepository extends JpaRepository<CoinTransaction, Long> {
    List<CoinTransaction> findByUserIdOrderByCreatedAtDesc(String userId);
    List<CoinTransaction> findByOrderId(Long orderId);
}

