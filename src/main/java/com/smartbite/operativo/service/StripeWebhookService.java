package com.smartbite.operativo.service;

public interface StripeWebhookService {

    void procesarWebhook(String payload, String signature);
}