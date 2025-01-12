package io.perfana.tinybank.api;

import io.perfana.tinybank.domain.AccountInfo;
import io.perfana.tinybank.domain.Transactions;
import io.perfana.tinybank.service.TinyBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class TinyBankController {

    private static final Logger logger = LoggerFactory.getLogger(TinyBankController.class);
    
    @Autowired
    private TinyBankService tinyBankService;

    @GetMapping("/accountInfo")
    public AccountInfo accountInfo(@RequestParam String userId) {
        long startTimeMillis = System.currentTimeMillis();
        AccountInfo accountInfo = tinyBankService.retrieveAccountInfo(userId);
        logger.info("Retrieve account info for user: {} duration ms: {}", userId, System.currentTimeMillis() - startTimeMillis);
        return accountInfo;
    }

    @GetMapping("/transactions")
    public Transactions transactions(@RequestParam String userId) {
        long startTimeMillis = System.currentTimeMillis();
        Transactions transactions = tinyBankService.retrieveTransactions(userId);
        logger.info("Retrieve transactions for user: {} duration ms: {}", userId, System.currentTimeMillis() - startTimeMillis);
        return transactions;
    }
}
