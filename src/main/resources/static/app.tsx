import React from 'react';
import ReactDOM from 'react-dom/client';
import { App } from './components/App';
import './styles.css';

// API base URL - change this to match your backend URL in production
const API_BASE_URL: string = 'http://localhost:8081/api/transactions';

// Make API_BASE_URL available globally
declare global {
  interface Window {
    API_BASE_URL: string;
  }
}
window.API_BASE_URL = API_BASE_URL;

// Render the App component to the DOM
const root = ReactDOM.createRoot(document.getElementById('root') as HTMLElement);
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);