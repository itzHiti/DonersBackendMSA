package kz.itzhiti.orderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.itzhiti.orderservice.dto.CoinTransactionDTO;
import kz.itzhiti.orderservice.dto.UserBalanceDTO;
import kz.itzhiti.orderservice.model.CoinTransaction;
import kz.itzhiti.orderservice.model.UserBalance;
import kz.itzhiti.orderservice.service.UserBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coins")
@RequiredArgsConstructor
@Tag(name = "Doner Coins", description = "Управление дкоинами (донер-коины)")
@SecurityRequirement(name = "bearer-jwt")
public class UserBalanceController {

    private final UserBalanceService userBalanceService;

    @GetMapping("/balance")
    @Operation(summary = "Получить баланс дкоинов", description = "Получить текущий баланс дкоинов пользователя")
    public ResponseEntity<UserBalanceDTO> getBalance(Authentication authentication) {
        String userId = getUsername(authentication);
        UserBalance balance = userBalanceService.getUserBalance(userId);

        UserBalanceDTO dto = UserBalanceDTO.builder()
                .userId(balance.getUserId())
                .donerCoins(balance.getDonerCoins())
                .createdAt(balance.getCreatedAt())
                .updatedAt(balance.getUpdatedAt())
                .build();

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/history")
    @Operation(summary = "История транзакций", description = "Получить историю всех транзакций дкоинов пользователя")
    public ResponseEntity<List<CoinTransactionDTO>> getTransactionHistory(Authentication authentication) {
        String userId = getUsername(authentication);
        List<CoinTransaction> transactions = userBalanceService.getUserTransactionHistory(userId);

        List<CoinTransactionDTO> dtos = transactions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/order/{orderId}/transactions")
    @Operation(summary = "Транзакции заказа", description = "Получить все транзакции дкоинов для конкретного заказа")
    public ResponseEntity<List<CoinTransactionDTO>> getOrderTransactions(@PathVariable Long orderId) {
        List<CoinTransaction> transactions = userBalanceService.getOrderTransactions(orderId);

        List<CoinTransactionDTO> dtos = transactions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    private String getUsername(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("preferred_username");
        }
        return authentication.getName();
    }

    private CoinTransactionDTO mapToDTO(CoinTransaction transaction) {
        return CoinTransactionDTO.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .orderId(transaction.getOrderId())
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

