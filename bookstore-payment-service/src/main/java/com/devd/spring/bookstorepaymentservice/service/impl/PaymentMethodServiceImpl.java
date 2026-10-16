package com.devd.spring.bookstorepaymentservice.service.impl;

import com.devd.spring.bookstorecommons.exception.RunTimeExceptionPlaceHolder;
import com.devd.spring.bookstorepaymentservice.repository.UserPaymentCustomerRepository;
import com.devd.spring.bookstorepaymentservice.repository.dao.UserPaymentCustomer;
import com.devd.spring.bookstorepaymentservice.service.PaymentMethodService;
import com.devd.spring.bookstorepaymentservice.web.CreatePaymentMethodRequest;
import com.devd.spring.bookstorepaymentservice.web.GetPaymentMethodResponse;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.devd.spring.bookstorecommons.util.CommonUtilityMethods.getUserIdFromToken;
import static com.devd.spring.bookstorecommons.util.CommonUtilityMethods.getUserNameFromToken;

/**
 * Payment method service implementation.
 */
@Service
@Slf4j
public class PaymentMethodServiceImpl implements PaymentMethodService {

    @Autowired
    private UserPaymentCustomerRepository userPaymentCustomerRepository;

    public PaymentMethodServiceImpl() {
    }

    @Override
    public void createPaymentMethod(
            CreatePaymentMethodRequest createPaymentMethodRequest) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String userIdFromToken = getUserIdFromToken(authentication);
        String userNameFromToken = getUserNameFromToken(authentication);

        UserPaymentCustomer paymentCustomer =
                userPaymentCustomerRepository.findByUserId(userIdFromToken);

        // Retrieve and validate the PaymentMethod created by Stripe.js
        // before creating a Stripe customer or changing customer mappings.
        String paymentMethodId = createPaymentMethodRequest.getPaymentMethodId();
        PaymentMethod paymentMethod = retrieveCardPaymentMethod(paymentMethodId);

        String existingCustomerId = paymentMethod.getCustomer();

        if (existingCustomerId != null && !existingCustomerId.trim().isEmpty()) {
            if (paymentCustomer == null
                    || !existingCustomerId.equals(paymentCustomer.getPaymentCustomerId())) {
                throw new RunTimeExceptionPlaceHolder(
                        "PaymentMethod is already attached to another customer.");
            }

            // Already attached to this authenticated user's customer.
            return;
        }

        String customerId;

        if (paymentCustomer == null) {
            customerId = createCustomerAtStripe();

            UserPaymentCustomer userPaymentCustomer = new UserPaymentCustomer();
            userPaymentCustomer.setUserId(userIdFromToken);
            userPaymentCustomer.setUserName(userNameFromToken);
            userPaymentCustomer.setPaymentCustomerId(customerId);
            userPaymentCustomerRepository.save(userPaymentCustomer);
        } else {
            customerId = paymentCustomer.getPaymentCustomerId();
        }

