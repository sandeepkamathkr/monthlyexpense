const SpendingByCategory = ({categoryTotals}) => {
    const chartRef = React.useRef(null);
    const [chartInstance, setChartInstance] = React.useState(null);
    const { formatCurrency } = window.Formatters;

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

// Export the component
window.SpendingByCategory = SpendingByCategory;