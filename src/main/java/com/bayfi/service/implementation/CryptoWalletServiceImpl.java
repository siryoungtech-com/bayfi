package com.bayfi.service.implementation;

import com.bayfi.dto.response.MasterWalletResponse;
import com.bayfi.entity.MasterWallet;
import com.bayfi.enums.BlockChainType;
import com.bayfi.repository.MasterWalletRepository;
import com.bayfi.service.TatumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class CryptoWalletServiceImpl implements TatumService {
    private final static Logger logger = LoggerFactory.getLogger(CryptoWalletServiceImpl.class);

    private final WebClient tatumWebClient;
    private final MasterWalletRepository masterWalletRepository;

    public CryptoWalletServiceImpl(@Qualifier("tatumWebClient") WebClient tatumWebClient, MasterWalletRepository masterWalletRepository) {
        this.tatumWebClient = tatumWebClient;
        this.masterWalletRepository = masterWalletRepository;
    }

    @Override
    public Mono<MasterWallet> generateMasterWallet(BlockChainType blockchainType) {
        // Check if master wallet already exists for this blockchain
        Optional<MasterWallet> existingWallet = masterWalletRepository.findByBlockchainType(blockchainType.name());
        if (existingWallet.isPresent()) {
            logger.info("Master wallet for {} already exists", blockchainType);
            return Mono.just(existingWallet.get());
        }

        // Determine the correct URI path based on blockchain type
        String uriPath = getUriPathForBlockchain(blockchainType);

        // Create new wallet
        return tatumWebClient.get()
                .uri(uriPath)
                .retrieve()
                .bodyToMono(MasterWalletResponse.class)
                .map(response -> {
                    MasterWallet wallet = new MasterWallet();
                    wallet.setBlockchainType(blockchainType.name());
                    wallet.setEncryptedMnemonic(response.getMnemonic()); // Note: You'll need to encrypt this
                    wallet.setXPub(response.getXPub());
                    wallet.setAddress(response.getAddress());
                    wallet.setPrivateKey(response.getPrivateKey()); // Note: Encrypt with this

//                    TODO: IMPLEMENT KMS

                    // Save to database
                    return masterWalletRepository.save(wallet);

                })
                .doOnSuccess(wallet -> logger.info("Successfully generated master wallet for {}", blockchainType))
                .doOnError(error -> logger.error("Error generating master wallet for {}: {}", blockchainType, error.getMessage()));
    }

    private String getUriPathForBlockchain(BlockChainType blockchainType) {
        return switch (blockchainType) {
            case BITCOIN -> "/bitcoin/wallet";
            case BSC -> "/bsc/wallet";
            case ETHEREUM -> "/ethereum/wallet?testnetType=ethereum-sepolia";
            case SOLANA -> "/solana/wallet";
            case TRON -> "/tron/wallet";
            default -> throw new IllegalArgumentException("Unsupported blockchain type: " + blockchainType);
        };
    }

    private String encryptWithKMS(String data){
        return null;
    }

}

