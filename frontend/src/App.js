import React, { useState, useEffect, useRef, useMemo } from 'react';
import axios from 'axios';
import './styles.css';

// API base URL - change this to match your backend URL in production
const API_BASE_URL = '/api/transactions';

1// Utility function to format currency with proper symbols
const formatCurrency = (amount, currency = 'AUD') => {
    // For Australian users, prioritize symbol display over locale consistency
    const currencyLocaleMap = {
        'AUD': 'en-AU',  // A$100.00
        'USD': 'en-US',  // $100.00 (instead of USD 100.00)
        'EUR': 'en-US',  // €100.00 (instead of EUR 100.00) 
        'GBP': 'en-GB',  // £100.00 (instead of GBP 100.00)
        'INR': 'en-IN',  // ₹100.00 (instead of INR 100.00)
        'JPY': 'ja-JP',  // ¥100 (instead of JPY 100.00)
        'CAD': 'en-CA',  // CA$100.00 (instead of CAD 100.00)
        'CHF': 'de-CH',  // CHF 100.00 (Swiss formatting)
        'CNY': 'zh-CN',  // ¥100.00 (instead of CNY 100.00)
        'KRW': 'ko-KR'   // ₩100 (instead of KRW 100.00)
    };
    
    const locale = currencyLocaleMap[currency] || 'en-US';
    
    return new Intl.NumberFormat(locale, {
        style: 'currency',
        currency: currency
    }).format(amount);
};

// Currency utilities
const getCurrencySymbol = (currency = 'AUD') => {
    try {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: currency
        }).formatToParts(0).find(part => part.type === 'currency').value;
    } catch (error) {
        return currency;
    }
};

const SUPPORTED_CURRENCIES = [
    { code: 'USD', name: 'US Dollar', symbol: '$' },
    { code: 'EUR', name: 'Euro', symbol: '€' },
    { code: 'GBP', name: 'British Pound', symbol: '£' },
    { code: 'JPY', name: 'Japanese Yen', symbol: '¥' },
    { code: 'AUD', name: 'Australian Dollar', symbol: 'A$' },
    { code: 'CAD', name: 'Canadian Dollar', symbol: 'C$' },
    { code: 'CHF', name: 'Swiss Franc', symbol: 'CHF' },
    { code: 'CNY', name: 'Chinese Yuan', symbol: '¥' },
    { code: 'INR', name: 'Indian Rupee', symbol: '₹' },
    { code: 'SGD', name: 'Singapore Dollar', symbol: 'S$' }
];

// Utility function to format date
const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
};

// File Upload Component
const FileUpload = ({onUploadSuccess}) => {
    const [selectedFile, setSelectedFile] = useState(null);
    const [isUploading, setIsUploading] = useState(false);
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [showErrorModal, setShowErrorModal] = useState(false);
    const [modalError, setModalError] = useState(null);
    const [selectedCurrency, setSelectedCurrency] = useState('USD');
    const [primaryCurrency, setPrimaryCurrency] = useState(null);
    const [isLoadingCurrency, setIsLoadingCurrency] = useState(true);

    // Load primary currency when component mounts
    useEffect(() => {
        const loadPrimaryCurrency = async () => {
            try {
                const response = await axios.get(`${API_BASE_URL}/primary-currency`);
                const currency = response.data.primaryCurrency;
                setPrimaryCurrency(currency);
                if (currency) {
                    setSelectedCurrency(currency);
                }
            } catch (error) {
                console.error('Error loading primary currency:', error);
            } finally {
                setIsLoadingCurrency(false);
            }
        };
        loadPrimaryCurrency();
    }, []);

    const handleFileChange = (event) => {
        setSelectedFile(event.target.files[0]);
        setMessage('');
        setError('');
    };

    const handleUpload = async () => {
        if (!selectedFile) {
            setError('Please select a file to upload');
            return;
        }

        const formData = new FormData();
        formData.append('file', selectedFile);
        formData.append('currency', selectedCurrency);

        setIsUploading(true);
        setMessage('');
        setError('');

        try {
            const response = await axios.post(`${API_BASE_URL}/upload`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });

            setMessage(`File uploaded successfully. ${response.data.count} transactions processed.`);
            setSelectedFile(null);

            // Notify parent component about successful upload
            if (onUploadSuccess) {
                onUploadSuccess();
            }
        } catch (error) {
            console.error('Error uploading file:', error);
            
            // Check if it's a structured error response
            if (error.response?.data && typeof error.response.data === 'object' && error.response.data.error) {
                const errorData = error.response.data;
                
                if (errorData.error === 'CURRENCY_MISMATCH' || errorData.error === 'MIXED_CURRENCIES_IN_CSV' || errorData.error === 'INVALID_CURRENCY') {
                    // Show modal for currency-related errors
                    setModalError({
                        type: errorData.error,
                        message: errorData.message
                    });
                    setShowErrorModal(true);
                } else {
                    // Show regular error for other validation errors
                    setError(errorData.message || 'Validation error occurred. Please check your file.');
                }
            } else {
                // Handle non-structured error responses
                const errorMessage = typeof error.response?.data === 'string' 
                    ? error.response.data 
                    : 'Error uploading file. Please try again.';
                setError(errorMessage);
            }
        } finally {
            setIsUploading(false);
        }
    };

    const handleResetFromModal = async () => {
        if (window.confirm('Are you sure you want to reset all data? This action cannot be undone.')) {
            try {
                await axios.delete(`${API_BASE_URL}/reset`);
                setShowErrorModal(false);
                setModalError(null);
                setMessage('Database reset successfully. You can now upload your new CSV file.');
                
                // Notify parent to reload data
                if (onUploadSuccess) {
                    onUploadSuccess();
                }
            } catch (error) {
                console.error('Error resetting data:', error);
                setError('Failed to reset database. Please try again.');
            }
        }
    };

    const handleCloseModal = () => {
        setShowErrorModal(false);
        setModalError(null);
    };

    return (
        <div className="card">
            <div className="card-header">Upload Transaction CSV</div>
            <div className="card-body">
                <div className="form-group mb-3">
                    <label htmlFor="file" className="form-label">Select CSV File</label>
                    <input
                        type="file"
                        className="form-control"
                        id="file"
                        accept=".csv"
                        onChange={handleFileChange}
                    />
                    <small className="form-text text-muted">
                        CSV should have columns: Date, Description, Amount, Category (no Currency column needed)
                    </small>
                </div>

                <div className="form-group mb-3">
                    <label htmlFor="currency" className="form-label">
                        Currency {primaryCurrency ? '(Set by existing transactions)' : '(Select for new transactions)'}
                    </label>
                    {isLoadingCurrency ? (
                        <div className="text-muted">Loading currency...</div>
                    ) : (
                        <select
                            id="currency"
                            className="form-select"
                            value={selectedCurrency}
                            onChange={(e) => setSelectedCurrency(e.target.value)}
                            disabled={!!primaryCurrency}
                        >
                            {SUPPORTED_CURRENCIES.map(currency => (
                                <option key={currency.code} value={currency.code}>
                                    {currency.code} - {currency.name} ({currency.symbol})
                                </option>
                            ))}
                        </select>
                    )}
                    {primaryCurrency && (
                        <small className="form-text text-muted">
                            Currency is locked to {primaryCurrency} based on existing transactions. Reset database to change.
                        </small>
                    )}
                    {!primaryCurrency && (
                        <small className="form-text text-muted">
                            This will be the primary currency for all future uploads.
                        </small>
                    )}
                </div>

                <button
                    className="btn btn-primary"
                    onClick={handleUpload}
                    disabled={!selectedFile || isUploading || isLoadingCurrency}
                >
                    {isUploading ? 'Uploading...' : 'Upload'}
                </button>

                {message && <div className="alert alert-success mt-3">{message}</div>}
                {error && <div className="alert alert-danger mt-3">{error}</div>}
            </div>
            
            <ErrorModal 
                show={showErrorModal}
                onClose={handleCloseModal}
                error={modalError}
                onReset={handleResetFromModal}
            />
        </div>
    );
};

