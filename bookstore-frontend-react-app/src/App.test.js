import { render, screen } from '@testing-library/react';
import App from './App';

jest.mock('./components/Header', () => () => <div>Bookstore Header</div>);
jest.mock('./components/Footer', () => () => <div>Bookstore Footer</div>);

jest.mock('./screens/UserListScreen', () => () => <div>Mock User List Screen</div>);
jest.mock('./screens/UserEditScreen', () => () => <div>Mock User Edit Screen</div>);
jest.mock('./screens/HomeScreen', () => () => <div>Mock Home Screen</div>);
jest.mock('./screens/ProductScreen', () => () => <div>Mock Product Screen</div>);
jest.mock('./screens/CartScreen', () => () => <div>Mock Cart Screen</div>);
jest.mock('./screens/LoginScreen', () => () => <div>Mock Login Screen</div>);
jest.mock('./screens/RegisterScreen', () => () => <div>Mock Register Screen</div>);
jest.mock('./screens/ProfileScreen', () => () => <div>Mock Profile Screen</div>);
jest.mock('./screens/ShippingScreen', () => () => <div>Mock Shipping Screen</div>);
jest.mock('./screens/PaymentScreen', () => () => <div>Mock Payment Screen</div>);
jest.mock('./screens/PlaceOrderScreen', () => () => <div>Mock Place Order Screen</div>);
jest.mock('./screens/OrderScreen', () => () => <div>Mock Order Screen</div>);
jest.mock('./screens/ProductListScreen', () => () => <div>Mock Product List Screen</div>);
jest.mock('./screens/ProductEditScreen', () => () => <div>Mock Product Edit Screen</div>);
jest.mock('./screens/ProductCreateScreen', () => () => <div>Mock Product Create Screen</div>);
jest.mock('./screens/OrderListScreen', () => () => <div>Mock Order List Screen</div>);

test('renders the bookstore home route and shared layout', () => {
  window.history.pushState({}, 'Home', '/');

  render(<App />);

  expect(screen.getByText('Bookstore Header')).toBeInTheDocument();
  expect(screen.getByText('Mock Home Screen')).toBeInTheDocument();
  expect(screen.getByText('Bookstore Footer')).toBeInTheDocument();
});
