package com.bankpro.service;

import com.bankpro.model.Account;
import com.bankpro.model.Customer;
import com.bankpro.model.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BankingService {

    private final ConcurrentHashMap<Long, Customer> customers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, List<Transaction>> transactions = new ConcurrentHashMap<>();

    private final AtomicLong customerSequence = new AtomicLong(2);
    private final AtomicLong accountSequence = new AtomicLong(2);
    private final AtomicLong transactionSequence = new AtomicLong(2);

    public BankingService() {
        Customer customer = new Customer(1L, "John Doe", "john.doe@bankpro.com", "+1-555-0100");
        customers.put(1L, customer);

        Account account = new Account(1L, 1L, "BP10000001", "SAVINGS",
                new BigDecimal("5000.00"));
        accounts.put(1L, account);

        Transaction transaction = new Transaction(
                1L, 1L, "DEPOSIT", new BigDecimal("5000.00"), LocalDateTime.now());
        transactions.put(1L, new ArrayList<>(List.of(transaction)));
    }

    public List<Customer> getCustomers() {
        return new ArrayList<>(customers.values());
    }

    public Customer getCustomer(Long id) {
        return customers.get(id);
    }

    public Customer createCustomer(Customer customer) {
        long id = customerSequence.incrementAndGet();
        customer.setId(id);
        customers.put(id, customer);
        return customer;
    }

    public List<Account> getAccounts() {
        return new ArrayList<>(accounts.values());
    }

    public Account getAccount(Long id) {
        return accounts.get(id);
    }

    public Account createAccount(Account account) {
        long id = accountSequence.incrementAndGet();
        account.setId(id);
        accounts.put(id, account);
        return account;
    }

    public List<Transaction> getTransactions(Long accountId) {
        return transactions.getOrDefault(accountId, new ArrayList<>());
    }

    public Transaction createTransaction(Long accountId, String type, BigDecimal amount) {
        Account account = accounts.get(accountId);
        if (account == null) {
            return null;
        }

        BigDecimal current = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
        if ("WITHDRAW".equalsIgnoreCase(type)) {
            if (current.compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient account balance");
            }
            account.setBalance(current.subtract(amount));
        } else {
            account.setBalance(current.add(amount));
        }

        long id = transactionSequence.incrementAndGet();
        Transaction tx = new Transaction(id, accountId, type.toUpperCase(),
                amount, LocalDateTime.now());
        transactions.computeIfAbsent(accountId, k -> new ArrayList<>()).add(tx);
        return tx;
    }
}