// Error Modal Component
const ErrorModal = ({ show, onClose, error, onReset }) => {
    if (!show) return null;

    const isCurrencyMismatch = error?.type === 'CURRENCY_MISMATCH';
    const isMixedCurrencies = error?.type === 'MIXED_CURRENCIES_IN_CSV';
    const isInvalidCurrency = error?.type === 'INVALID_CURRENCY';

    return (
        <div className="modal" tabIndex="-1" style={{display: 'block', backgroundColor: 'rgba(0,0,0,0.5)'}}>
            <div className="modal-dialog modal-dialog-centered">
                <div className="modal-content">
                    <div className="modal-header bg-danger text-white">
                        <h5 className="modal-title">
                            <i className="bi bi-exclamation-triangle me-2"></i>
                            {isCurrencyMismatch ? 'Currency Mismatch' : 
                             isMixedCurrencies ? 'Mixed Currencies Detected' : 
                             isInvalidCurrency ? 'Invalid Currency Code' : 'Upload Error'}
                        </h5>
                    </div>
                    <div className="modal-body">
                        <div className="text-center mb-3">
                            <i className="bi bi-currency-exchange fs-1 text-danger"></i>
                        </div>
                        <p className="text-center mb-3">
                            {error?.message || 'An error occurred during file upload.'}
                        </p>
                        
                        {isCurrencyMismatch && (
                            <div className="alert alert-warning">
                                <i className="bi bi-info-circle me-2"></i>
                                <strong>Solution:</strong> To upload transactions with a different currency, 
                                you must first reset the database to clear all existing transactions.
                            </div>
                        )}
                        
                        {isMixedCurrencies && (
                            <div className="alert alert-info">
                                <i className="bi bi-info-circle me-2"></i>
                                <strong>Required:</strong> All transactions in a single CSV file must use 
                                the same currency. Please ensure your CSV contains only one currency.
                            </div>
                        )}
                        
                        {isInvalidCurrency && (
                            <div className="alert alert-info">
                                <i className="bi bi-info-circle me-2"></i>
                                <strong>Required:</strong> Please use only valid ISO 4217 currency codes. 
                                Check your CSV file for any typos in the Currency column.
                            </div>
                        )}
                    </div>
                    <div className="modal-footer">
                        {isCurrencyMismatch && (
                            <button 
                                type="button" 
                                className="btn btn-warning me-2" 
                                onClick={onReset}
                            >
                                <i className="bi bi-arrow-counterclockwise me-1"></i>
                                Reset Database
                            </button>
                        )}
                        <button type="button" className="btn btn-secondary" onClick={onClose}>
                            <i className="bi bi-x-circle me-1"></i>
                            Close
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

// Category Transactions Modal Component
const CategoryTransactionsModal = ({show, onClose, category, transactions}) => {
    const [sortField, setSortField] = useState('date');
    const [sortDirection, setSortDirection] = useState('desc');

    if (!show) {
        return null;
    }

    // Helper function to capitalize first letter of each word
    const capitalizeWords = (str) => {
        return str.replace(/\b\w/g, char => char.toUpperCase());
    };

    const handleSort = (field) => {
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
                return sortDirection === 'asc' ? dateA - dateB : dateB - dateA;
            } else if (sortField === 'amount') {
                return sortDirection === 'asc' ? a.amount - b.amount : b.amount - a.amount;
            }
            return 0;
        });

    // Calculate total amount for this category and get primary currency
    const categoryTotal = filteredTransactions.reduce((sum, transaction) => sum + transaction.amount, 0);
    const primaryCurrency = filteredTransactions.length > 0 ? filteredTransactions[0].currency : 'AUD';

    return (
        <div className="modal" tabIndex="-1" style={{display: 'block', backgroundColor: 'rgba(0,0,0,0.5)'}}>
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
                                Total: <span className="fw-bold">{formatCurrency(categoryTotal, primaryCurrency)}</span>
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
                                        <td colSpan="3" className="text-center py-4">
                                            <i className="bi bi-inbox-fill fs-3 d-block mb-2 text-muted"></i>
                                            No transactions found
                                        </td>
                                    </tr>
                                ) : (
                                    filteredTransactions.map((transaction, index) => (
                                        <tr key={index} className="transaction-row">
                                            <td className="date-cell">{formatDate(transaction.date)}</td>
                                            <td className="description-cell">{transaction.description}</td>
                                            <td className="amount-cell">{formatCurrency(transaction.amount, transaction.currency)}</td>
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

// Month/Year Selector Component
const MonthYearSelector = ({ selectedMonth, selectedYear, onMonthYearChange }) => {
    const currentYear = new Date().getFullYear();
    const years = Array.from({ length: 5 }, (_, i) => currentYear - i);
    const months = [
        { value: null, label: 'All Months' },
        { value: 1, label: 'January' },
        { value: 2, label: 'February' },
        { value: 3, label: 'March' },
        { value: 4, label: 'April' },
        { value: 5, label: 'May' },
        { value: 6, label: 'June' },
        { value: 7, label: 'July' },
        { value: 8, label: 'August' },
        { value: 9, label: 'September' },
        { value: 10, label: 'October' },
        { value: 11, label: 'November' },
        { value: 12, label: 'December' }
    ];

    const handleMonthChange = (e) => {
        const month = e.target.value === '' ? null : parseInt(e.target.value);
        onMonthYearChange(month, selectedYear);
    };

    const handleYearChange = (e) => {
        const year = parseInt(e.target.value);
        onMonthYearChange(selectedMonth, year);
    };

    return (
        <div className="mb-3">
            <div className="row g-2">
                <div className="col-md-6">
                    <label htmlFor="monthSelect" className="form-label small">Month Filter:</label>
                    <select
                        id="monthSelect"
                        className="form-select form-select-sm"
                        value={selectedMonth || ''}
                        onChange={handleMonthChange}
                    >
                        {months.map(month => (
                            <option key={month.value || 'all'} value={month.value || ''}>
                                {month.label}
                            </option>
                        ))}
                    </select>
                </div>
                <div className="col-md-6">
                    <label htmlFor="yearSelect" className="form-label small">Year Filter:</label>
                    <select
                        id="yearSelect"
                        className="form-select form-select-sm"
                        value={selectedYear}
                        onChange={handleYearChange}
                    >
                        {years.map(year => (
                            <option key={year} value={year}>{year}</option>
                        ))}
                    </select>
                </div>
            </div>
            <div className="mt-2">
                <small className="text-muted">
                    <i className="bi bi-info-circle me-1"></i>
                    {selectedMonth ? 
                        `Showing category totals for ${months.find(m => m.value === selectedMonth)?.label} ${selectedYear}` :
                        `Showing category totals for all months in ${selectedYear}`
                    }
                </small>
            </div>
        </div>
    );
};

// Category Totals Table Component
const CategoryTotalsTable = ({categoryTotals, transactions, selectedMonth, selectedYear, onMonthYearChange}) => {
    const [showModal, setShowModal] = useState(false);
    const [selectedCategory, setSelectedCategory] = useState('');
    const [sortField, setSortField] = useState('amount');
    const [sortDirection, setSortDirection] = useState('desc');
    const [hoveredCategory, setHoveredCategory] = useState(null);

    // Get primary currency from transactions
    const getPrimaryCurrency = (transactions) => {
        if (!transactions || transactions.length === 0) return 'AUD';
        const currencyCount = {};
        transactions.forEach(transaction => {
            const currency = transaction.currency || 'AUD';
            currencyCount[currency] = (currencyCount[currency] || 0) + 1;
        });
        return Object.keys(currencyCount).reduce((a, b) => 
            currencyCount[a] > currencyCount[b] ? a : b
        );
    };
    
    const primaryCurrency = getPrimaryCurrency(transactions);

    const handleCategoryClick = (category) => {
        setSelectedCategory(category);
        setShowModal(true);
    };

    const handleCloseModal = () => {
        setShowModal(false);
    };

    const handleSort = (field) => {
        // If clicking the same field, toggle direction
        if (field === sortField) {
            setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
        } else {
            // If clicking a new field, set it as sort field and default to descending
            setSortField(field);
            setSortDirection('desc');
        }
    };

    // Calculate total of all categories
    const totalOfAllCategories = Object.values(categoryTotals).reduce((sum, amount) => sum + amount, 0);

    // Sort categories based on current sort field and direction
    const sortedCategories = Object.entries(categoryTotals)
        .sort((a, b) => {
            const [categoryA, amountA] = a;
            const [categoryB, amountB] = b;

            if (sortField === 'percentage') {
                // Sort by percentage (same as amount)
                return sortDirection === 'asc'
                    ? amountA - amountB
                    : amountB - amountA;
            } else if (sortField === 'category') {
                return sortDirection === 'asc'
                    ? categoryA.localeCompare(categoryB)
                    : categoryB.localeCompare(categoryA);
            } else { // amount
                return sortDirection === 'asc'
                    ? amountA - amountB
                    : amountB - amountA;
            }
        });

    // Helper function to capitalize first letter of each word
    const capitalizeWords = (str) => {
        return str.replace(/\b\w/g, char => char.toUpperCase());
    };

    // Calculate percentage for each category
    const calculatePercentage = (amount) => {
        return totalOfAllCategories > 0 ? (amount / totalOfAllCategories) * 100 : 0;
    };

    // Generate a consistent color for a category
    const getCategoryColor = (category) => {
        // Predefined colors for common categories
        const categoryColors = {
            'groceries': '#2ecc71',
            'food': '#e67e22',
            'dining': '#e74c3c',
            'restaurant': '#e74c3c',
            'utilities': '#3498db',
            'rent': '#9b59b6',
            'mortgage': '#9b59b6',
            'transportation': '#f1c40f',
            'entertainment': '#1abc9c',
            'shopping': '#e84393',
            'travel': '#0984e3',
            'health': '#00b894',
            'insurance': '#fdcb6e',
            'education': '#6c5ce7',
            'personal': '#00cec9',
            'gifts': '#ff7675',
            'subscriptions': '#a29bfe',
            'bills': '#74b9ff',
            'other': '#95a5a6'
        };

        // Check if the category has a predefined color
        const lowerCategory = category.toLowerCase();
        for (const [key, color] of Object.entries(categoryColors)) {
            if (lowerCategory.includes(key)) {
                return color;
            }
        }

        // If no predefined color, generate one based on the category name
        let hash = 0;
        for (let i = 0; i < category.length; i++) {
            hash = category.charCodeAt(i) + ((hash << 5) - hash);
        }

        // Convert to a hex color
        let color = '#';
        for (let i = 0; i < 3; i++) {
            const value = (hash >> (i * 8)) & 0xFF;
            color += ('00' + value.toString(16)).substr(-2);
        }

        return color;
    };

    return (
        <div>
            <MonthYearSelector 
                selectedMonth={selectedMonth}
                selectedYear={selectedYear}
                onMonthYearChange={onMonthYearChange}
            />
            <div className="d-flex justify-content-between align-items-center mb-3">
                <div className="info-text">
                    <i className="bi bi-info-circle"></i> Click on any category to view detailed transactions
                </div>
                <div className="total-summary">
                    <span className="total-label">Total:</span>
                    <span className="badge bg-primary rounded-pill total-badge">
                        {formatCurrency(totalOfAllCategories, primaryCurrency)}
                    </span>
                </div>
            </div>
            <div className="table-container">
                <table className="table table-hover category-table">
                    <thead>
                    <tr>
                        <th
                            onClick={() => handleSort('category')}
                            className="sortable-header category-column"
                        >
                            <div className="d-flex align-items-center">
                                <i className="bi bi-tag me-1 sort-icon"></i>
                                <span>Category</span>
                                {sortField === 'category' && (
                                    <i className={`bi ms-1 ${sortDirection === 'asc'
                                        ? 'bi-sort-up'
                                        : 'bi-sort-down'}`}></i>
                                )}
                            </div>
                        </th>
                        <th
                            onClick={() => handleSort('amount')}
                            className="sortable-header amount-column"
                        >
                            <div className="d-flex align-items-center">
                                <i className="bi bi-currency-dollar me-1 sort-icon"></i>
                                <span>Amount</span>
                                {sortField === 'amount' && (
                                    <i className={`bi ms-1 ${sortDirection === 'asc'
                                        ? 'bi-sort-up'
                                        : 'bi-sort-down'}`}></i>
                                )}
                            </div>
                        </th>
                        <th
                            onClick={() => handleSort('percentage')}
                            className="sortable-header percentage-column"
                        >
                            <div className="d-flex align-items-center">
                                <i className="bi bi-pie-chart me-1 sort-icon"></i>
                                <span>% of Total</span>
                                {sortField === 'percentage' && (
                                    <i className={`bi ms-1 ${sortDirection === 'asc'
                                        ? 'bi-sort-up'
                                        : 'bi-sort-down'}`}></i>
                                )}
                            </div>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    {sortedCategories.length === 0 ? (
                        <tr>
                            <td colSpan="3" className="text-center">No categories found</td>
                        </tr>
                    ) : (
                        sortedCategories.map(([category, amount], index) => {
                            const percentage = calculatePercentage(amount);
                            const isHovered = hoveredCategory === category;

                            return (
                                <tr
                                    key={index}
                                    onClick={() => handleCategoryClick(category)}
                                    onMouseEnter={() => setHoveredCategory(category)}
                                    onMouseLeave={() => setHoveredCategory(null)}
                                    className={`category-row ${isHovered ? 'hovered' : ''}`}
                                >
                                    <td>
                                        <div className="d-flex align-items-center">
                                            <div
                                                className="category-color-indicator me-2"
                                                style={{backgroundColor: getCategoryColor(category)}}
                                            ></div>
                                            <span className="category-name">{capitalizeWords(category)}</span>
                                            <i className="bi bi-box-arrow-up-right ms-2 click-icon"></i>
                                        </div>
                                    </td>
                                    <td className="amount-cell">{formatCurrency(amount, primaryCurrency)}</td>
                                    <td className="percentage-cell">
                                        <div className="d-flex align-items-center">
                                            <div className="progress flex-grow-1 me-2">
                                                <div
                                                    className="progress-bar"
                                                    role="progressbar"
                                                    style={{
                                                        width: `${percentage}%`,
                                                        backgroundColor: getCategoryColor(category)
                                                    }}
                                                    aria-valuenow={percentage}
                                                    aria-valuemin="0"
                                                    aria-valuemax="100"
                                                ></div>
                                            </div>
                                            <span className="percentage-value">{percentage.toFixed(1)}%</span>
                                        </div>
                                    </td>
                                </tr>
                            );
                        })
                    )}
                    </tbody>
                </table>
            </div>
            <CategoryTransactionsModal
                show={showModal}
                onClose={handleCloseModal}
                category={selectedCategory}
                transactions={transactions}
            />
        </div>
    );
};

// Transactions Table Component
const TransactionsTable = ({transactions}) => {
    const [filteredTransactions, setFilteredTransactions] = useState(transactions);
    const [descriptionFilter, setDescriptionFilter] = useState('');
    const [categoryFilter, setCategoryFilter] = useState('');
    const [isFiltering, setIsFiltering] = useState(false);

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
    const handleDescriptionFilterChange = (e) => {
        setDescriptionFilter(e.target.value);
    };

    // Handle category filter change
    const handleCategoryFilterChange = (e) => {
        setCategoryFilter(e.target.value);
    };

    // Clear all filters
    const clearFilters = () => {
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
                            <td colSpan="4" className="text-center">
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
                                <td>{formatCurrency(transaction.amount, transaction.currency)}</td>
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

// Summary Component
const Summary = ({totalAmount, monthlyTotals, primaryCurrency = 'AUD'}) => {
    return (
        <div className="summary-container mb-4">
            <div className="summary-card">
                <div className="summary-title">Total Spending</div>
                <div className="summary-value">{formatCurrency(totalAmount || 0, primaryCurrency)}</div>
            </div>

            {monthlyTotals && Object.entries(monthlyTotals).map(([month, amount]) => (
                <div className="summary-card" key={month}>
                    <div className="summary-title">
                        {new Date(0, month - 1).toLocaleString('default', {month: 'long'})}
                    </div>
                    <div className="summary-value">{formatCurrency(amount, primaryCurrency)}</div>
                </div>
            ))}
        </div>
    );
};

// Spending By Category Component (Stable Chart.js Implementation)
const SpendingByCategory = ({categoryTotals, primaryCurrency = 'AUD'}) => {
    const chartRef = useRef(null);
    const chartInstanceRef = useRef(null);
    const [chartError, setChartError] = useState(false);
    
    // Calculate top categories for display
    const topCategories = Object.entries(categoryTotals || {})
        .sort((a, b) => b[1] - a[1])
        .slice(0, 10);

    // Create chart effect
    useEffect(() => {
        if (!chartRef.current || topCategories.length === 0) {
            return;
        }

        // Function to create the chart
        const createChart = () => {
            try {
                // Destroy existing chart
                if (chartInstanceRef.current) {
                    chartInstanceRef.current.destroy();
                    chartInstanceRef.current = null;
                }

                const ctx = chartRef.current.getContext('2d');
                const categories = topCategories.map(item => item[0]);
                const amounts = topCategories.map(item => item[1]);

                chartInstanceRef.current = new window.Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: categories,
                        datasets: [{
                            data: amounts,
                            backgroundColor: [
                                '#3498db', '#2ecc71', '#e74c3c', '#f39c12', '#9b59b6',
                                '#1abc9c', '#d35400', '#34495e', '#16a085', '#c0392b'
                            ],
                            borderWidth: 1,
                            borderRadius: 4
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        indexAxis: 'y',
                        plugins: {
                            legend: {
                                display: false
                            },
                            title: {
                                display: true,
                                text: `Top ${topCategories.length} Spending Categories`,
                                font: {
                                    size: 16,
                                    weight: 'bold'
                                }
                            },
                            tooltip: {
                                callbacks: {
                                    label: function(context) {
                                        return formatCurrency(context.raw, primaryCurrency);
                                    }
                                }
                            }
                        },
                        scales: {
                            x: {
                                beginAtZero: true,
                                ticks: {
                                    callback: function(value) {
                                        return formatCurrency(value, primaryCurrency);
                                    }
                                }
                            },
                            y: {
                                ticks: {
                                    maxRotation: 0,
                                    font: {
                                        size: 12
                                    }
                                }
                            }
                        }
                    }
                });
                
                setChartError(false);
            } catch (error) {
                console.error('Chart creation error:', error);
                setChartError(true);
            }
        };

        // Load Chart.js if not already loaded
        if (window.Chart) {
            createChart();
        } else {
            // Load Chart.js dynamically
            const script = document.createElement('script');
            script.src = 'https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.js';
            script.onload = () => {
                createChart();
            };
            script.onerror = () => {
                console.error('Failed to load Chart.js');
                setChartError(true);
            };
            document.head.appendChild(script);
        }

        // Cleanup function
        return () => {
            if (chartInstanceRef.current) {
                try {
                    chartInstanceRef.current.destroy();
                    chartInstanceRef.current = null;
                } catch (error) {
                    console.error('Chart cleanup error:', error);
                }
            }
        };
    }, [topCategories]);

    // Show message if no data
    if (!categoryTotals || Object.keys(categoryTotals).length === 0) {
        return (
            <div className="chart-container d-flex justify-content-center align-items-center" style={{minHeight: '300px'}}>
                <div className="text-center text-muted">
                    <i className="bi bi-bar-chart fs-1 mb-2"></i>
                    <p>No category data available</p>
                </div>
            </div>
        );
    }

    // Fallback to progress bars if Chart.js fails
    if (chartError) {
        const colors = [
            '#3498db', '#2ecc71', '#e74c3c', '#f39c12', 
            '#9b59b6', '#1abc9c', '#d35400', '#34495e'
        ];
        const maxAmount = topCategories.length > 0 ? topCategories[0][1] : 0;
        
        return (
            <div className="chart-container">
                <div className="alert alert-warning mb-3">
                    <i className="bi bi-exclamation-triangle me-2"></i>
                    Chart.js unavailable - showing simplified view
                </div>
                <div className="row g-2">
                    {topCategories.slice(0, 8).map(([category, amount], index) => {
                        const percentage = maxAmount > 0 ? (amount / maxAmount) * 100 : 0;
                        const color = colors[index] || '#6c757d';
                        
                        return (
                            <div key={category} className="col-12">
                                <div className="d-flex justify-content-between align-items-center mb-1">
                                    <span className="fw-medium text-truncate" style={{maxWidth: '70%'}}>{category}</span>
                                    <span className="badge bg-primary rounded-pill">{formatCurrency(amount, primaryCurrency)}</span>
                                </div>
                                <div className="progress" style={{height: '8px'}}>
                                    <div 
                                        className="progress-bar" 
                                        style={{
                                            width: `${percentage}%`,
                                            backgroundColor: color
                                        }}
                                    ></div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>
        );
    }

    return (
        <div className="chart-container" style={{height: '400px', position: 'relative'}}>
            <canvas ref={chartRef}></canvas>
        </div>
    );
};

// Collapsible Card Component
const CollapsibleCard = ({ title, children, defaultExpanded = true, dragHandleProps = null, showHandle = true }) => {
    const [isExpanded, setIsExpanded] = useState(defaultExpanded);

    return (
        <div className="card">
            <div className="card-header d-flex justify-content-between align-items-center">
                <span className="d-flex align-items-center">
                    {showHandle && dragHandleProps && (
                        <span className="drag-handle" title="Drag to reorder" {...dragHandleProps}>
                            <i className="bi bi-grip-vertical" style={{color: 'white', fontSize: '1.05rem'}}></i>
                        </span>
                    )}
                    <span>{title}</span>
                </span>
                <button
                    className="btn btn-sm btn-link p-0"
                    onClick={() => setIsExpanded(!isExpanded)}
                    aria-expanded={isExpanded}
                >
                    <i className={`bi ${isExpanded ? 'bi-chevron-up' : 'bi-chevron-down'}`} style={{color: 'white'}}></i>
                </button>
            </div>
            {isExpanded && (
                <div className="card-body">
                    {children}
                </div>
            )}
        </div>
    );
};

// Drag-and-drop zone layout — three zones: left column, right column, full-width bottom
const DraggableZones = ({ cards, layout, setLayout, showHandles }) => {
    const [dragId, setDragId] = useState(null);
    const [overKey, setOverKey] = useState(null);

    const findCard = (id) => {
        for (const z of Object.keys(layout)) {
            const i = layout[z].indexOf(id);
            if (i >= 0) return { zone: z, index: i };
        }
        return null;
    };

    const moveTo = (id, zone, index) => {
        const next = { left: [...layout.left], right: [...layout.right], bottom: [...layout.bottom] };
        const src = findCard(id);
        if (!src) return;
        next[src.zone].splice(src.index, 1);
        let ins = index;
        if (src.zone === zone && src.index < index) ins = index - 1;
        ins = Math.max(0, Math.min(ins, next[zone].length));
        next[zone].splice(ins, 0, id);
        setLayout(next);
    };

    const onDragStart = (id) => (e) => {
        setDragId(id);
        e.dataTransfer.effectAllowed = 'move';
        try { e.dataTransfer.setData('text/plain', id); } catch {}
    };
    const onDragEnd = () => { setDragId(null); setOverKey(null); };

    const onCardDragOver = (zone, idx) => (e) => {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';
        const k = `${zone}:${idx}`;
        if (k !== overKey) setOverKey(k);
    };
    const onCardDrop = (zone, idx) => (e) => {
        e.preventDefault();
        if (dragId) { moveTo(dragId, zone, idx); }
        setDragId(null); setOverKey(null);
    };
    const onZoneDragOver = (zone) => (e) => {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';
        if (overKey !== zone) setOverKey(zone);
    };
    const onZoneDrop = (zone) => (e) => {
        e.preventDefault();
        if (dragId) { moveTo(dragId, zone, layout[zone].length); }
        setDragId(null); setOverKey(null);
    };

    const renderCard = (id, zone, idx) => {
        const card = cards[id];
        if (!card) return null;
        const isDragging = dragId === id;
        const isTarget = overKey === `${zone}:${idx}` && dragId && dragId !== id;
        return (
            <div
                key={id}
                className={`card-wrap mb-4 ${isDragging ? 'dragging' : ''} ${isTarget ? 'drop-target' : ''}`}
                onDragOver={onCardDragOver(zone, idx)}
                onDrop={onCardDrop(zone, idx)}
            >
                <CollapsibleCard
                    title={card.title}
                    defaultExpanded={card.defaultExpanded}
                    showHandle={showHandles}
                    dragHandleProps={{
                        draggable: true,
                        onDragStart: onDragStart(id),
                        onDragEnd: onDragEnd,
                    }}
                >
                    {card.body}
                </CollapsibleCard>
            </div>
        );
    };

    const zoneClass = (z) =>
        `drop-zone ${dragId ? 'dz-active' : ''} ${overKey === z ? 'dz-over' : ''}`;

    return (
        <div>
            <div className="row">
                <div className="col-md-6">
                    <div className={zoneClass('left')} onDragOver={onZoneDragOver('left')} onDrop={onZoneDrop('left')}>
                        {layout.left.map((id, i) => renderCard(id, 'left', i))}
                        {layout.left.length === 0 && <div className="drop-hint">Drop here</div>}
                    </div>
                </div>
                <div className="col-md-6">
                    <div className={zoneClass('right')} onDragOver={onZoneDragOver('right')} onDrop={onZoneDrop('right')}>
                        {layout.right.map((id, i) => renderCard(id, 'right', i))}
                        {layout.right.length === 0 && <div className="drop-hint">Drop here</div>}
                    </div>
                </div>
            </div>
            <div className={zoneClass('bottom')} onDragOver={onZoneDragOver('bottom')} onDrop={onZoneDrop('bottom')}>
                {layout.bottom.map((id, i) => renderCard(id, 'bottom', i))}
                {layout.bottom.length === 0 && <div className="drop-hint">Drop here — full-width row</div>}
            </div>
        </div>
    );
};

// Tweaks panel — fixed position, controlled by postMessage from design tool or direct toggle
const TweaksPanel = ({ visible, onReset, showHandles, setShowHandles }) => {
    if (!visible) return null;
    return (
        <div className="tweaks-panel">
            <h4>Tweaks</h4>
            <div className="tweak-row">
                <span>Show drag handles</span>
                <input type="checkbox" checked={showHandles} onChange={e => setShowHandles(e.target.checked)} />
            </div>
            <div className="tweak-row">
                <span>Card order</span>
                <button onClick={onReset}>Reset</button>
            </div>
            <div className="tweak-hint">
                Grab the <i className="bi bi-grip-vertical"></i> handle on any blue header to reorder the 4 cards.
            </div>
        </div>
    );
};

const DEFAULT_LAYOUT = {
    layout: { left: ['chart', 'upload'], right: ['totals'], bottom: ['transactions'] },
    showHandles: true,
};

// Main App Component
const App = () => {
    const [transactions, setTransactions] = useState([]);
    const [totalAmount, setTotalAmount] = useState(0);
    const [monthlyTotals, setMonthlyTotals] = useState({});
    const [categoryTotals, setCategoryTotals] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [selectedMonth, setSelectedMonth] = useState(null);
    const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());

    const [layout, setLayout] = useState(() => {
        try {
            const saved = JSON.parse(localStorage.getItem('met_card_layout') || 'null');
            if (saved && Array.isArray(saved.left) && Array.isArray(saved.right) && Array.isArray(saved.bottom)) return saved;
        } catch {}
        return DEFAULT_LAYOUT.layout;
    });
    const [showHandles, setShowHandles] = useState(DEFAULT_LAYOUT.showHandles);
    const [tweaksVisible, setTweaksVisible] = useState(false);

    // Load category totals based on selected month/year
    const loadCategoryTotals = async (month = null, year = null) => {
        try {
            let url = `${API_BASE_URL}/category-totals`;
            const params = [];
            
            if (month) params.push(`month=${month}`);
            if (year) params.push(`year=${year}`);
            
            if (params.length > 0) {
                url += `?${params.join('&')}`;
            }
            
            const categoryResponse = await axios.get(url);
            setCategoryTotals(categoryResponse.data);
        } catch (error) {
            console.error('Error loading category totals:', error);
            
            // Handle validation errors specifically
            if (error.response && error.response.status === 400) {
                const errorData = error.response.data;
                if (errorData.fieldErrors) {
                    const errorMessages = Object.values(errorData.fieldErrors).join(', ');
                    setError(`Invalid parameters: ${errorMessages}`);
                } else {
                    setError(errorData.message || 'Invalid request parameters');
                }
            } else {
                setError('Failed to load category totals. Please try again.');
            }
        }
    };

    // Determine primary currency from transactions
    const getPrimaryCurrency = (transactions) => {
        if (!transactions || transactions.length === 0) return 'AUD';
        
        // Count occurrences of each currency
        const currencyCount = {};
        transactions.forEach(transaction => {
            const currency = transaction.currency || 'AUD';
            currencyCount[currency] = (currencyCount[currency] || 0) + 1;
        });
        
        // Return the most frequent currency
        return Object.keys(currencyCount).reduce((a, b) => 
            currencyCount[a] > currencyCount[b] ? a : b
        );
    };

    // Load data from API
    const loadData = async () => {
        setLoading(true);
        setError('');

        try {
            // Get all transactions
            const transactionsResponse = await axios.get(`${API_BASE_URL}`);
            setTransactions(transactionsResponse.data);

            // Get total amount
            const totalResponse = await axios.get(`${API_BASE_URL}/total`);
            setTotalAmount(totalResponse.data.total);

            // Get monthly totals for current year
            const currentYear = new Date().getFullYear();
            const monthlyResponse = await axios.get(`${API_BASE_URL}/monthly-totals?year=${currentYear}`);
            setMonthlyTotals(monthlyResponse.data);

            // Get category totals based on current selection
            await loadCategoryTotals(selectedMonth, selectedYear);
        } catch (error) {
            console.error('Error loading data:', error);
            setError('Failed to load data. Please try again later.');
        } finally {
            setLoading(false);
        }
    };

    // Handle month/year selection change
    const handleMonthYearChange = async (month, year) => {
        setSelectedMonth(month);
        setSelectedYear(year);
        await loadCategoryTotals(month, year);
    };

    // Reset all data
    const handleReset = async () => {
        if (window.confirm('Are you sure you want to reset all data? This action cannot be undone.')) {
            try {
                await axios.delete(`${API_BASE_URL}/reset`);
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

    // Persist layout to localStorage whenever it changes
    useEffect(() => {
        try { localStorage.setItem('met_card_layout', JSON.stringify(layout)); } catch {}
    }, [layout]);

    // Respond to tweaks panel activation from design tool postMessage protocol
    useEffect(() => {
        const onMsg = (e) => {
            const d = e.data || {};
            if (d.type === '__activate_edit_mode') setTweaksVisible(true);
            if (d.type === '__deactivate_edit_mode') setTweaksVisible(false);
        };
        window.addEventListener('message', onMsg);
        try { window.parent.postMessage({ type: '__edit_mode_available' }, '*'); } catch {}
        return () => window.removeEventListener('message', onMsg);
    }, []);

    if (loading) {
        return (
            <div className="container mt-5">
                <div className="text-center">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                    <p className="mt-2">Loading data...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="container mt-5">
                <div className="alert alert-danger" role="alert">
                    <i className="bi bi-exclamation-triangle-fill me-2"></i>
                    {error}
                    <button className="btn btn-outline-danger ms-3" onClick={loadData}>
                        <i className="bi bi-arrow-clockwise me-1"></i>
                        Retry
                    </button>
                </div>
            </div>
        );
    }

    const primaryCurrency = getPrimaryCurrency(transactions);

    const cards = {
        upload: {
            title: '📊 Upload Data',
            defaultExpanded: false,
            body: <FileUpload onUploadSuccess={loadData} />,
        },
        chart: {
            title: '📈 Spending by Category',
            defaultExpanded: true,
            body: <SpendingByCategory categoryTotals={categoryTotals} primaryCurrency={primaryCurrency} />,
        },
        totals: {
            title: '💰 Category Totals',
            defaultExpanded: true,
            body: (
                <CategoryTotalsTable
                    categoryTotals={categoryTotals}
                    transactions={transactions}
                    selectedMonth={selectedMonth}
                    selectedYear={selectedYear}
                    onMonthYearChange={handleMonthYearChange}
                />
            ),
        },
        transactions: {
            title: '📋 All Transactions',
            defaultExpanded: false,
            body: <TransactionsTable transactions={transactions} />,
        },
    };

    return (
        <div className="container mt-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h1 className="display-4">
                    <i className="bi bi-receipt me-2"></i>
                    Monthly Expense Tracker
                </h1>
                <div>
                    <button className="btn btn-outline-primary me-2" onClick={loadData}>
                        <i className="bi bi-arrow-clockwise me-1"></i>
                        Refresh
                    </button>
                    <button className="btn btn-outline-danger" onClick={handleReset}>
                        <i className="bi bi-trash me-1"></i>
                        Reset All Data
                    </button>
                </div>
            </div>

            <Summary
                totalAmount={totalAmount}
                monthlyTotals={monthlyTotals}
                primaryCurrency={primaryCurrency}
            />

            <DraggableZones cards={cards} layout={layout} setLayout={setLayout} showHandles={showHandles} />

            <div className="text-center text-muted small py-3">
                Grab the <i className="bi bi-grip-vertical"></i> handle on any blue header · drag between left / right columns or the full-width bottom · saved to localStorage
            </div>

            <TweaksPanel
                visible={tweaksVisible}
                onReset={() => setLayout(DEFAULT_LAYOUT.layout)}
                showHandles={showHandles}
                setShowHandles={setShowHandles}
            />
        </div>
    );
};

export default App;