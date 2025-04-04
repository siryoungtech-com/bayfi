package com.bayfi.service;
import com.bayfi.entity.MasterWallet;
import com.bayfi.enums.BlockChainType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public interface TatumService {
    Mono<MasterWallet> generateMasterWallet(BlockChainType blockchainType);
}
