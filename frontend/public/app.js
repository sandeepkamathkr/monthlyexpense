// API base URL - change this to match your backend URL in production
const API_BASE_URL = '/api/transactions';
// Utility function to format currency
const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
    }).format(amount);
};

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
    const [selectedFile, setSelectedFile] = React.useState(null);
    const [isUploading, setIsUploading] = React.useState(false);
    const [message, setMessage] = React.useState('');
    const [error, setError] = React.useState('');

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
            setError(error.response?.data || 'Error uploading file. Please try again.');
        } finally {
            setIsUploading(false);
        }
    };

    return (
        <div className="card">
            <div className="card-header">Upload Transaction CSV</div>
            <div className="card-body">
                <div className="form-group">
                    <label htmlFor="file" className="form-label">Select CSV File</label>
                    <input
                        type="file"
                        className="form-control"
                        id="file"
                        accept=".csv"
                        onChange={handleFileChange}
                    />
                    <small className="form-text text-muted">
                        CSV should have columns: Date, Description, Amount, Category
                    </small>
                </div>

                <button
                    className="btn btn-primary"
                    onClick={handleUpload}
                    disabled={!selectedFile || isUploading}
                >
                    {isUploading ? 'Uploading...' : 'Upload'}
                </button>

                {message && <div className="alert alert-success mt-3">{message}</div>}
                {error && <div className="alert alert-danger mt-3">{error}</div>}
            </div>
        </div>
    );
};



