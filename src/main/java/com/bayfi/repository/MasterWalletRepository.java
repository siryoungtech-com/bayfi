package com.bayfi.repository;

import com.bayfi.entity.MasterWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface MasterWalletRepository  extends JpaRepository<MasterWallet, Long> {
    Optional<MasterWallet> findByBlockchainType(String blockchainType);
}
