import React, { useState } from 'react';
import { formatCurrency, formatDate } from '../utils/formatters';

interface Transaction {
  id: number;
  date: string;
  description: string;
  category: string;
  amount: number;
}

interface CategoryTransactionsModalProps {
  show: boolean;
  onClose: () => void;
  category: string;
  transactions: Transaction[];
}

export const CategoryTransactionsModal: React.FC<CategoryTransactionsModalProps> = ({
  show,
  onClose,
  category,
  transactions
}) => {
  const [sortField, setSortField] = useState<'date' | 'amount'>('date');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc');

  if (!show) {
    return null;
  }

  // Helper function to capitalize first letter of each word
  const capitalizeWords = (str: string): string => {
    return str.replace(/\b\w/g, char => char.toUpperCase());
  };

  const handleSort = (field: 'date' | 'amount'): void => {
    // If clicking the same field, toggle direction
    if (field === sortField) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      // If clicking a new field, set it as sort field and default to descending
      setSortField(field);
      setSortDirection('desc');
    }
  };

  // Filter transactions by category
  const filteredTransactions = transactions
    .filter(transaction => transaction.category.toLowerCase() === category.toLowerCase())
    .sort((a, b) => {
      // Sort by the selected field
      if (sortField === 'date') {
        const dateA = new Date(a.date);
        const dateB = new Date(b.date);
        return sortDirection === 'asc' ? dateA.getTime() - dateB.getTime() : dateB.getTime() - dateA.getTime();
      } else if (sortField === 'amount') {
        return sortDirection === 'asc' ? a.amount - b.amount : b.amount - a.amount;
      }
      return 0;
    });

  // Calculate total amount for this category
  const categoryTotal = filteredTransactions.reduce((sum, transaction) => sum + transaction.amount, 0);

  return (
    <div className="modal" tabIndex={-1} style={{display: 'block', backgroundColor: 'rgba(0,0,0,0.5)'}}>
      <div className="modal-dialog modal-lg">
        <div className="modal-content">
          <div className="modal-header">
            <div className="d-flex flex-column">
              <h5 className="modal-title">
                <i className="bi bi-tag-fill me-2"></i>
                {capitalizeWords(category)} Transactions
              </h5>
              <div className="text-muted small mt-1">
                <span className="transaction-count">{filteredTransactions.length} transactions</span>
              </div>
            </div>
            <div className="d-flex flex-column align-items-end">
              <button type="button" className="btn-close" onClick={onClose}></button>
              <div className="category-total mt-1">
                Total: <span className="fw-bold">{formatCurrency(categoryTotal)}</span>
              </div>
            </div>
          </div>
          <div className="modal-body">
            <div className="sort-instructions mb-3">
              <i className="bi bi-arrow-down-up me-1"></i>
              Click on Date or Amount headers to sort
            </div>
            <div className="table-container">
              <table className="table table-hover transaction-table">
                <thead>
                <tr>
                  <th
                    onClick={() => handleSort('date')}
                    className="sortable-header"
                  >
                    <div className="d-flex align-items-center">
                      <span>Date</span>
                      {sortField === 'date' && (
                        <i className={`bi ms-1 ${sortDirection === 'asc'
                          ? 'bi-sort-up'
                          : 'bi-sort-down'}`}></i>
                      )}
                    </div>
                  </th>
                  <th>Description</th>
                  <th
                    onClick={() => handleSort('amount')}
                    className="sortable-header"
                  >
                    <div className="d-flex align-items-center">
                      <span>Amount</span>
                      {sortField === 'amount' && (
                        <i className={`bi ms-1 ${sortDirection === 'asc'
                          ? 'bi-sort-up'
                          : 'bi-sort-down'}`}></i>
                      )}
                    </div>
                  </th>
                </tr>
                </thead>
                <tbody>
                {filteredTransactions.length === 0 ? (
                  <tr>
                    <td colSpan={3} className="text-center py-4">
                      <i className="bi bi-inbox-fill fs-3 d-block mb-2 text-muted"></i>
                      No transactions found
                    </td>
                  </tr>
                ) : (
                  filteredTransactions.map((transaction, index) => (
                    <tr key={index} className="transaction-row">
                      <td className="date-cell">{formatDate(transaction.date)}</td>
                      <td className="description-cell">{transaction.description}</td>
                      <td className="amount-cell">{formatCurrency(transaction.amount)}</td>
                    </tr>
                  ))
                )}
                </tbody>
              </table>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-primary" onClick={onClose}>
              <i className="bi bi-x-circle me-1"></i>
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};