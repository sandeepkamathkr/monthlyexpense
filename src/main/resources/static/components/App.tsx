import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { FileUpload } from './FileUpload';
import { Summary } from './Summary';
import { SpendingByCategory } from './SpendingByCategory';
import { CategoryTotalsTable } from './CategoryTotalsTable';
import { TransactionsTable } from './TransactionsTable';
import { CollapsibleCard } from './CollapsibleCard';

// Define types for our data
interface Transaction {
  id: number;
  date: string;
  description: string;
  category: string;
  amount: number;
}

interface CategoryTotal {
  category: string;
  total: number;
}

interface MonthlyTotal {
  month: string;
  total: number;
}

// Main App Component
export const App: React.FC = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [totalAmount, setTotalAmount] = useState<number>(0);
  const [monthlyTotals, setMonthlyTotals] = useState<Record<string, number>>({});
  const [categoryTotals, setCategoryTotals] = useState<Record<string, number>>({});
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>('');
  
  // Load data from API
  const loadData = async (): Promise<void> => {
    setLoading(true);
    setError('');

    try {
      // Get all transactions
      const transactionsResponse = await axios.get<Transaction[]>(`${window.API_BASE_URL}`);
      setTransactions(transactionsResponse.data);

      // Get total amount
      const totalResponse = await axios.get<{total: number}>(`${window.API_BASE_URL}/total`);
      setTotalAmount(totalResponse.data.total);

      // Get monthly totals for current year
      const currentYear = new Date().getFullYear();
      const monthlyResponse = await axios.get<Record<string, number>>(`${window.API_BASE_URL}/monthly-totals?year=${currentYear}`);
      setMonthlyTotals(monthlyResponse.data);

      // Get category totals
      const categoryResponse = await axios.get<Record<string, number>>(`${window.API_BASE_URL}/category-totals`);
      setCategoryTotals(categoryResponse.data);
    } catch (error) {
      console.error('Error loading data:', error);
      setError('Failed to load data. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  // Reset all data
  const handleReset = async (): Promise<void> => {
    if (window.confirm('Are you sure you want to reset all data? This action cannot be undone.')) {
      try {
        await axios.delete(`${window.API_BASE_URL}/reset`);
        alert('All data has been reset successfully.');
        loadData(); // Reload data after reset
      } catch (error) {
        console.error('Error resetting data:', error);
        alert('Failed to reset data. Please try again.');
      }
    }
  };

  // Load data on component mount
  useEffect(() => {
    loadData();
  }, []);

  return (
    <div className="container">
      <h1 className="app-title">Monthly Expense Tracker</h1>

      <div className="row mb-4">
        <div className="col-md-6">
          <FileUpload onUploadSuccess={loadData}/>
        </div>
        <div className="col-md-6 d-flex align-items-center justify-content-center">
          <button className="btn btn-danger" onClick={handleReset}>
            Reset All Data
          </button>
        </div>
      </div>

      {loading ? (
        <div className="text-center">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p>Loading data...</p>
        </div>
      ) : error ? (
        <div className="alert alert-danger">{error}</div>
      ) : (
        <div>
          <Summary
            totalAmount={totalAmount}
            monthlyTotals={monthlyTotals}
          />

          <CollapsibleCard title="Spending by Category">
            <SpendingByCategory categoryTotals={categoryTotals} />
          </CollapsibleCard>

          <CollapsibleCard title="Category Totals">
            <CategoryTotalsTable categoryTotals={categoryTotals} transactions={transactions}/>
          </CollapsibleCard>

          <CollapsibleCard title="Transactions">
            <TransactionsTable transactions={transactions}/>
          </CollapsibleCard>
        </div>
      )}
    </div>
  );
};