import React, { useState, useEffect, useRef, useCallback } from 'react';
import axios from 'axios';

const API = '/api/transactions';

const SUPPORTED_CURRENCIES = ['AUD', 'USD', 'EUR', 'GBP', 'INR', 'SGD', 'CAD', 'CHF'];

const formatAmt = (n, currency = 'AUD') => {
    try {
        return new Intl.NumberFormat('en-AU', { style: 'currency', currency }).format(n);
    } catch {
        return `${currency} ${Number(n).toFixed(2)}`;
    }
};

const formatDate = (d) => {
    if (!d) return '';
    const dt = new Date(d);
    return dt.toLocaleDateString('en-AU', { day: '2-digit', month: 'short', year: 'numeric' });
};

// ── Step indicator ──────────────────────────────────────────────────────────
const StepBar = ({ step }) => (
    <div className="d-flex align-items-center mb-4" style={{ gap: 0 }}>
        {[1, 2, 3].map((s, i) => (
            <React.Fragment key={s}>
                <div className="d-flex flex-column align-items-center" style={{ minWidth: 80 }}>
                    <div style={{
                        width: 32, height: 32, borderRadius: '50%',
                        background: step >= s ? 'var(--accent)' : 'var(--line)',
                        color: step >= s ? '#fff' : 'var(--muted)',
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                        fontWeight: 600, fontSize: 13, transition: 'background 0.2s',
                    }}>{s}</div>
                    <span style={{ fontSize: 11, color: step >= s ? 'var(--accent)' : 'var(--muted)', marginTop: 4 }}>
                        {['Upload', 'Review', 'Queue'][s - 1]}
                    </span>
                </div>
                {i < 2 && (
                    <div style={{
                        flex: 1, height: 2, marginBottom: 16,
                        background: step > s ? 'var(--accent)' : 'var(--line)',
                        transition: 'background 0.2s',
                    }} />
                )}
            </React.Fragment>
        ))}
    </div>
);

