package net.codejava.utea.service;

import net.codejava.utea.entity.Account;

public interface AccountService {
    Account findByUsername(String username);
}
