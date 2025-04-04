package com.bayfi.component;

import com.bayfi.enums.BlockChainType;
import com.bayfi.service.TatumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MasterWalletInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MasterWalletInitializer.class);

    private final TatumService tatumService;

    public MasterWalletInitializer(TatumService tatumService) {
        this.tatumService = tatumService;
    }



    @Override
    public void run(String... args) {
        logger.info("Initializing master wallets for all supported blockchains");

        // Generate master wallets for all blockchain types
        Flux.fromArray(BlockChainType.values())
                .flatMap(blockchainType ->
                        tatumService.generateMasterWallet(blockchainType)
                                .onErrorResume(e -> {
                                    logger.error("Failed to generate wallet for {}: {}",
                                            blockchainType, e.getMessage());
                                    return Mono.empty();
                                })
                )
                .collectList()
                .block(); // Block to ensure wallets are created before application proceeds

        logger.info("Master wallet initialization completed");
    }

}
