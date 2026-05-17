import React, { useState, useEffect, useRef, useMemo } from 'react';
import axios from 'axios';
import './styles.css';
import ImportPage from './components/ImportPage';

// API base URL - change this to match your backend URL in production
const API_BASE_URL = '/api/transactions';

// Utility function to format currency with proper symbols
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
const CategoryTotalsTable = ({categoryTotals, transactions, selectedMonth, selectedYear}) => {
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
const TransactionsTable = ({transactions, onCategoryChange}) => {
    const [filteredTransactions, setFilteredTransactions] = useState(transactions);
    const [descriptionFilter, setDescriptionFilter] = useState('');
    const [categoryFilter, setCategoryFilter] = useState('');
    const [isFiltering, setIsFiltering] = useState(false);
    const [categories, setCategories] = useState([]);
    const [editingId, setEditingId] = useState(null);
    const [pendingCategory, setPendingCategory] = useState('');
    const [editingAmountId, setEditingAmountId] = useState(null);
    const [pendingAmount, setPendingAmount] = useState('');

    useEffect(() => {
        axios.get(`${API_BASE_URL}/categories`)
            .then(res => setCategories(res.data))
            .catch(() => {});
    }, []);

    const startEdit = (id, current) => {
        setEditingId(id);
        setPendingCategory(current);
    };

    const commitCategoryEdit = async (id) => {
        if (!pendingCategory) { setEditingId(null); return; }
        try {
            await axios.patch(`${API_BASE_URL}/${id}/category`, { category: pendingCategory });
            if (onCategoryChange) onCategoryChange();
        } catch (err) {
            console.warn('Category update failed:', err);
        }
        setEditingId(null);
    };

    const startAmountEdit = (id, current) => {
        setEditingAmountId(id);
        setPendingAmount(String(current));
    };

    const commitAmountEdit = async (id) => {
        const val = parseFloat(pendingAmount);
        if (isNaN(val)) { setEditingAmountId(null); return; }
        try {
            await axios.patch(`${API_BASE_URL}/${id}/amount`, { amount: val });
            if (onCategoryChange) onCategoryChange();
        } catch (err) {
            console.warn('Amount update failed:', err);
        }
        setEditingAmountId(null);
    };

    const excludeTransaction = async (id) => {
        try {
            await axios.patch(`${API_BASE_URL}/${id}/exclude`);
            if (onCategoryChange) onCategoryChange();
        } catch (err) {
            console.warn('Exclude failed:', err);
        }
    };

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
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {filteredTransactions.length === 0 ? (
                        <tr>
                            <td colSpan="5" className="text-center">
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
                                <td onClick={() => startAmountEdit(transaction.id, transaction.amount)}
                                    style={{cursor: 'pointer'}}>
                                    {editingAmountId === transaction.id ? (
                                        <input type="number" step="0.01"
                                               className="form-control form-control-sm"
                                               style={{width: 100}}
                                               value={pendingAmount}
                                               onChange={e => setPendingAmount(e.target.value)}
                                               onBlur={() => commitAmountEdit(transaction.id)}
                                               onKeyDown={e => {
                                                   if (e.key === 'Enter') commitAmountEdit(transaction.id);
                                                   if (e.key === 'Escape') setEditingAmountId(null);
                                               }}
                                               autoFocus />
                                    ) : (
                                        formatCurrency(transaction.amount, transaction.currency)
                                    )}
                                </td>
                                <td>
                                    <div style={{display: 'flex', alignItems: 'center', gap: 4}}>
                                        {transaction.hasOverride && editingId !== transaction.id && (
                                            <i className="bi bi-bookmark-fill"
                                               title="Saved rule applied"
                                               style={{color: 'var(--accent)', fontSize: 12, flexShrink: 0}} />
                                        )}
                                        {editingId === transaction.id ? (
                                            <select
                                                className="form-select form-select-sm"
                                                style={{fontSize: 12, minWidth: 140}}
                                                value={pendingCategory}
                                                onChange={e => setPendingCategory(e.target.value)}
                                                onBlur={() => commitCategoryEdit(transaction.id)}
                                                autoFocus
                                            >
                                                {categories.map(c => <option key={c}>{c}</option>)}
                                            </select>
                                        ) : (
                                            <span
                                                onClick={() => startEdit(transaction.id, transaction.category)}
                                                style={{cursor: 'pointer'}}
                                                title="Click to edit"
                                            >
                                                {transaction.category}
                                            </span>
                                        )}
                                    </div>
                                </td>
                                <td>
                                    <button className="btn btn-sm btn-outline-danger"
                                            title="Exclude transaction"
                                            onClick={() => excludeTransaction(transaction.id)}>
                                        <i className="bi bi-eye-slash"></i>
                                    </button>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

// Hero — YTD total + 12-month SVG sparkline + transaction count
const Hero = ({ totalAmount, monthlyTotals, primaryCurrency, selectedYear, selectedMonth, transactionCount }) => {
    const monthsArr = Array.from({length: 12}, (_, i) => monthlyTotals[i + 1] || 0);
    const max = Math.max(...monthsArr, 1);
    const now = new Date();
    const curMonth = (selectedYear === now.getFullYear()) ? now.getMonth() : 11;
    const activeIdx = selectedMonth ? selectedMonth - 1 : curMonth;
    const thisMonth = monthsArr[activeIdx] || 0;
    const prevMonth = activeIdx > 0 ? (monthsArr[activeIdx - 1] || 0) : 0;
    const delta = prevMonth > 0 ? ((thisMonth - prevMonth) / prevMonth) * 100 : 0;
    const monthName = new Date(0, activeIdx).toLocaleString('default', {month: 'long'});

    const W = 340, H = 56, gap = 4, bw = (W - gap * 11) / 12;

    return (
        <div className="hero">
            <div>
                <div className="ytd-label">
                    {selectedMonth ? `${monthName} ${selectedYear}` : `Total spending · ${selectedYear}`}
                </div>
                <div className="ytd-value">
                    {formatCurrency(selectedMonth ? thisMonth : totalAmount, primaryCurrency)}
                </div>
                <div className="ytd-sub">
                    {prevMonth > 0 ? (
                        <>
                            <span className={delta >= 0 ? 'delta-up' : 'delta-down'}>
                                <i className={`bi ${delta >= 0 ? 'bi-arrow-up-right' : 'bi-arrow-down-right'}`}></i>
                                {' '}{Math.abs(delta).toFixed(1)}%
                            </span>
                            {' '}vs previous month
                        </>
                    ) : (
                        <span className="text-muted">No prior month data</span>
                    )}
                </div>
            </div>
            <div className="spark">
                <svg width="100%" height={H} viewBox={`0 0 ${W} ${H}`} preserveAspectRatio="none">
                    {monthsArr.map((v, i) => {
                        const h = v > 0 ? Math.max(2, (v / max) * (H - 8)) : 2;
                        const x = i * (bw + gap);
                        const y = H - h;
                        const isActive = i === activeIdx;
                        return (
                            <g key={i}>
                                <rect x={x} y={y} width={bw} height={h}
                                      fill={isActive ? 'var(--accent)' : (v > 0 ? '#c7d4e8' : '#eef1f5')}
                                      rx={2} />
                                <text x={x + bw / 2} y={H - 0.5} textAnchor="middle"
                                      fontSize="9" fill={isActive ? 'var(--accent)' : 'var(--muted)'}
                                      style={{fontWeight: isActive ? 600 : 400}}>
                                    {new Date(0, i).toLocaleString('default', {month: 'narrow'})}
                                </text>
                            </g>
                        );
                    })}
                </svg>
            </div>
            <div className="last-month">
                <div className="last-month-label">Transactions</div>
                <div className="last-month-value">{transactionCount}</div>
            </div>
        </div>
    );
};

// FilterStrip — global month/year selector that drives the whole dashboard
const FilterStrip = ({ selectedMonth, selectedYear, onChange, scope }) => {
    const currentYear = new Date().getFullYear();
    const years = Array.from({length: 5}, (_, i) => currentYear - i);
    return (
        <div className="filter-strip">
            <span className="fs-label"><i className="bi bi-funnel me-1"></i>Filter</span>
            <select
                value={selectedMonth || ''}
                onChange={e => onChange(e.target.value === '' ? null : +e.target.value, selectedYear)}
            >
                <option value="">All months</option>
                {Array.from({length: 12}, (_, i) => (
                    <option key={i + 1} value={i + 1}>
                        {new Date(0, i).toLocaleString('default', {month: 'long'})}
                    </option>
                ))}
            </select>
            <select
                value={selectedYear}
                onChange={e => onChange(selectedMonth, +e.target.value)}
            >
                {years.map(y => <option key={y} value={y}>{y}</option>)}
            </select>
            <span className="spacer"></span>
            <span className="scope-chip">
                <i className="bi bi-info-circle me-1"></i>
                Dashboard scoped to {scope}
            </span>
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
                            <i className="bi bi-grip-vertical"></i>
                        </span>
                    )}
                    <span>{title}</span>
                </span>
                <button
                    className="btn btn-sm btn-link p-0"
                    onClick={() => setIsExpanded(!isExpanded)}
                    aria-expanded={isExpanded}
                >
                    <i className={`bi ${isExpanded ? 'bi-chevron-up' : 'bi-chevron-down'}`}></i>
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
    layout: { left: ['chart'], right: ['totals'], bottom: ['transactions'] },
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
            if (saved && Array.isArray(saved.left) && Array.isArray(saved.right) && Array.isArray(saved.bottom)) {
                const strip = arr => arr.filter(id => id !== 'upload');
                return { left: strip(saved.left), right: strip(saved.right), bottom: strip(saved.bottom) };
            }
        } catch {}
        return DEFAULT_LAYOUT.layout;
    });
    const [showHandles, setShowHandles] = useState(DEFAULT_LAYOUT.showHandles);
    const [tweaksVisible, setTweaksVisible] = useState(false);
    const [showImport, setShowImport] = useState(false);

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

    // Handle month/year selection change (global filter)
    const handleMonthYearChange = async (month, year) => {
        setSelectedMonth(month);
        setSelectedYear(year);
        await loadCategoryTotals(month, year);
    };

    // Client-side monthly breakdown for the sparkline (respects selectedYear)
    const monthlyTotalsForYear = useMemo(() => {
        const out = {};
        transactions.forEach(t => {
            const d = new Date(t.date);
            if (d.getFullYear() !== selectedYear) return;
            const m = d.getMonth() + 1;
            out[m] = (out[m] || 0) + t.amount;
        });
        return out;
    }, [transactions, selectedYear]);

    // Transactions visible under current global filter (for Hero count + category modal)
    const filteredTransactions = useMemo(() => {
        return transactions.filter(t => {
            const d = new Date(t.date);
            if (d.getFullYear() !== selectedYear) return false;
            if (selectedMonth && (d.getMonth() + 1) !== selectedMonth) return false;
            return true;
        });
    }, [transactions, selectedMonth, selectedYear]);

    // YTD total for the selected year
    const yearTotal = useMemo(() =>
        Object.values(monthlyTotalsForYear).reduce((s, a) => s + a, 0),
        [monthlyTotalsForYear]
    );

    // Reset all data
    const handleReset = async () => {
        const confirmed = window.confirm(
            'Reset will:\n' +
            '  • Delete ALL transactions from the database\n' +
            '  • Re-queue all .csv.done files for reprocessing\n' +
            '  • Re-queue all .csv.failed files for retry\n' +
            '  • Clear all import failure records\n\n' +
            'This cannot be undone. Continue?'
        );
        if (confirmed) {
            try {
                await axios.delete(`${API_BASE_URL}/reset`);
                alert('Reset complete. All CSV files have been re-queued and will be processed within 5 minutes.');
                loadData();
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

    if (showImport) {
        return <ImportPage onBack={() => { setShowImport(false); loadData(); }} />;
    }

    if (loading) {
        return (
            <div className="container mt-5">
                <div className="text-center" style={{color: 'var(--muted)'}}>
                    <div className="spinner-border" style={{color: 'var(--accent)'}} role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                    <p className="mt-2" style={{fontSize: 13}}>Loading data…</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="container mt-5">
                <div className="alert" style={{background: '#fff5f5', border: '1px solid #fcd4d1', color: 'var(--danger)'}} role="alert">
                    <i className="bi bi-exclamation-triangle-fill me-2"></i>
                    {error}
                    <button className="btn btn-sm ms-3" style={{color: 'var(--accent)', border: '1px solid var(--line)'}} onClick={loadData}>
                        <i className="bi bi-arrow-clockwise me-1"></i>Retry
                    </button>
                </div>
            </div>
        );
    }

    const primaryCurrency = getPrimaryCurrency(transactions);

    const cards = {
        chart: {
            title: <span className="section-title"><i className="bi bi-bar-chart"></i>Spending by Category</span>,
            defaultExpanded: true,
            body: <SpendingByCategory categoryTotals={categoryTotals} primaryCurrency={primaryCurrency} />,
        },
        totals: {
            title: <span className="section-title"><i className="bi bi-pie-chart"></i>Category Totals</span>,
            defaultExpanded: true,
            body: (
                <CategoryTotalsTable
                    categoryTotals={categoryTotals}
                    transactions={filteredTransactions}
                    selectedMonth={selectedMonth}
                    selectedYear={selectedYear}
                />
            ),
        },
        transactions: {
            title: <span className="section-title"><i className="bi bi-list-ul"></i>All Transactions</span>,
            defaultExpanded: false,
            body: <TransactionsTable transactions={transactions} onCategoryChange={loadData} />,
        },
    };

    return (
        <div className="container mt-4">
            <div className="appbar">
                <h1><i className="bi bi-receipt"></i>Monthly Expense Tracker</h1>
                <div className="actions">
                    <button className="btn btn-outline-primary btn-sm me-2" onClick={loadData}>
                        <i className="bi bi-arrow-clockwise me-1"></i>Refresh
                    </button>
                    <button className="btn btn-outline-primary btn-sm me-2" onClick={() => setShowImport(true)}>
                        <i className="bi bi-upload me-1"></i>Import
                    </button>
                    <button className="btn btn-outline-danger btn-sm" onClick={handleReset}>
                        <i className="bi bi-trash me-1"></i>Reset
                    </button>
                </div>
            </div>

            <FilterStrip
                selectedMonth={selectedMonth}
                selectedYear={selectedYear}
                onChange={handleMonthYearChange}
                scope={selectedMonth
                    ? `${new Date(0, selectedMonth - 1).toLocaleString('default', {month: 'long'})} ${selectedYear}`
                    : selectedYear}
            />

            <Hero
                totalAmount={yearTotal}
                monthlyTotals={monthlyTotalsForYear}
                primaryCurrency={primaryCurrency}
                selectedYear={selectedYear}
                selectedMonth={selectedMonth}
                transactionCount={filteredTransactions.length}
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