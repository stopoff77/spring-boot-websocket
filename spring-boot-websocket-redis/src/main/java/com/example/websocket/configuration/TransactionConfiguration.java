package com.example.websocket.configuration;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement(mode = AdviceMode.PROXY, proxyTargetClass = true)
@Configuration
public class TransactionConfiguration {

}