// ── Stat pill ───────────────────────────────────────────────────────────────
const Pill = ({ icon, label, value }) => (
    <div style={{
        background: 'var(--accent-soft)', border: '1px solid var(--line)',
        borderRadius: 8, padding: '10px 16px', flex: 1, minWidth: 130,
    }}>
        <div style={{ fontSize: 11, color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.07em' }}>
            <i className={`bi ${icon} me-1`}></i>{label}
        </div>
        <div style={{ fontSize: 18, fontWeight: 700, color: 'var(--ink)', marginTop: 2 }}>{value}</div>
    </div>
);

// ── Main component ──────────────────────────────────────────────────────────
const ImportPage = ({ onBack }) => {
    // ── Step 1 state ────────────────────────────────────────────────────────
    const [step, setStep] = useState(1);
    const [currency, setCurrency] = useState('AUD');
    const [files, setFiles] = useState([]); // [{ file, status, error }]
    const [processing, setProcessing] = useState(false);
    const [dragOver, setDragOver] = useState(false);
    const fileInputRef = useRef(null);

    // ── Step 2 state ────────────────────────────────────────────────────────
    const [allRows, setAllRows] = useState([]); // TransactionDTO + { _id, _excluded, _srcFile }
    const [filterDesc, setFilterDesc] = useState('');
    const [filterCat, setFilterCat] = useState('');
    const [filterSrc, setFilterSrc] = useState('');
    const [selectedIds, setSelectedIds] = useState(new Set());
    const [bulkCategory, setBulkCategory] = useState('');

    // ── Step 3 state ────────────────────────────────────────────────────────
    const [monthCounts, setMonthCounts] = useState({}); // "YYYY/MMM" → count
    const [queuing, setQueuing] = useState(false);
    const [queueError, setQueueError] = useState(null);
    const [queuedCount, setQueuedCount] = useState(0);

    // ── Failed imports state ─────────────────────────────────────────────────
    const [failures, setFailures] = useState([]);
    const [failuresOpen, setFailuresOpen] = useState(true);
    const [allCategories, setAllCategories] = useState([]);

    // Load failures and categories on mount
    useEffect(() => {
        axios.get(`${API}/import-failures`)
            .then(r => setFailures(r.data))
            .catch(() => {});
        axios.get(`${API}/categories`)
            .then(r => setAllCategories(r.data))
            .catch(() => {});
    }, []);

    // ── Drag-drop helpers ────────────────────────────────────────────────────
    const addFiles = useCallback((newFiles) => {
        const entries = Array.from(newFiles)
            .filter(f => f.name.toLowerCase().endsWith('.csv'))
            .map(f => ({ file: f, status: 'pending', error: null }));
        if (entries.length === 0) return;
        setFiles(prev => {
            const existing = new Set(prev.map(e => e.file.name));
            return [...prev, ...entries.filter(e => !existing.has(e.file.name))];
        });
    }, []);

    const onDrop = (e) => {
        e.preventDefault();
        setDragOver(false);
        addFiles(e.dataTransfer.files);
    };

    const removeFile = (name) =>
        setFiles(prev => prev.filter(e => e.file.name !== name));

    // ── Step 1 → Step 2: parse files ─────────────────────────────────────────
    const handleProcessFiles = async () => {
        if (files.length === 0) return;
        setProcessing(true);
        const parsed = [];

        for (let i = 0; i < files.length; i++) {
            const entry = files[i];
            setFiles(prev => prev.map((e, idx) =>
                idx === i ? { ...e, status: 'processing' } : e
            ));
            try {
                const fd = new FormData();
                fd.append('file', entry.file);
                fd.append('currency', currency);
                const res = await axios.post(`${API}/parse`, fd);
                const rows = res.data.map((tx, j) => ({
                    ...tx,
                    _id: `${entry.file.name}-${j}`,
                    _excluded: false,
                    _srcFile: entry.file.name,
                    _originalCategory: tx.category,
                    _hasOverride: tx.hasOverride === true,
                }));
                parsed.push(...rows);
                setFiles(prev => prev.map((e, idx) =>
                    idx === i ? { ...e, status: 'done', count: rows.length } : e
                ));
            } catch (err) {
                const msg = err.response?.data?.message || err.response?.data || err.message;
                setFiles(prev => prev.map((e, idx) =>
                    idx === i ? { ...e, status: 'error', error: String(msg) } : e
                ));
            }
        }

        setProcessing(false);
        if (parsed.length > 0) {
            setAllRows(parsed);
            setStep(2);
        }
    };

    // ── Step 2: filtering & selection ────────────────────────────────────────
    const srcFiles = [...new Set(allRows.map(r => r._srcFile))];

    const filtered = allRows.filter(r => {
        if (filterDesc && !r.description.toLowerCase().includes(filterDesc.toLowerCase())) return false;
        if (filterCat && r.category !== filterCat) return false;
        if (filterSrc && r._srcFile !== filterSrc) return false;
        return true;
    });

    const toggleSelect = (id) => {
        setSelectedIds(prev => {
            const s = new Set(prev);
            s.has(id) ? s.delete(id) : s.add(id);
            return s;
        });
    };

    const toggleSelectAll = () => {
        if (selectedIds.size === filtered.length) {
            setSelectedIds(new Set());
        } else {
            setSelectedIds(new Set(filtered.map(r => r._id)));
        }
    };

    const applyBulkCategory = () => {
        if (!bulkCategory) return;
        setAllRows(prev => prev.map(r =>
            selectedIds.has(r._id) ? { ...r, category: bulkCategory } : r
        ));
        setSelectedIds(new Set());
        setBulkCategory('');
    };

    const updateCategory = (id, cat) =>
        setAllRows(prev => prev.map(r => r._id === id ? { ...r, category: cat } : r));

    const toggleExclude = (id) =>
        setAllRows(prev => prev.map(r => r._id === id ? { ...r, _excluded: !r._excluded } : r));

    // ── Step 2 → Step 3: check conflicts ──────────────────────────────────────
    const activeRows = allRows.filter(r => !r._excluded);

    // Derive unique year/month groups from active rows
    const monthGroups = () => {
        const groups = {};
        activeRows.forEach(r => {
            const d = new Date(r.date);
            const year = d.getFullYear();
            const month = d.getMonth() + 1; // 1-12
            const abbr = d.toLocaleString('en', { month: 'short' }); // Jan, Feb, ...
            const key = `${year}/${abbr}`;
            if (!groups[key]) groups[key] = { year, month, abbr, rows: [] };
            groups[key].rows.push(r);
        });
        return groups;
    };

    const handleGoToStep3 = async () => {
        if (activeRows.length === 0) return;
        const groups = monthGroups();
        const counts = {};
        await Promise.all(
            Object.entries(groups).map(async ([key, { year, month }]) => {
                try {
                    const res = await axios.get(`${API}/month-count`, { params: { month, year } });
                    counts[key] = res.data.count;
                } catch {
                    counts[key] = 0;
                }
            })
        );
        setMonthCounts(counts);
        setQueueError(null);
        setQueuedCount(0);
        setStep(3);
    };

    // ── Step 3: queue ─────────────────────────────────────────────────────────
    const hasConflicts = Object.values(monthCounts).some(c => c > 0);

    // Group active rows by source file (one staged file per uploaded CSV)
    const fileGroups = () => {
        const groups = {};
        activeRows.forEach(r => {
            if (!groups[r._srcFile]) groups[r._srcFile] = [];
            groups[r._srcFile].push(r);
        });
        return groups;
    };

    const handleQueue = async () => {
        setQueuing(true);
        setQueueError(null);
        // Stage each source file separately — one uploaded CSV = one staged file
        const groups = fileGroups();
        // Track which months have already been cleaned up to avoid deleting a just-staged file
        const cleanedMonths = new Set();
        try {
            let count = 0;
            for (const [srcFile, rows] of Object.entries(groups)) {
                const firstDate = new Date(rows[0].date);
                const monthKey = `${firstDate.getFullYear()}/${firstDate.toLocaleString('en', { month: 'short' })}`;
                const skipCleanup = cleanedMonths.has(monthKey);
                cleanedMonths.add(monthKey);

                const payload = rows.map(r => ({
                    date: r.date,
                    description: r.description,
                    amount: r.amount,
                    currency: r.currency || currency,
                    category: r.category,
                }));
                await axios.post(`${API}/stage`, payload, {
                    params: { filename: srcFile, skipCleanup },
                });
                count++;
            }
            setQueuedCount(count);

            // Save any user-changed categories as permanent override rules (best-effort)
            const changed = activeRows.filter(r => r.category !== r._originalCategory);
            if (changed.length > 0) {
                axios.post(`${API}/category-overrides`, changed.map(r => ({
                    description: r.description,
                    category: r.category,
                }))).catch(err => console.warn('Override save failed (non-fatal):', err));
            }
        } catch (err) {
            const msg = err.response?.data?.message || err.response?.data || err.message;
            setQueueError(String(msg));
        } finally {
            setQueuing(false);
        }
    };

    // ── Failed imports helpers ────────────────────────────────────────────────
    const handleDownloadFailed = (id) => {
        window.open(`${API}/import-failures/${id}/download`, '_blank');
    };

    const handleDismissFailed = async (id) => {
        try {
            await axios.delete(`${API}/import-failures/${id}`);
            setFailures(prev => prev.filter(f => f.id !== id));
        } catch { }
    };

    // ── Render ────────────────────────────────────────────────────────────────
    return (
        <div className="container mt-4">
            {/* ── Appbar ── */}
            <div className="appbar">
                <h1>
                    <i className="bi bi-upload" style={{ color: 'var(--accent)' }}></i>
                    Import Transactions
                </h1>
                <div className="actions">
                    <button className="btn btn-outline-primary btn-sm" onClick={onBack}>
                        <i className="bi bi-arrow-left me-1"></i>Back to Dashboard
                    </button>
                </div>
            </div>

            {/* ── Failed imports panel ── */}
            {failures.length > 0 && (
                <div className="card mb-4" style={{ border: '1px solid #fcd4d1' }}>
                    <div
                        className="card-header"
                        style={{ cursor: 'pointer', background: '#fff5f5' }}
                        onClick={() => setFailuresOpen(o => !o)}
                    >
                        <span style={{ color: 'var(--danger)', fontWeight: 600 }}>
                            <i className="bi bi-exclamation-triangle-fill me-2"></i>
                            Failed Imports ({failures.length})
                        </span>
                        <i className={`bi ${failuresOpen ? 'bi-chevron-up' : 'bi-chevron-down'}`}
                            style={{ color: 'var(--muted)' }}></i>
                    </div>
                    {failuresOpen && (
                        <div className="card-body p-0">
                            <div className="table-responsive">
                                <table className="table table-sm mb-0" style={{ fontSize: 13 }}>
                                    <thead style={{ background: 'var(--bg)' }}>
                                        <tr>
                                            <th style={{ padding: '8px 14px', color: 'var(--muted)', fontWeight: 600 }}>File</th>
                                            <th style={{ padding: '8px 14px', color: 'var(--muted)', fontWeight: 600 }}>Error</th>
                                            <th style={{ padding: '8px 14px', color: 'var(--muted)', fontWeight: 600 }}>Failed at</th>
                                            <th style={{ padding: '8px 14px', color: 'var(--muted)', fontWeight: 600 }}>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {failures.map(f => (
                                            <tr key={f.id}>
                                                <td style={{ padding: '8px 14px', fontFamily: 'monospace', fontSize: 12 }}>{f.filePath}</td>
                                                <td style={{ padding: '8px 14px', color: 'var(--danger)', maxWidth: 380 }}>{f.errorMessage}</td>
                                                <td style={{ padding: '8px 14px', color: 'var(--muted)', whiteSpace: 'nowrap' }}>
                                                    {f.failedAt ? new Date(f.failedAt).toLocaleString('en-AU') : '—'}
                                                </td>
                                                <td style={{ padding: '8px 14px', whiteSpace: 'nowrap' }}>
                                                    <button
                                                        className="btn btn-sm btn-outline-primary me-2"
                                                        style={{ fontSize: 12 }}
                                                        onClick={() => handleDownloadFailed(f.id)}
                                                    >
                                                        <i className="bi bi-download me-1"></i>Download
                                                    </button>
                                                    <button
                                                        className="btn btn-sm"
                                                        style={{ fontSize: 12, color: 'var(--muted)', border: '1px solid var(--line)' }}
                                                        onClick={() => handleDismissFailed(f.id)}
                                                    >
                                                        Dismiss
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                            <div style={{ padding: '8px 14px 12px', color: 'var(--muted)', fontSize: 12 }}>
                                <i className="bi bi-info-circle me-1"></i>
                                Download the file, fix it on your computer, then re-upload using the Import steps below.
                            </div>
                        </div>
                    )}
                </div>
            )}

            {/* ── Step progress bar ── */}
            <StepBar step={step} />

            {/* ════════════════════════════════════════════════════════════════
                STEP 1 — Upload
                ════════════════════════════════════════════════════════════════ */}
            {step === 1 && (
                <div className="card">
                    <div className="card-body">
                        {/* Drag-drop zone */}
                        <div
                            onDrop={onDrop}
                            onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
                            onDragLeave={() => setDragOver(false)}
                            onClick={() => fileInputRef.current?.click()}
                            style={{
                                border: `2px dashed ${dragOver ? 'var(--accent)' : 'var(--line)'}`,
                                borderRadius: 'var(--radius)',
                                background: dragOver ? 'var(--accent-soft)' : 'var(--bg)',
                                padding: '40px 24px',
                                textAlign: 'center',
                                cursor: 'pointer',
                                transition: 'all 0.15s',
                                marginBottom: 20,
                            }}
                        >
                            <i className="bi bi-cloud-upload" style={{ fontSize: 36, color: 'var(--accent)', display: 'block', marginBottom: 10 }}></i>
                            <div style={{ fontWeight: 600, color: 'var(--ink)', marginBottom: 4 }}>
                                Drag &amp; drop CSV files here
                            </div>
                            <div style={{ color: 'var(--muted)', fontSize: 13 }}>
                                or click to browse · Supports CommBank, HSBC, and any configured bank format
                            </div>
                            <input
                                ref={fileInputRef}
                                type="file"
                                accept=".csv"
                                multiple
                                style={{ display: 'none' }}
                                onChange={e => addFiles(e.target.files)}
                            />
                        </div>

                        {/* Currency selector */}
                        <div className="d-flex align-items-center gap-3 mb-3" style={{ flexWrap: 'wrap' }}>
                            <label style={{ fontSize: 13, color: 'var(--muted)', whiteSpace: 'nowrap', marginBottom: 0 }}>
                                Currency:
                            </label>
                            <select
                                className="form-select form-select-sm"
                                style={{ width: 120 }}
                                value={currency}
                                onChange={e => setCurrency(e.target.value)}
                            >
                                {SUPPORTED_CURRENCIES.map(c => (
                                    <option key={c} value={c}>{c}</option>
                                ))}
                            </select>
                        </div>

                        {/* File list */}
                        {files.length > 0 && (
                            <div className="mb-3">
                                {files.map(({ file, status, error, count }) => (
                                    <div
                                        key={file.name}
                                        className="d-flex align-items-center justify-content-between"
                                        style={{
                                            padding: '8px 12px',
                                            borderRadius: 6,
                                            marginBottom: 6,
                                            background: status === 'error' ? '#fff5f5' : 'var(--bg)',
                                            border: '1px solid var(--line)',
                                        }}
                                    >
                                        <div style={{ display: 'flex', alignItems: 'center', gap: 10, flex: 1, minWidth: 0 }}>
                                            <i className="bi bi-file-earmark-text" style={{ color: 'var(--accent)', flexShrink: 0 }}></i>
                                            <span style={{ fontSize: 13, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                                {file.name}
                                            </span>
                                            <span style={{ fontSize: 11, color: 'var(--muted)', whiteSpace: 'nowrap' }}>
                                                ({(file.size / 1024).toFixed(1)} KB)
                                            </span>
                                        </div>
                                        <div className="d-flex align-items-center gap-2" style={{ flexShrink: 0 }}>
                                            {status === 'pending' && (
                                                <span className="badge" style={{ background: 'var(--line)', color: 'var(--muted)', fontSize: 11 }}>Pending</span>
                                            )}
                                            {status === 'processing' && (
                                                <span className="badge" style={{ background: 'var(--accent-soft)', color: 'var(--accent)', fontSize: 11 }}>
                                                    <span className="spinner-border spinner-border-sm me-1" style={{ width: 10, height: 10 }}></span>
                                                    Processing
                                                </span>
                                            )}
                                            {status === 'done' && (
                                                <span className="badge" style={{ background: '#e6f4ec', color: 'var(--good)', fontSize: 11 }}>
                                                    <i className="bi bi-check-circle-fill me-1"></i>{count} rows
                                                </span>
                                            )}
                                            {status === 'error' && (
                                                <span title={error} className="badge" style={{ background: '#fde8e7', color: 'var(--danger)', fontSize: 11, maxWidth: 220, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                                    <i className="bi bi-exclamation-circle me-1"></i>{error}
                                                </span>
                                            )}
                                            {status === 'pending' && (
                                                <button
                                                    className="btn btn-sm"
                                                    style={{ padding: '1px 6px', color: 'var(--muted)', border: 'none' }}
                                                    onClick={e => { e.stopPropagation(); removeFile(file.name); }}
                                                >
                                                    <i className="bi bi-x"></i>
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}

                        <div className="d-flex justify-content-end">
                            <button
                                className="btn btn-primary"
                                disabled={files.length === 0 || processing}
                                onClick={handleProcessFiles}
                            >
                                {processing
                                    ? <><span className="spinner-border spinner-border-sm me-2"></span>Processing…</>
                                    : <><i className="bi bi-arrow-right-circle me-1"></i>Process Files</>
                                }
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* ════════════════════════════════════════════════════════════════
                STEP 2 — Review
                ════════════════════════════════════════════════════════════════ */}
            {step === 2 && (() => {
                const nonExcluded = allRows.filter(r => !r._excluded);
                const total = nonExcluded.reduce((s, r) => s + Number(r.amount), 0);
                const dates = nonExcluded.map(r => new Date(r.date)).filter(d => !isNaN(d));
                const minDate = dates.length ? new Date(Math.min(...dates)) : null;
                const maxDate = dates.length ? new Date(Math.max(...dates)) : null;
                const dateRange = minDate && maxDate
                    ? `${formatDate(minDate)} – ${formatDate(maxDate)}`
                    : '—';

                return (
                    <div className="card">
                        <div className="card-body">
                            {/* Stat pills */}
                            <div className="d-flex gap-3 mb-4 flex-wrap">
                                <Pill icon="bi-files" label="Files" value={srcFiles.length} />
                                <Pill icon="bi-list-ul" label="Transactions" value={nonExcluded.length} />
                                <Pill icon="bi-cash-stack" label="Total" value={formatAmt(total, currency)} />
                                <Pill icon="bi-calendar3" label="Date Range" value={dateRange} />
                            </div>

                            {/* Filter bar */}
                            <div className="d-flex gap-2 mb-3 flex-wrap">
                                <input
                                    className="form-control form-control-sm"
                                    style={{ maxWidth: 200 }}
                                    placeholder="Search description…"
                                    value={filterDesc}
                                    onChange={e => setFilterDesc(e.target.value)}
                                />
                                <select
                                    className="form-select form-select-sm"
                                    style={{ maxWidth: 180 }}
                                    value={filterCat}
                                    onChange={e => setFilterCat(e.target.value)}
                                >
                                    <option value="">All categories</option>
                                    {allCategories.map(c => <option key={c} value={c}>{c}</option>)}
                                </select>
                                {srcFiles.length > 1 && (
                                    <select
                                        className="form-select form-select-sm"
                                        style={{ maxWidth: 200 }}
                                        value={filterSrc}
                                        onChange={e => setFilterSrc(e.target.value)}
                                    >
                                        <option value="">All files</option>
                                        {srcFiles.map(f => <option key={f} value={f}>{f}</option>)}
                                    </select>
                                )}
                            </div>

                            {/* Bulk action bar */}
                            {selectedIds.size > 0 && (
                                <div
                                    className="d-flex align-items-center gap-3 mb-3 p-2"
                                    style={{ background: 'var(--accent-soft)', borderRadius: 6, flexWrap: 'wrap' }}
                                >
                                    <span style={{ fontSize: 13, color: 'var(--accent)', fontWeight: 600 }}>
                                        {selectedIds.size} selected
                                    </span>
                                    <span style={{ color: 'var(--muted)', fontSize: 13 }}>· Change category:</span>
                                    <select
                                        className="form-select form-select-sm"
                                        style={{ width: 180 }}
                                        value={bulkCategory}
                                        onChange={e => setBulkCategory(e.target.value)}
                                    >
                                        <option value="">Pick category…</option>
                                        {allCategories.map(c => <option key={c} value={c}>{c}</option>)}
                                    </select>
                                    <button
                                        className="btn btn-sm btn-primary"
                                        disabled={!bulkCategory}
                                        onClick={applyBulkCategory}
                                    >
                                        Apply
                                    </button>
                                    <button
                                        className="btn btn-sm"
                                        style={{ color: 'var(--muted)', border: '1px solid var(--line)' }}
                                        onClick={() => setSelectedIds(new Set())}
                                    >
                                        Clear
                                    </button>
                                </div>
                            )}

                            {/* Transaction table */}
                            <div className="table-responsive" style={{ maxHeight: 480, overflowY: 'auto' }}>
                                <table className="table table-sm" style={{ fontSize: 13 }}>
                                    <thead style={{ background: 'var(--bg)', position: 'sticky', top: 0, zIndex: 1 }}>
                                        <tr>
                                            <th style={{ width: 36, padding: '8px' }}>
                                                <input
                                                    type="checkbox"
                                                    checked={filtered.length > 0 && selectedIds.size === filtered.length}
                                                    onChange={toggleSelectAll}
                                                />
                                            </th>
                                            <th style={{ padding: '8px 12px', color: 'var(--muted)', fontWeight: 600, whiteSpace: 'nowrap' }}>Date</th>
                                            <th style={{ padding: '8px 12px', color: 'var(--muted)', fontWeight: 600 }}>Description</th>
                                            <th style={{ padding: '8px 12px', color: 'var(--muted)', fontWeight: 600, textAlign: 'right', whiteSpace: 'nowrap' }}>Amount</th>
                                            {srcFiles.length > 1 && (
                                                <th style={{ padding: '8px 12px', color: 'var(--muted)', fontWeight: 600 }}>Source</th>
                                            )}
                                            <th style={{ padding: '8px 12px', color: 'var(--muted)', fontWeight: 600, minWidth: 160 }}>Category</th>
                                            <th style={{ padding: '8px 12px', color: 'var(--muted)', fontWeight: 600, textAlign: 'center', whiteSpace: 'nowrap' }}>Exclude</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {filtered.length === 0 ? (
                                            <tr>
                                                <td colSpan="7" className="text-center py-4" style={{ color: 'var(--muted)' }}>
                                                    <i className="bi bi-inbox fs-4 d-block mb-1"></i>
                                                    No rows match your filters
                                                </td>
                                            </tr>
                                        ) : filtered.map(row => (
                                            <tr
                                                key={row._id}
                                                style={{ opacity: row._excluded ? 0.35 : 1, transition: 'opacity 0.15s' }}
                                            >
                                                <td style={{ padding: '6px 8px' }}>
                                                    <input
                                                        type="checkbox"
                                                        checked={selectedIds.has(row._id)}
                                                        onChange={() => toggleSelect(row._id)}
                                                        disabled={row._excluded}
                                                    />
                                                </td>
                                                <td style={{ padding: '6px 12px', whiteSpace: 'nowrap', color: 'var(--ink-2)' }}>
                                                    {formatDate(row.date)}
                                                </td>
                                                <td style={{ padding: '6px 12px', maxWidth: 280, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                                    <span style={{ textDecoration: row._excluded ? 'line-through' : 'none' }}>
                                                        {row.description}
                                                    </span>
                                                </td>
                                                <td style={{ padding: '6px 12px', textAlign: 'right', whiteSpace: 'nowrap', fontWeight: 500 }}>
                                                    {formatAmt(row.amount, row.currency || currency)}
                                                </td>
                                                {srcFiles.length > 1 && (
                                                    <td style={{ padding: '6px 12px', color: 'var(--muted)', fontSize: 11, maxWidth: 130, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                                        {row._srcFile}
                                                    </td>
                                                )}
                                                <td style={{ padding: '6px 8px' }}>
                                                    <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                                        {row._hasOverride && row.category === row._originalCategory && (
                                                            <i className="bi bi-bookmark-fill" title="Saved rule applied" style={{ color: 'var(--accent)', fontSize: 11, flexShrink: 0 }} />
                                                        )}
                                                        {row.category !== row._originalCategory && (
                                                            <i className="bi bi-pencil-fill" title="Will be saved as rule" style={{ color: '#f59e0b', fontSize: 11, flexShrink: 0 }} />
                                                        )}
                                                        <select
                                                            className="form-select form-select-sm"
                                                            style={{ fontSize: 12 }}
                                                            value={row.category}
                                                            onChange={e => updateCategory(row._id, e.target.value)}
                                                            disabled={row._excluded}
                                                        >
                                                            {allCategories.map(c => <option key={c} value={c}>{c}</option>)}
                                                        </select>
                                                    </div>
                                                </td>
                                                <td style={{ padding: '6px 8px', textAlign: 'center' }}>
                                                    <button
                                                        className="btn btn-sm"
                                                        style={{
                                                            fontSize: 12, padding: '2px 8px',
                                                            background: row._excluded ? 'var(--line)' : '#fde8e7',
                                                            color: row._excluded ? 'var(--muted)' : 'var(--danger)',
                                                            border: 'none', borderRadius: 4,
                                                        }}
                                                        onClick={() => toggleExclude(row._id)}
                                                    >
                                                        {row._excluded ? 'Include' : 'Exclude'}
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>

                            <div className="d-flex justify-content-between align-items-center mt-3">
                                <button className="btn btn-sm" style={{ color: 'var(--muted)', border: '1px solid var(--line)' }} onClick={() => setStep(1)}>
                                    <i className="bi bi-arrow-left me-1"></i>Back
                                </button>
                                <button
                                    className="btn btn-primary"
                                    disabled={activeRows.length === 0}
                                    onClick={handleGoToStep3}
                                >
                                    <i className="bi bi-arrow-right-circle me-1"></i>
                                    Next — Review &amp; Queue ({activeRows.length} transactions)
                                </button>
                            </div>
                        </div>
                    </div>
                );
            })()}

            {/* ════════════════════════════════════════════════════════════════
                STEP 3 — Queue
                ════════════════════════════════════════════════════════════════ */}
            {step === 3 && (() => {
                const fGroups = fileGroups();

                return (
                    <div className="card">
                        <div className="card-body">
                            <h6 style={{ fontWeight: 600, color: 'var(--ink)', marginBottom: 4 }}>
                                Ready to queue
                            </h6>
                            <p style={{ fontSize: 12, color: 'var(--muted)', marginBottom: 12 }}>
                                Each uploaded CSV will be staged as a separate file for the scheduler.
                            </p>

                            {/* Per-file summary */}
                            <div className="mb-3">
                                {Object.entries(fGroups).map(([srcFile, rows]) => {
                                    const fileTotal = rows.reduce((s, r) => s + Number(r.amount), 0);
                                    const dates = rows.map(r => new Date(r.date)).filter(d => !isNaN(d));
                                    const minD = dates.length ? new Date(Math.min(...dates)) : null;
                                    const maxD = dates.length ? new Date(Math.max(...dates)) : null;
                                    const dateRange = minD && maxD
                                        ? `${formatDate(minD)} – ${formatDate(maxD)}`
                                        : '—';
                                    return (
                                        <div
                                            key={srcFile}
                                            style={{
                                                padding: '10px 14px', marginBottom: 6,
                                                background: 'var(--bg)', borderRadius: 6,
                                                border: '1px solid var(--line)',
                                            }}
                                        >
                                            <div className="d-flex justify-content-between align-items-center">
                                                <span style={{ fontWeight: 500, color: 'var(--ink)', fontSize: 13 }}>
                                                    <i className="bi bi-file-earmark-text me-2" style={{ color: 'var(--accent)' }}></i>
                                                    {srcFile}
                                                </span>
                                                <span style={{ color: 'var(--muted)', fontSize: 12 }}>
                                                    {rows.length} txn · {formatAmt(fileTotal, currency)}
                                                </span>
                                            </div>
                                            <div style={{ fontSize: 11, color: 'var(--muted)', marginTop: 3, paddingLeft: 22 }}>
                                                {dateRange}
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>

                            {/* Conflict warning */}
                            {hasConflicts && !queuedCount && (
                                <div
                                    className="mb-3 p-3"
                                    style={{
                                        background: 'var(--accent-soft)',
                                        border: '1px solid var(--accent)',
                                        borderRadius: 6,
                                    }}
                                >
                                    <div style={{ fontWeight: 600, color: 'var(--accent)', marginBottom: 6 }}>
                                        <i className="bi bi-exclamation-triangle-fill me-2"></i>
                                        Existing data will be replaced:
                                    </div>
                                    <ul style={{ margin: 0, paddingLeft: 20, fontSize: 13, color: 'var(--ink-2)' }}>
                                        {Object.entries(monthCounts)
                                            .filter(([, c]) => c > 0)
                                            .map(([key, count]) => (
                                                <li key={key}>{key} — {count} transaction{count !== 1 ? 's' : ''} will be deleted</li>
                                            ))}
                                    </ul>
                                    <div style={{ marginTop: 8, fontSize: 12, color: 'var(--muted)' }}>
                                        Their CSV files will also be removed from the import folder. This cannot be undone.
                                    </div>
                                </div>
                            )}

                            {/* Queue error */}
                            {queueError && (
                                <div className="alert" style={{ background: '#fff5f5', border: '1px solid #fcd4d1', color: 'var(--danger)', fontSize: 13 }}>
                                    <i className="bi bi-exclamation-circle-fill me-2"></i>{queueError}
                                </div>
                            )}

                            {/* Success */}
                            {queuedCount > 0 && (
                                <div className="alert" style={{ background: '#e6f4ec', border: '1px solid #b7dfcb', color: 'var(--good)', fontSize: 13 }}>
                                    <i className="bi bi-check-circle-fill me-2"></i>
                                    {queuedCount} file{queuedCount !== 1 ? 's' : ''} queued — will be processed within seconds.
                                    <div style={{ marginTop: 6 }}>
                                        <button className="btn btn-sm btn-primary" onClick={onBack}>
                                            <i className="bi bi-arrow-left me-1"></i>Back to Dashboard
                                        </button>
                                    </div>
                                </div>
                            )}

                            {!queuedCount && (
                                <div className="d-flex justify-content-between align-items-center mt-2">
                                    <button
                                        className="btn btn-sm"
                                        style={{ color: 'var(--muted)', border: '1px solid var(--line)' }}
                                        onClick={() => setStep(2)}
                                        disabled={queuing}
                                    >
                                        <i className="bi bi-arrow-left me-1"></i>Back
                                    </button>
                                    <button
                                        className={`btn ${hasConflicts ? 'btn-warning' : 'btn-primary'}`}
                                        style={{ minWidth: 220 }}
                                        disabled={queuing}
                                        onClick={handleQueue}
                                    >
                                        {queuing
                                            ? <><span className="spinner-border spinner-border-sm me-2"></span>Queuing…</>
                                            : <>
                                                <i className="bi bi-send me-1"></i>
                                                {hasConflicts ? 'Queue — Replace Existing Data' : 'Queue for Import'}
                                              </>
                                        }
                                    </button>
                                </div>
                            )}
                        </div>
                    </div>
                );
            })()}
        </div>
    );
};

export default ImportPage;
