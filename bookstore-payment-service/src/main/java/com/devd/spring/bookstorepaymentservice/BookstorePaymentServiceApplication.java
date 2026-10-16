package com.devd.spring.bookstorepaymentservice;

import com.stripe.Stripe;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.devd.spring"})
@EnableFeignClients(basePackages = {"com.devd.spring"})
@EnableDiscoveryClient
public class BookstorePaymentServiceApplication {

    public static void main(String[] args) {
        String stripeSecretKey = System.getenv("STRIPE_SECRET_KEY");

        if (stripeSecretKey == null || stripeSecretKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "STRIPE_SECRET_KEY environment variable is not configured"
            );
        }

        Stripe.apiKey = stripeSecretKey;

        SpringApplication.run(BookstorePaymentServiceApplication.class, args);
    }
}