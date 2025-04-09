package com.bayfi.dto.response;

import lombok.Data;

@Data
public class MasterWalletResponse {

    private String message;
    private String mnemonic;
    private String xPub;
    private String privateKey;
    private String address;
    private String blockChainType;
}
