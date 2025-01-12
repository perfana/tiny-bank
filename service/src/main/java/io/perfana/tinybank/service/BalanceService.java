package io.perfana.tinybank.service;

import io.perfana.tinybank.domain.Balance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BalanceService {
    @Value("${remote.balance.service.url}")
    private String remoteServiceUrl;

    private final RestTemplate restTemplate;

    public BalanceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Balance getBalance(String accountNumber) {
        String url = String.format("%s/balance?accountNumber=%s", remoteServiceUrl, accountNumber);
        return restTemplate.getForObject(url, Balance.class);
    }

}
