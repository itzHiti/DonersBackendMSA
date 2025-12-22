package kz.itzhiti.orderservice.service;

import kz.itzhiti.orderservice.model.CoinTransaction;
import kz.itzhiti.orderservice.model.UserBalance;
import kz.itzhiti.orderservice.model.enums.TransactionType;
import kz.itzhiti.orderservice.repository.CoinTransactionRepository;
import kz.itzhiti.orderservice.repository.UserBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserBalanceService {

    private final UserBalanceRepository userBalanceRepository;
    private final CoinTransactionRepository coinTransactionRepository;

    private static final BigDecimal COIN_EARN_PERCENTAGE = new BigDecimal("0.05"); // 5%

    /**
     * Получить баланс дкоинов пользователя
     */
    public UserBalance getUserBalance(String userId) {
        return userBalanceRepository.findByUserId(userId)
                .orElseGet(() -> createNewBalance(userId));
    }

    /**
     * Начислить дкоины за заказ (5% от суммы)
     */
    public void earnCoinsFromOrder(String userId, Long orderId, BigDecimal orderTotal) {
        UserBalance balance = getUserBalance(userId);
        BigDecimal coinsToEarn = orderTotal.multiply(COIN_EARN_PERCENTAGE)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        balance.addCoins(coinsToEarn);
        userBalanceRepository.save(balance);

        // Записать транзакцию
        CoinTransaction transaction = CoinTransaction.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(coinsToEarn)
                .type(TransactionType.EARNED)
                .description("Заработано 5% за заказ #" + orderId)
                .build();
        coinTransactionRepository.save(transaction);
    }

    /**
     * Потратить дкоины (для скидок)
     */
    public void spendCoins(String userId, Long orderId, BigDecimal amount, String description) {
        UserBalance balance = getUserBalance(userId);

        if (!balance.hasEnoughCoins(amount)) {
            throw new IllegalArgumentException("Недостаточно дкоинов. Есть: " + balance.getDonerCoins() + ", нужно: " + amount);
        }

        balance.spendCoins(amount);
        userBalanceRepository.save(balance);

        // Записать транзакцию
        CoinTransaction transaction = CoinTransaction.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(amount)
                .type(TransactionType.SPENT)
                .description(description != null ? description : "Потрачено")
                .build();
        coinTransactionRepository.save(transaction);
    }

    /**
     * История транзакций пользователя
     */
    public List<CoinTransaction> getUserTransactionHistory(String userId) {
        return coinTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * История транзакций заказа
     */
    public List<CoinTransaction> getOrderTransactions(Long orderId) {
        return coinTransactionRepository.findByOrderId(orderId);
    }

    /**
     * Вернуть дкоины (при отмене заказа)
     */
    public void refundCoins(String userId, Long orderId, BigDecimal amount, String description) {
        UserBalance balance = getUserBalance(userId);
        balance.addCoins(amount);
        userBalanceRepository.save(balance);

        CoinTransaction transaction = CoinTransaction.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(amount)
                .type(TransactionType.REFUNDED)
                .description(description != null ? description : "Возвращено")
                .build();
        coinTransactionRepository.save(transaction);
    }

    /**
     * Создать новый баланс для пользователя
     */
    private UserBalance createNewBalance(String userId) {
        UserBalance newBalance = UserBalance.builder()
                .userId(userId)
                .donerCoins(BigDecimal.ZERO)
                .build();
        return userBalanceRepository.save(newBalance);
    }
}

