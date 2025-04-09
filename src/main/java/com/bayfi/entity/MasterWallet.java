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

    private String xPub;

    private String address;

    private String privateKey; // MUST BE ENCRYPTED WITH KMS

    private String encryptedMnemonic; // MUST BE ENCRYPTED WITH KMS

    @CreationTimestamp
    private LocalDateTime createdAt;


}
