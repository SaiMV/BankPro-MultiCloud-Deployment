package com.bankpro.controller;

import com.bankpro.model.Account;
import com.bankpro.model.Transaction;
import com.bankpro.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final BankingService bankingService;

    public AccountController(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    @GetMapping
    public List<Account> getAccounts() {
        return bankingService.getAccounts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable Long id) {
        Account account = bankingService.getAccount(id);
        return account == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(account);
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account account) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bankingService.createAccount(account));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable Long id) {
        if (bankingService.getAccount(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bankingService.getTransactions(id));
    }

    @PostMapping("/{id}/transactions")
    public ResponseEntity<Transaction> createTransaction(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        String type = String.valueOf(request.getOrDefault("type", "DEPOSIT"));
        BigDecimal amount = new BigDecimal(String.valueOf(request.getOrDefault("amount", "0")));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Transaction transaction = bankingService.createTransaction(id, type, amount);
        return transaction == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
}
