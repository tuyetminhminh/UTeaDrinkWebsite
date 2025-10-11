package net.codejava.utea.service.impl;

import net.codejava.utea.entity.Account;
import net.codejava.utea.repository.AccountRepository;
import net.codejava.utea.service.AccountService;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository repo;

    public AccountServiceImpl(AccountRepository repo) {
        this.repo = repo;
    }

    @Override
    public Account findByUsername(String username) {
        return repo.findByUsername(username).orElse(null);
    }
}
