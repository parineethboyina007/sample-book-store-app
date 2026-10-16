import React, { useEffect, useState } from 'react';
import {
  Button,
  Col,
  Form,
  InputGroup,
  ListGroup,
  Row,
  Spinner,
} from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import {
  CardElement,
  useElements,
  useStripe,
} from '@stripe/react-stripe-js';

import { savePaymentMethodIdToLocalStorage } from '../actions/orderActions';
import {
  getMyPaymentMethodsAction,
  savePaymentMethodAction,
} from '../actions/paymentActions';
import CheckoutSteps from '../components/CheckoutSteps';
import Loader from '../components/Loader';
import Message from '../components/Message';

const PaymentScreen = ({ history }) => {
  const stripe = useStripe();
  const elements = useElements();

  const order = useSelector((state) => state.order);
  const { shippingAddressId } = order;

  const [paymentMethodId, setPaymentMethodId] = useState('');
  const [message, setMessage] = useState(null);
  const [cardComplete, setCardComplete] = useState(false);
  const [isCreatingPaymentMethod, setIsCreatingPaymentMethod] =
    useState(false);

  const dispatch = useDispatch();

  const paymentMethodSave = useSelector(
    (state) => state.paymentMethodSave
  );

  const {
    loading: saveLoading,
    error: saveError,
  } = paymentMethodSave;

  const paymentMethodListMy = useSelector(
    (state) => state.paymentMethodListMy
  );

  const {
    paymentMethods = [],
    loading: listLoading,
    error: listError,
  } = paymentMethodListMy;

  useEffect(() => {
    if (!shippingAddressId) {
      history.replace('/shipping');
      return;
    }

    dispatch(getMyPaymentMethodsAction());
  }, [dispatch, history, shippingAddressId]);

  const proceedToPlaceOrder = () => {
    if (!paymentMethodId) {
      setMessage('Please select or add a payment method.');
      return;
    }

    dispatch(savePaymentMethodIdToLocalStorage(paymentMethodId));
    history.push('/placeorder');
  };

  const saveCard = async (event) => {
    event.preventDefault();
    setMessage(null);

    if (!stripe || !elements) {
      setMessage('Payment system is loading. Please try again.');
      return;
    }

    const cardElement = elements.getElement(CardElement);

    if (!cardElement) {
      setMessage('Card input is unavailable. Please refresh the page.');
      return;
    }

    if (!cardComplete) {
      setMessage('Please enter complete and valid card details.');
      return;
    }

    setIsCreatingPaymentMethod(true);

    try {
      const { error, paymentMethod } =
        await stripe.createPaymentMethod({
          type: 'card',
          card: cardElement,
        });

      if (error) {
        setMessage(
          error.message || 'Unable to validate the card details.'
        );
        return;
      }

      if (!paymentMethod) {
        setMessage('Stripe did not return a payment method.');
        return;
      }

      // Send only the Stripe PaymentMethod ID to the backend.
      // Never send raw card details to the application server.
      dispatch(
        savePaymentMethodAction({
          paymentMethodId: paymentMethod.id,
        })
      );

      // Clear the card input after creating the PaymentMethod.
      cardElement.clear();
      setCardComplete(false);
    } catch (error) {
      setMessage(
        error.message || 'Unable to create the payment method.'
      );
    } finally {
      setIsCreatingPaymentMethod(false);
    }
  };

  const savingCard = saveLoading || isCreatingPaymentMethod;

  return (
    <>
      <CheckoutSteps step1 step2 step3 />

      <Row className="mx-5 justify-content-md-center">
        <h1 className="mx-5 justify-content-md-center">
          Payment Method
        </h1>
      </Row>

      <hr />

      {(saveError || listError) && (
        <Message variant="danger">
          {JSON.stringify(saveError || listError)}
        </Message>
      )}

      {message && (
        <Message variant="danger">{message}</Message>
      )}

      <Row>
        {/* Existing saved payment methods */}
        <Col xs={12} md={6}>
          {listLoading ? (
            <Loader />
          ) : (
            <>
              <h2>Select Payment Method</h2>

              {paymentMethods.length === 0 ? (
                <p>No saved payment methods. Add a new card.</p>
              ) : (
                <ListGroup variant="flush">
                  {paymentMethods.map((paymentMethod) => (
                    <ListGroup.Item
                      key={paymentMethod.paymentMethodId}
                    >
                      <InputGroup>
                        <Col md={1}>
                          <Form.Check
                            type="radio"
                            id={paymentMethod.paymentMethodId}
                            name="paymentMethod"
                            value={paymentMethod.paymentMethodId}
                            checked={
                              paymentMethod.paymentMethodId ===
                              paymentMethodId
                            }
                            onChange={() => {
                              setPaymentMethodId(
                                paymentMethod.paymentMethodId
                              );
                              setMessage(null);
                            }}
                            aria-label={`Select ${paymentMethod.cardType} ending in ${paymentMethod.cardLast4Digits}`}
                          />
                        </Col>

                        <Col>
                          <div
                            className="p-2"
                            role="button"
                            tabIndex={0}
                            style={{
                              whiteSpace: 'pre-wrap',
                              backgroundColor: '#eeeeee',
                              cursor: 'pointer',
                            }}
                            onClick={() => {
                              setPaymentMethodId(
                                paymentMethod.paymentMethodId
                              );
                              setMessage(null);
                            }}
                            onKeyDown={(event) => {
                              if (
                                event.key === 'Enter' ||
                                event.key === ' '
                              ) {
                                event.preventDefault();
                                setPaymentMethodId(
                                  paymentMethod.paymentMethodId
                                );
                                setMessage(null);
                              }
                            }}
                          >
                            <p
                              className="m-0"
                              style={{ textTransform: 'uppercase' }}
                            >
                              {paymentMethod.cardType}
                            </p>

                            <p className="m-0">
                              **** **** ****{' '}
                              {paymentMethod.cardLast4Digits} -{' '}
                              {paymentMethod.cardExpirationMonth} /{' '}
                              {paymentMethod.cardExpirationYear}
                            </p>
                          </div>
                        </Col>
                      </InputGroup>
                    </ListGroup.Item>
                  ))}
                </ListGroup>
              )}
            </>
          )}
        </Col>

        {/* Add a new card using Stripe Elements */}
        <Col xs={12} md={6}>
          <h2>Add New Card</h2>

          <Row className="mx-5 justify-content-md-center">
            <Col>
              <Form onSubmit={saveCard}>
                <Form.Group controlId="stripeCard">
                  <Form.Label>Card Details</Form.Label>

                  <div
                    className="p-3 border rounded"
                    style={{
                      backgroundColor: '#ffffff',
                      minHeight: '48px',
                    }}
                  >
                    <CardElement
                      options={{
                        hidePostalCode: true,
                        style: {
                          base: {
                            fontSize: '16px',
                            color: '#32325d',
                            fontFamily:
                              '-apple-system, BlinkMacSystemFont, Segoe UI, sans-serif',
                            '::placeholder': {
                              color: '#aab7c4',
                            },
                          },
                          invalid: {
                            color: '#dc3545',
                          },
                        },
                      }}
                      onChange={(event) => {
                        setCardComplete(event.complete);

                        if (event.error) {
                          setMessage(event.error.message);
                        } else {
                          setMessage(null);
                        }
                      }}
                    />
                  </div>

                  <Form.Text className="text-muted">
                    Your card details are collected securely by Stripe.
                  </Form.Text>
                </Form.Group>

                <Button
                  type="submit"
                  variant="primary"
                  disabled={
                    savingCard ||
                    !stripe ||
                    !elements ||
                    !cardComplete
                  }
                >
                  {savingCard ? (
                    <>
                      <Spinner
                        as="span"
                        animation="border"
                        size="sm"
                        role="status"
                        aria-hidden="true"
                      />{' '}
                      Adding Card...
                    </>
                  ) : (
                    'Add Card'
                  )}
                </Button>
              </Form>
            </Col>
          </Row>
        </Col>
      </Row>

      <hr />

      <Row className="mx-5 justify-content-md-center">
        <Button
          type="button"
          variant="primary"
          onClick={proceedToPlaceOrder}
          className="mt-3"
          disabled={!paymentMethodId}
        >
          Proceed to PlaceOrder
        </Button>
      </Row>
    </>
  );
};

export default PaymentScreen;