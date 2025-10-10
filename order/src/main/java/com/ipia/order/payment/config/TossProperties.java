package com.ipia.order.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss")
public class TossProperties {

    private String baseUrl = "https://api.tosspayments.com";
    private String secretKey;
    private boolean enableRealCall = false;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isEnableRealCall() {
        return enableRealCall;
    }

    public void setEnableRealCall(boolean enableRealCall) {
        this.enableRealCall = enableRealCall;
    }
}


