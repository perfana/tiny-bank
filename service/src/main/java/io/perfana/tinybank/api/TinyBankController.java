package io.perfana.tinybank.api;

import io.perfana.tinybank.domain.AccountInfo;
import io.perfana.tinybank.domain.Transactions;
import io.perfana.tinybank.service.TinyBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;

@RestController
public class TinyBankController {

    private static final Logger logger = LoggerFactory.getLogger(TinyBankController.class);
    
    @Autowired
    private TinyBankService tinyBankService;

    @GetMapping("/accountInfo")
    public ResponseEntity<AccountInfo> accountInfo(@RequestParam String userId) {
        long startTimeMillis = System.currentTimeMillis();

        AccountInfo accountInfo = tinyBankService.retrieveAccountInfo(userId);
        logger.info("Retrieve account info for user: {} duration ms: {}", userId, System.currentTimeMillis() - startTimeMillis);
        return ResponseEntity.ok(accountInfo);
    }

    @GetMapping("/transactions")
    public ResponseEntity<Transactions> transactions(@RequestParam String userId) {
        long startTimeMillis = System.currentTimeMillis();

        Transactions transactions = tinyBankService.retrieveTransactions(userId);
        logger.info("Retrieve transactions for user: {} duration ms: {}", userId, System.currentTimeMillis() - startTimeMillis);
        return ResponseEntity.ok(transactions);
    }
}
