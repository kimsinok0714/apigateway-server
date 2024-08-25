package com.mzc.apigateway_server.service;

import org.springframework.stereotype.Service;

import com.mzc.apigateway_server.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;


    public boolean existsByAccountIdAndToken(String accountId, String token) {
        return accountRepository.existsByAccountIdAndToken(accountId, token);
    }


}