// Category Transactions Modal Component
const CategoryTransactionsModal = ({show, onClose, category, transactions}) => {
    const [sortField, setSortField] = React.useState('date');
    const [sortDirection, setSortDirection] = React.useState('desc');

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

    // Calculate total amount for this category
    const categoryTotal = filteredTransactions.reduce((sum, transaction) => sum + transaction.amount, 0);

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

// Category Totals Table Component
const CategoryTotalsTable = ({categoryTotals, transactions}) => {
    const [showModal, setShowModal] = React.useState(false);
    const [selectedCategory, setSelectedCategory] = React.useState('');
    const [sortField, setSortField] = React.useState('amount');
    const [sortDirection, setSortDirection] = React.useState('desc');
    const [hoveredCategory, setHoveredCategory] = React.useState(null);

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
                        {formatCurrency(totalOfAllCategories)}
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
                                    <td className="amount-cell">{formatCurrency(amount)}</td>
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
    const [filteredTransactions, setFilteredTransactions] = React.useState(transactions);
    const [descriptionFilter, setDescriptionFilter] = React.useState('');
    const [categoryFilter, setCategoryFilter] = React.useState('');
    const [isFiltering, setIsFiltering] = React.useState(false);

    // Update filtered transactions when props or filters change
    React.useEffect(() => {
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

// Summary Component
const Summary = ({totalAmount, monthlyTotals}) => {
    return (
        <div className="summary-container mb-4">
            <div className="summary-card">
                <div className="summary-title">Total Spending</div>
                <div className="summary-value">{formatCurrency(totalAmount || 0)}</div>
            </div>

            {monthlyTotals && Object.entries(monthlyTotals).map(([month, amount]) => (
                <div className="summary-card" key={month}>
                    <div className="summary-title">
                        {new Date(0, month - 1).toLocaleString('default', {month: 'long'})}
                    </div>
                    <div className="summary-value">{formatCurrency(amount)}</div>
                </div>
            ))}
        </div>
    );
};

// Spending By Category Component
const SpendingByCategory = ({categoryTotals}) => {
    const chartRef = React.useRef(null);
    const [chartInstance, setChartInstance] = React.useState(null);

    // Create chart when data changes
    React.useEffect(() => {
        if (chartRef.current && categoryTotals) {
            // Destroy previous chart if it exists
            if (chartInstance) {
                chartInstance.destroy();
            }

            // Prepare data for chart - sort by amount and take top 10
            const sortedCategories = Object.entries(categoryTotals)
                .sort((a, b) => b[1] - a[1])  // Sort by amount (descending)
                .slice(0, 10);  // Take only top 10

            const categories = sortedCategories.map(item => item[0]);
            const amounts = sortedCategories.map(item => item[1]);

            // Create new chart
            const ctx = chartRef.current.getContext('2d');
            const newChartInstance = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: categories,
                    datasets: [{
                        data: amounts,
                        backgroundColor: [
                            '#3498db', '#2ecc71', '#e74c3c', '#f39c12', '#9b59b6',
                            '#1abc9c', '#d35400', '#34495e', '#16a085', '#c0392b'
                        ],
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    indexAxis: 'y',  // Horizontal bar chart
                    plugins: {
                        legend: {
                            display: false  // Hide legend as it's not needed for a single dataset
                        },
                        title: {
                            display: true,
                            text: 'Top 10 Spending Categories'
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return formatCurrency(context.raw);
                                }
                            }
                        }
                    },
                    scales: {
                        x: {
                            ticks: {
                                callback: function(value) {
                                    return formatCurrency(value);
                                }
                            }
                        }
                    }
                }
            });

            setChartInstance(newChartInstance);
        }
    }, [categoryTotals]);

    return (
        <div className="chart-container">
            <canvas ref={chartRef}></canvas>
        </div>
    );
};

// Collapsible Card Component
const CollapsibleCard = ({ title, children, defaultExpanded = true }) => {
    const [isExpanded, setIsExpanded] = React.useState(defaultExpanded);

    const toggleExpand = () => {
        setIsExpanded(!isExpanded);
    };

    return (
        <div className="card mb-4">
            <div className="card-header d-flex justify-content-between align-items-center">
                <span>{title}</span>
                <button 
                    className="btn btn-sm btn-link p-0" 
                    onClick={toggleExpand}
                    aria-expanded={isExpanded}
                    aria-controls="collapsible-content"
                >
                    <i className={`bi ${isExpanded ? 'bi-chevron-up' : 'bi-chevron-down'}`}></i>
                </button>
            </div>
            {isExpanded && (
                <div className="card-body" id="collapsible-content">
                    {children}
                </div>
            )}
        </div>
    );
};

// Main App Component
const App = () => {
    const [transactions, setTransactions] = React.useState([]);
    const [totalAmount, setTotalAmount] = React.useState(0);
    const [monthlyTotals, setMonthlyTotals] = React.useState({});
    const [categoryTotals, setCategoryTotals] = React.useState({});
    const [loading, setLoading] = React.useState(true);
    const [error, setError] = React.useState('');
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

            // Get category totals
            const categoryResponse = await axios.get(`${API_BASE_URL}/category-totals`);
            setCategoryTotals(categoryResponse.data);
        } catch (error) {
            console.error('Error loading data:', error);
            setError('Failed to load data. Please try again later.');
        } finally {
            setLoading(false);
        }
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
    React.useEffect(() => {
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

// Add CSS for improved UX
const style = document.createElement('style');
style.textContent = `
    /* Import Bootstrap Icons */
    @import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css");

    /* Card styling improvements */
    .card {
        border-radius: 10px;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        transition: box-shadow 0.3s ease;
        overflow: hidden;
        border: none;
    }

    .card:hover {
        box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
    }

    .card-header {
          background-color: #3498db;
    color: white;
    padding: 15px 20px;
    font-weight: bold;
    }

    .card-body {
        padding: 20px;
    }

    /* Category card specific styling */
    .category-card .card-header {
                  background-color: #3498db;
    color: white;
    padding: 15px 20px;
    font-weight: bold;
    }

    .total-badge {
        font-size: 0.9rem;
        padding: 8px 12px;
        background-color: #0d6efd;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    .info-text {
        color: #6c757d;
        font-size: 0.9rem;
        padding: 8px 12px;
        background-color: #f8f9fa;
        border-radius: 5px;
        border-left: 3px solid #0d6efd;
    }

    .info-text i {
        color: #0d6efd;
        margin-right: 5px;
    }

    /* Table styling improvements */
    .table-container {
        overflow-x: auto;
        border-radius: 5px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    }

    .table {
        margin-bottom: 0;
        border-collapse: separate;
        border-spacing: 0;
        width: 100%;
    }

    .table thead th {
        background-color: #f8f9fa;
        border-top: none;
        border-bottom: 2px solid #dee2e6;
        padding: 12px 15px;
        font-weight: 600;
        color: #495057;
    }

    .table tbody td {
        padding: 12px 15px;
        vertical-align: middle;
        border-top: 1px solid #dee2e6;
    }

    /* Category table styling */
    .category-table {
        border-collapse: separate;
        border-spacing: 0;
    }

    .category-table th {
        position: relative;
        padding: 12px 15px;
    }

    .category-column {
        width: 40%;
    }

    .amount-column {
        width: 25%;
    }

    .percentage-column {
        width: 35%;
    }

    .sort-icon {
        color: #6c757d;
        font-size: 0.9rem;
    }

    .sortable-header:hover .sort-icon {
        color: #007bff;
    }

    /* Total summary styling */
    .total-summary {
        display: flex;
        align-items: center;
        background-color: #f8f9fa;
        padding: 8px 15px;
        border-radius: 5px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    }

    .total-label {
        font-weight: 600;
        color: #495057;
        margin-right: 10px;
    }

    .total-badge {
        font-size: 1rem;
        padding: 8px 12px;
    }

    /* Category row styling */
    .category-row {
        cursor: pointer;
        transition: all 0.2s ease;
    }

    .category-row:hover, .category-row.hovered {
        background-color: #f0f7ff;
        transform: translateY(-2px);
        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
    }

    .category-row:hover td, .category-row.hovered td {
        font-weight: bold;
        color: #007bff;
    }

    .category-row:active {
        transform: translateY(0);
        background-color: #e6f0ff;
    }

    .category-color-indicator {
        width: 12px;
        height: 12px;
        border-radius: 50%;
        display: inline-block;
        flex-shrink: 0;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
        transition: transform 0.2s ease;
    }

    .category-row:hover .category-color-indicator, 
    .category-row.hovered .category-color-indicator {
        transform: scale(1.2);
    }

    .category-name {
        font-weight: 500;
    }

    .click-icon {
        opacity: 0;
        font-size: 0.8rem;
        color: #6c757d;
        transition: opacity 0.2s ease;
    }

    .category-row:hover .click-icon, .category-row.hovered .click-icon {
        opacity: 1;
        color: #007bff;
    }

    .amount-cell {
        font-weight: 500;
        text-align: right;
    }

    /* Progress bar styling */
    .percentage-cell {
        padding-right: 15px;
    }

    .progress {
        height: 8px;
        background-color: #e9ecef;
        border-radius: 4px;
        overflow: hidden;
    }

    .progress-bar {
        background-color: #007bff;
        transition: width 0.6s ease;
    }

    .category-row:hover .progress-bar, .category-row.hovered .progress-bar {
        background-color: #0056b3;
    }

    .percentage-value {
        font-size: 0.85rem;
        font-weight: 600;
        color: #6c757d;
        min-width: 50px;
        text-align: right;
    }

    .category-row:hover .percentage-value, .category-row.hovered .percentage-value {
        color: #007bff;
    }

    /* Transaction table styling */
    .transaction-table thead th {
        background-color: #f0f7ff;
    }

    .transaction-row:hover {
        background-color: #f8f9fa;
    }

    .date-cell {
        white-space: nowrap;
        color: #495057;
    }

    .description-cell {
        max-width: 300px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    /* Sortable header styling */
    .sortable-header {
        position: relative;
        user-select: none;
        cursor: pointer;
        transition: all 0.2s ease;
    }

    .sortable-header:hover {
        background-color: #e9ecef;
        color: #007bff;
    }

    .sortable-header i {
        color: #007bff;
    }

    /* Modal styling improvements */
    .modal {
        backdrop-filter: blur(5px);
    }

    .modal-content {
        border-radius: 10px;
        border: none;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
        animation: modalFadeIn 0.3s ease;
    }

    .modal-header {
        border-bottom: 1px solid rgba(0, 0, 0, 0.1);
        color: white;
        padding: 15px 20px;
        background-color: #f8f9fa;
    }

    .modal-title {
        font-weight: 600;
        color: #495057;
        display: flex;
        align-items: center;
    }

    .modal-title i {
        color: #0d6efd;
    }

    .transaction-count {
        color: #6c757d;
    }

    .category-total {
        font-size: 0.9rem;
        color: #6c757d;
    }

    .category-total .fw-bold {
        color: #0d6efd;
    }

    .sort-instructions {
        color: #6c757d;
        font-size: 0.9rem;
        padding: 8px 12px;
        background-color: #f8f9fa;
        border-radius: 5px;
        display: inline-block;
    }

    .modal-body {
        padding: 20px;
    }

    .modal-footer {
        border-top: 1px solid rgba(0, 0, 0, 0.1);
        padding: 15px 20px;
        background-color: #f8f9fa;
    }

    /* Animation for modal */
    @keyframes modalFadeIn {
        from {
            opacity: 0;
            transform: translateY(-20px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    /* Collapsible Card Styles */
    .card-header .btn-link {
        color: white;
        text-decoration: none;
        transition: all 0.2s ease;
    }

    .card-header .btn-link:hover {
        transform: scale(1.2);
    }

    .card-header .btn-link i {
        font-size: 1.2rem;
    }

    /* Animation for collapsible content */
    #collapsible-content {
        animation: fadeIn 0.3s ease;
    }

    @keyframes fadeIn {
        from {
            opacity: 0;
            transform: translateY(-10px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    /* Responsive improvements */
    @media (max-width: 768px) {
        .card-body {
            padding: 15px;
        }

        .table thead th, 
        .table tbody td {
            padding: 10px;
        }

        .modal-dialog {
            margin: 10px;
        }

        .description-cell {
            max-width: 150px;
        }

        .modal-header {
            flex-direction: column;
            align-items: flex-start;
        }

        .modal-header .d-flex {
            margin-bottom: 10px;
            width: 100%;
        }

        .modal-header .btn-close {
            position: absolute;
            top: 15px;
            right: 15px;
        }
    }
`;
document.head.appendChild(style);

// Render the App component to the DOM
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App/>);
