package io.perfana.tinybank.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import io.perfana.tinybank.domain.Account;

@Service
public class AccountService {

    @Value("${remote.account.service.url}")
    private String remoteServiceUrl;

    private final RestTemplate restTemplate;

    public AccountService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Account getAccount(String userId) {
        String url = String.format("%s/account?userId=%s", remoteServiceUrl, userId);
        return restTemplate.getForObject(url, Account.class);
    }
}