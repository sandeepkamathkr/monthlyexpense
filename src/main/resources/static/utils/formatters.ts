// Utility function to format currency
export const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(amount);
};

// Utility function to format date
export const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
};

// For backward compatibility with the global window object
declare global {
  interface Window {
    Formatters: {
      formatCurrency: (amount: number) => string;
      formatDate: (dateString: string) => string;
    };
  }
}

// Export to window for legacy code
window.Formatters = {
  formatCurrency,
  formatDate
};