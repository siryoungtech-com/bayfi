package com.bayfi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Table(name = "crypto_wallets")
@Entity
public class MasterWallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String blockchainType; //e.g "BTC", "ETH", "SOL"

    @Column(nullable = false, unique = true)
    private String xPub;

    @Column(nullable = false)
    private String encryptedMnemonic; // ENCRYPTED WITH KMS

    @CreationTimestamp
    private LocalDateTime createdAt;


}
