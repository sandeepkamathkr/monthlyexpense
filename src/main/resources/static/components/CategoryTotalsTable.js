const CategoryTotalsTable = ({categoryTotals, transactions}) => {
    const [showModal, setShowModal] = React.useState(false);
    const [selectedCategory, setSelectedCategory] = React.useState('');
    const [sortField, setSortField] = React.useState('amount');
    const [sortDirection, setSortDirection] = React.useState('desc');
    const [hoveredCategory, setHoveredCategory] = React.useState(null);
    const { formatCurrency } = window.Formatters;
    const CategoryTransactionsModal = window.CategoryTransactionsModal;

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

// Export the component
window.CategoryTotalsTable = CategoryTotalsTable;