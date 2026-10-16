import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import store from './store';
import './bootstrap.min.css';
import './index.css';
import reportWebVitals from './reportWebVitals';
import { Provider } from 'react-redux';
import { Elements } from '@stripe/react-stripe-js';
import { stripePromise } from './stripe';

ReactDOM.render(
  <Provider store={store}>
    <React.StrictMode>
      {stripePromise ? (
        <Elements stripe={stripePromise}>
          <App />
        </Elements>
      ) : (
        <div className="container py-4">
          Payment configuration is missing. Please contact the administrator.
        </div>
      )}
    </React.StrictMode>
  </Provider>,
  document.getElementById('root')
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
