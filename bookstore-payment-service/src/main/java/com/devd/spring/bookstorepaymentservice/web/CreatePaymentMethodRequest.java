package com.devd.spring.bookstorepaymentservice.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * Request containing a Stripe PaymentMethod ID created by Stripe.js.
 * Raw card details must never be sent to this backend endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentMethodRequest {

    @NotBlank
    private String paymentMethodId;
}
