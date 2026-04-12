package com.tju.unify.conv.transaction;



@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.tju.unify.conv.transaction.mapper")
public class ConvTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConvTransactionApplication.class);
    }
}
