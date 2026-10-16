import { loadStripe } from '@stripe/stripe-js';

// Read the Stripe publishable key from the environment.
// This key is intended to be exposed to the frontend.
const publishableKey = process.env.REACT_APP_STRIPE_PUBLISHABLE_KEY;

export const stripePromise = publishableKey
  ? loadStripe(publishableKey)
  : null;
