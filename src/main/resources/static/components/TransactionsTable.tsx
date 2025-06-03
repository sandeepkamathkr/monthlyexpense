import React, { useState, useEffect, ChangeEvent } from 'react';
import { formatCurrency, formatDate } from '../utils/formatters';

interface Transaction {
  id: number;
  date: string;
  description: string;
  category: string;
  amount: number;
}

interface TransactionsTableProps {
  transactions: Transaction[];
}

export const TransactionsTable: React.FC<TransactionsTableProps> = ({ transactions }) => {
  const [filteredTransactions, setFilteredTransactions] = useState<Transaction[]>(transactions);
  const [descriptionFilter, setDescriptionFilter] = useState<string>('');
  const [categoryFilter, setCategoryFilter] = useState<string>('');
  const [isFiltering, setIsFiltering] = useState<boolean>(false);

  // Update filtered transactions when props or filters change
  useEffect(() => {
    if (descriptionFilter || categoryFilter) {
      setIsFiltering(true);
      // Apply filters
      const filtered = transactions.filter(transaction => {
        const matchesDescription = !descriptionFilter || 
          transaction.description.toLowerCase().includes(descriptionFilter.toLowerCase());
        const matchesCategory = !categoryFilter || 
          transaction.category.toLowerCase().includes(categoryFilter.toLowerCase());
        return matchesDescription && matchesCategory;
      });
      setFilteredTransactions(filtered);
    } else {
      setIsFiltering(false);
      setFilteredTransactions(transactions);
    }
  }, [transactions, descriptionFilter, categoryFilter]);

  // Handle description filter change
  const handleDescriptionFilterChange = (e: ChangeEvent<HTMLInputElement>): void => {
    setDescriptionFilter(e.target.value);
  };

  // Handle category filter change
  const handleCategoryFilterChange = (e: ChangeEvent<HTMLInputElement>): void => {
    setCategoryFilter(e.target.value);
  };

  // Clear all filters
  const clearFilters = (): void => {
    setDescriptionFilter('');
    setCategoryFilter('');
  };

  return (
    <div>
      <div className="filters-container mb-3">
        <div className="row g-3">
          <div className="col-md-5">
            <div className="input-group">
              <span className="input-group-text">
                <i className="bi bi-search"></i>
              </span>
              <input
                type="text"
                className="form-control"
                placeholder="Filter by description..."
                value={descriptionFilter}
                onChange={handleDescriptionFilterChange}
              />
            </div>
          </div>
          <div className="col-md-5">
            <div className="input-group">
              <span className="input-group-text">
                <i className="bi bi-tag"></i>
              </span>
              <input
                type="text"
                className="form-control"
                placeholder="Filter by category..."
                value={categoryFilter}
                onChange={handleCategoryFilterChange}
              />
            </div>
          </div>
          <div className="col-md-2">
            <button 
              className="btn btn-outline-secondary w-100" 
              onClick={clearFilters}
              disabled={!descriptionFilter && !categoryFilter}
            >
              <i className="bi bi-x-circle me-1"></i>
              Clear
            </button>
          </div>
        </div>
        {isFiltering && (
          <div className="filter-status mt-2">
            <span className="badge bg-info">
              <i className="bi bi-funnel-fill me-1"></i>
              Showing {filteredTransactions.length} of {transactions.length} transactions
            </span>
            {descriptionFilter && (
              <span className="badge bg-primary ms-2">
                Description: {descriptionFilter}
                <button 
                  className="btn-close btn-close-white ms-1" 
                  style={{fontSize: '0.5rem'}}
                  onClick={() => setDescriptionFilter('')}
                ></button>
              </span>
            )}
            {categoryFilter && (
              <span className="badge bg-primary ms-2">
                Category: {categoryFilter}
                <button 
                  className="btn-close btn-close-white ms-1" 
                  style={{fontSize: '0.5rem'}}
                  onClick={() => setCategoryFilter('')}
                ></button>
              </span>
            )}
          </div>
        )}
      </div>
      <div className="table-container">
        <table className="table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Description</th>
              <th>Amount</th>
              <th>Category</th>
            </tr>
          </thead>
          <tbody>
            {filteredTransactions.length === 0 ? (
              <tr>
                <td colSpan={4} className="text-center">
                  {isFiltering ? (
                    <div>
                      <i className="bi bi-filter-circle-fill fs-3 d-block mb-2 text-muted"></i>
                      No transactions match the current filters
                    </div>
                  ) : (
                    <div>No transactions found</div>
                  )}
                </td>
              </tr>
            ) : (
              filteredTransactions.map((transaction, index) => (
                <tr key={index}>
                  <td>{formatDate(transaction.date)}</td>
                  <td>{transaction.description}</td>
                  <td>{formatCurrency(transaction.amount)}</td>
                  <td>{transaction.category}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};