        linkCustomerAndPaymentMethod(paymentMethod, customerId);
    }

    @Override
    public List<GetPaymentMethodResponse> getAllMyPaymentMethods() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String userIdFromToken = getUserIdFromToken(authentication);

        List<GetPaymentMethodResponse> list = new ArrayList<>();

        UserPaymentCustomer paymentCustomer =
                userPaymentCustomerRepository.findByUserId(userIdFromToken);

        if (paymentCustomer != null) {

            PaymentMethodCollection paymentMethods =
                    getAllPaymentMethodsForCustomerFromStripe(
                            paymentCustomer.getPaymentCustomerId());

            paymentMethods.getData().forEach(pm -> {

                GetPaymentMethodResponse response =
                        GetPaymentMethodResponse.builder()
                                .paymentMethodId(pm.getId())
                                .cardCountry(pm.getCard().getCountry())
                                .cardExpirationMonth(pm.getCard().getExpMonth())
                                .cardExpirationYear(pm.getCard().getExpYear())
                                .cardLast4Digits(pm.getCard().getLast4())
                                .cardType(pm.getCard().getBrand())
                                .build();

                list.add(response);
            });
        }

        return list;
    }

    @Override
    public GetPaymentMethodResponse getMyPaymentMethodById(
            String paymentMethodId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String userIdFromToken = getUserIdFromToken(authentication);

        UserPaymentCustomer paymentCustomer =
                userPaymentCustomerRepository.findByUserId(userIdFromToken);

        if (paymentCustomer == null) {
            throw new RunTimeExceptionPlaceHolder(
                    "Payment customer not found for this user.");
        }

        try {
            PaymentMethod paymentMethod =
                    PaymentMethod.retrieve(paymentMethodId);

            // Ensure the payment method belongs to the authenticated user.
            if (!paymentCustomer.getPaymentCustomerId()
                    .equals(paymentMethod.getCustomer())) {

                throw new RunTimeExceptionPlaceHolder(
                        "PaymentMethod doesn't belong to this User");
            }

            return GetPaymentMethodResponse.builder()
                    .paymentMethodId(paymentMethod.getId())
                    .cardCountry(paymentMethod.getCard().getCountry())
                    .cardExpirationMonth(paymentMethod.getCard().getExpMonth())
                    .cardExpirationYear(paymentMethod.getCard().getExpYear())
                    .cardLast4Digits(paymentMethod.getCard().getLast4())
                    .cardType(paymentMethod.getCard().getBrand())
                    .build();

        } catch (StripeException e) {
            logStripeException("Retrieving payment method", e);

            throw new RunTimeExceptionPlaceHolder(
                    "Error while fetching payment method.");
        }
    }

    private PaymentMethodCollection getAllPaymentMethodsForCustomerFromStripe(
            String paymentCustomerId) {

        Map<String, Object> params = new HashMap<>();
        params.put("customer", paymentCustomerId);
        params.put("type", "card");

        try {
            return PaymentMethod.list(params);

        } catch (StripeException e) {
            logStripeException("Listing customer payment methods", e);

            throw new RunTimeExceptionPlaceHolder(
                    "Error while retrieving customer.");
        }
    }

    private PaymentMethod retrieveCardPaymentMethod(String paymentMethodId) {
        if (paymentMethodId == null
                || !paymentMethodId.matches("pm_[A-Za-z0-9]+")) {
            throw new RunTimeExceptionPlaceHolder(
                    "A valid Stripe PaymentMethod ID is required.");
        }

        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

            if (!"card".equals(paymentMethod.getType()) || paymentMethod.getCard() == null) {
                throw new RunTimeExceptionPlaceHolder(
                        "Only card PaymentMethods are supported.");
            }

            return paymentMethod;
        } catch (StripeException e) {
            logStripeException("Retrieving submitted PaymentMethod", e);
            throw new RunTimeExceptionPlaceHolder(
                    "Unable to retrieve the submitted PaymentMethod.");
        }
    }

    private void linkCustomerAndPaymentMethod(
            PaymentMethod paymentMethod,
            String customerId) {

        String existingCustomerId = paymentMethod.getCustomer();

        if (existingCustomerId != null && !existingCustomerId.trim().isEmpty()) {
            if (existingCustomerId.equals(customerId)) {
                return;
            }

            throw new RunTimeExceptionPlaceHolder(
                    "PaymentMethod is already attached to another customer.");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("customer", customerId);

        try {
            paymentMethod.attach(params);
        } catch (StripeException e) {
            logStripeException("Attaching payment method to customer", e);
            throw new RunTimeExceptionPlaceHolder(
                    "Error while attaching payment method.");
        }
    }

    private String createCustomerAtStripe() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String userIdFromToken = getUserIdFromToken(authentication);

        Map<String, Object> params = new HashMap<>();

        params.put(
                "description",
                "Creating Customer Account for UserId : " + userIdFromToken);

        try {
            return Customer.create(params).getId();

        } catch (StripeException e) {
            logStripeException("Creating Stripe customer", e);

            throw new RunTimeExceptionPlaceHolder(
                    "Error while setting up payment customer.");
        }
    }

    /**
     * Logs diagnostic metadata without logging card information,
     * API keys, raw exception messages, or request/response bodies.
     */
    private void logStripeException(String operation, StripeException e) {

        String parameter = null;
        String declineCode = null;

        if (e instanceof CardException) {
            CardException cardException = (CardException) e;

            parameter = cardException.getParam();
            declineCode = cardException.getDeclineCode();
        }

        log.error(
                "Stripe operation failed: operation={}, exceptionType={}, "
                        + "stripeErrorCode={}, declineCode={}, parameter={}, "
                        + "httpStatus={}, requestId={}",
                operation,
                e.getClass().getSimpleName(),
                e.getCode(),
                declineCode,
                parameter,
                e.getStatusCode(),
                e.getRequestId());
    }
}