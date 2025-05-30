// API base URL - change this to match your backend URL in production
const API_BASE_URL = 'http://localhost:8080/api/transactions';

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
const FileUpload = ({ onUploadSuccess }) => {
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

// Transactions Table Component
const TransactionsTable = ({ transactions }) => {
    return (
        <div className="card">
            <div className="card-header">Transactions</div>
            <div className="card-body">
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
                            {transactions.length === 0 ? (
                                <tr>
                                    <td colSpan="4" className="text-center">No transactions found</td>
                                </tr>
                            ) : (
                                transactions.map((transaction, index) => (
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
        </div>
    );
};

// Summary Component
const Summary = ({ totalAmount, monthlyTotals, categoryTotals }) => {
    const chartRef = React.useRef(null);
    const [chartInstance, setChartInstance] = React.useState(null);

    // Create chart when data changes
    React.useEffect(() => {
        if (chartRef.current && categoryTotals) {
            // Destroy previous chart if it exists
            if (chartInstance) {
                chartInstance.destroy();
            }

            // Prepare data for chart
            const categories = Object.keys(categoryTotals);
            const amounts = Object.values(categoryTotals);

            // Create new chart
            const ctx = chartRef.current.getContext('2d');
            const newChartInstance = new Chart(ctx, {
                type: 'pie',
                data: {
                    labels: categories,
                    datasets: [{
                        data: amounts,
                        backgroundColor: [
                            '#3498db', '#2ecc71', '#e74c3c', '#f39c12', '#9b59b6',
                            '#1abc9c', '#d35400', '#34495e', '#16a085', '#c0392b'
                        ]
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'right'
                        },
                        title: {
                            display: true,
                            text: 'Spending by Category'
                        }
                    }
                }
            });

            setChartInstance(newChartInstance);
        }
    }, [categoryTotals]);

    return (
        <div>
            <div className="summary-container">
                <div className="summary-card">
                    <div className="summary-title">Total Spending</div>
                    <div className="summary-value">{formatCurrency(totalAmount || 0)}</div>
                </div>
                
                {monthlyTotals && Object.entries(monthlyTotals).map(([month, amount]) => (
                    <div className="summary-card" key={month}>
                        <div className="summary-title">
                            {new Date(0, month - 1).toLocaleString('default', { month: 'long' })}
                        </div>
                        <div className="summary-value">{formatCurrency(amount)}</div>
                    </div>
                ))}
            </div>
            
            <div className="card">
                <div className="card-header">Spending by Category</div>
                <div className="card-body">
                    <div className="chart-container">
                        <canvas ref={chartRef}></canvas>
                    </div>
                </div>
            </div>
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
                    <FileUpload onUploadSuccess={loadData} />
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
                        categoryTotals={categoryTotals} 
                    />
                    <TransactionsTable transactions={transactions} />
                </div>
            )}
        </div>
    );
};

// Render the App component to the DOM
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);