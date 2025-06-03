import React from 'react';
import { formatCurrency } from '../utils/formatters';

interface SummaryProps {
  totalAmount: number;
  monthlyTotals: Record<string, number>;
}

export const Summary: React.FC<SummaryProps> = ({ totalAmount, monthlyTotals }) => {
  return (
    <div className="summary-container mb-4">
      <div className="summary-card">
        <div className="summary-title">Total Spending</div>
        <div className="summary-value">{formatCurrency(totalAmount || 0)}</div>
      </div>

      {monthlyTotals && Object.entries(monthlyTotals).map(([month, amount]) => (
        <div className="summary-card" key={month}>
          <div className="summary-title">
            {new Date(0, parseInt(month) - 1).toLocaleString('default', { month: 'long' })}
          </div>
          <div className="summary-value">{formatCurrency(amount)}</div>
        </div>
      ))}
    </div>
  );
};