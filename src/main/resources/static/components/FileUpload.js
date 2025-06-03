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

// Export the component
window.FileUpload = FileUpload;