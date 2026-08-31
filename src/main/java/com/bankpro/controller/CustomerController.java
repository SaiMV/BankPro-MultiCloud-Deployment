package com.bankpro.controller;

import com.bankpro.model.Customer;
import com.bankpro.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final BankingService bankingService;

    public CustomerController(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    @GetMapping
    public List<Customer> getCustomers() {
        return bankingService.getCustomers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
        Customer customer = bankingService.getCustomer(id);
        return customer == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(customer);
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bankingService.createCustomer(customer));
    }
}
