# Monthly Expense Tracker - Architecture Diagram

## 0. End-to-End Data Pipeline (CSV Preprocessor → Expense Tracker)

```mermaid
flowchart LR
    subgraph Preprocessor["🐍 CSV Preprocessor (External Project)"]
        direction TB
        RAW["📁 Raw Bank CSV Files<br/>(TransHist / CommBank format)"]
        FIND["find_csv_files()<br/>skip *_Processed.csv"]
        FMT["detect_file_format()<br/>headers / no headers"]
        OUT["determine_output_format()<br/>TransHist vs CommBank columns"]
        CLEAN["clean_date()<br/>clean_amount()"]
        CAT["categorize()<br/>keyword → category"]
        FILT["Filter<br/>amount ≤ 0, missing fields<br/>is_excluded()"]
        PROC["📄 {name}_Monthly_Processed.csv"]
        SUM["Print Summary<br/>rows, total, top categories"]

        RAW --> FIND --> FMT --> OUT --> CLEAN --> CAT --> FILT --> PROC --> SUM
    end

    subgraph Tracker["💰 Monthly Expense Tracker"]
        direction TB
        VOL["📁 /app/csv-input/YYYY/MMM/"]
        SCHED["CsvSchedulerService<br/>(every 5 min)"]
        DB["PostgreSQL<br/>transactions table"]
        API["Spring Boot REST API"]
        UI["React Dashboard<br/>Charts & Tables"]

        VOL --> SCHED --> DB --> API --> UI
    end

    PROC -->|"Copy to input folder"| VOL

    style Preprocessor fill:#fff8e1,stroke:#F57F17
    style Tracker fill:#e8f5e9,stroke:#2E7D32
```

---

## 1. Overall System Architecture

```mermaid
graph TB
    subgraph Client["🌐 Client Browser"]
        UI["React SPA<br/>(Bootstrap 5, Chart.js)"]
    end

    subgraph Docker["🐳 Docker Compose / Local Dev"]
        direction TB

        subgraph FE["Frontend Container (port 80)"]
            NGINX["Nginx Web Server<br/>+ Reverse Proxy"]
            REACT["React App<br/>(static files)"]
        end

        subgraph BE["Backend Container (port 8081)"]
            CTRL["TransactionController<br/>REST API"]
            SVC["TransactionService<br/>Business Logic"]
            REPO["TransactionRepository<br/>Spring Data JPA"]
            SCHED["CsvSchedulerService<br/>(every 5 min)"]
            LOCK["ShedLock<br/>(Distributed Lock)"]
        end

        subgraph DB["Database Container (port 5432)"]
            PG["PostgreSQL 17<br/>monthly_expense_db"]
        end

        subgraph VOL["📁 Volumes"]
            CSV["CSV Input Folder<br/>/app/csv-input/YYYY/MMM/"]
            DBVOL["db_data<br/>(persistent)"]
        end
    end

    UI -->|"HTTP GET /api/*"| NGINX
    UI -->|"Serve React SPA"| NGINX
    NGINX -->|"Proxy /api/ →"| CTRL
    NGINX --> REACT

    CTRL --> SVC
    SVC --> REPO
    REPO -->|"JPA / JDBC"| PG
    SCHED -->|"Scan & import CSV files"| CSV
    SCHED --> SVC
    SCHED --> LOCK
    LOCK --> PG
    PG --- DBVOL

    style Client fill:#e8f4f8,stroke:#2196F3
    style Docker fill:#f0f7f0,stroke:#4CAF50
    style FE fill:#fff3e0,stroke:#FF9800
    style BE fill:#fce4ec,stroke:#E91E63
    style DB fill:#e8eaf6,stroke:#3F51B5
    style VOL fill:#f3e5f5,stroke:#9C27B0
```

---

## 2. Backend Internal Architecture

```mermaid
graph LR
    subgraph API["REST API Layer"]
        EP1["GET /api/transactions"]
        EP2["GET /api/transactions/month"]
        EP3["GET /api/transactions/category/{cat}"]
        EP4["GET /api/transactions/total"]
        EP5["GET /api/transactions/monthly-totals"]
        EP6["GET /api/transactions/category-totals"]
        EP7["GET /api/transactions/primary-currency"]
        EP8["DELETE /api/transactions/reset"]
    end

    subgraph Controller["TransactionController"]
        TC["@RestController<br/>CORS configured"]
    end

    subgraph Service["Service Layer"]
        TSI["TransactionServiceImpl<br/>Business Logic"]
        CU["CurrencyUtil<br/>Currency Conversion"]
    end

    subgraph Scheduler["Scheduler"]
        CSS["CsvSchedulerService<br/>@Scheduled (5 min)"]
        CFM["CsvFileManager<br/>File I/O"]
        RR["ReprocessRunner<br/>Manual reprocess"]
    end

    subgraph Repository["Repository Layer"]
        TR["TransactionRepository<br/>Spring Data JPA"]
    end

    subgraph Config["Configuration"]
        CORS["CorsConfig"]
        GEH["GlobalExceptionHandler"]
        SLC["SchedulerLockConfig"]
        VC["ValidationConfig"]
    end

    subgraph Entity["Data Model"]
        T["Transaction<br/>──────────<br/>id: BIGINT PK<br/>date: DATE<br/>description: VARCHAR<br/>amount: DECIMAL<br/>currency: VARCHAR(3)<br/>category: VARCHAR<br/>transaction_month: INT<br/>transaction_year: INT"]
        DTO["TransactionDTO"]
    end

    EP1 & EP2 & EP3 & EP4 & EP5 & EP6 & EP7 & EP8 --> TC
    TC --> TSI
    TSI --> CU
    TSI --> TR
    CSS --> CFM
    CSS --> TSI
    RR --> TSI
    TR --> T
    T --> DTO

    style API fill:#e3f2fd,stroke:#1565C0
    style Controller fill:#fce4ec,stroke:#C62828
    style Service fill:#e8f5e9,stroke:#2E7D32
    style Scheduler fill:#fff8e1,stroke:#F57F17
    style Repository fill:#f3e5f5,stroke:#6A1B9A
    style Config fill:#efebe9,stroke:#4E342E
    style Entity fill:#e0f2f1,stroke:#00695C
```

---

## 3. Frontend Component Architecture

```mermaid
graph TD
    subgraph App["App.js (Root Component)"]
        FS["FilterStrip<br/>Global Month/Year Selector"]

        subgraph Layout["DraggableZones Layout (localStorage)"]
            direction LR
            LEFT["Left Column"]
            RIGHT["Right Column"]
            BOTTOM["Full-Width Bottom"]
        end

        subgraph Components["UI Components"]
            HERO["Hero<br/>YTD Total + Sparkline"]
            CTT["CategoryTotalsTable<br/>Sortable Breakdown"]
            SBC["SpendingByCategory<br/>Bar Chart (Chart.js)"]
            TT["TransactionsTable<br/>Full Transaction List"]
            CTM["CategoryTransactionsModal<br/>Drill-down Modal"]
            MYS["MonthYearSelector<br/>Date Filter Dropdowns"]
            CC["CollapsibleCard<br/>Reusable Wrapper"]
            TP["TweaksPanel<br/>Developer Customization"]
        end
    end

    subgraph API["Axios HTTP Client"]
        AX["/api/transactions<br/>/api/transactions/month<br/>/api/transactions/category-totals<br/>/api/transactions/monthly-totals<br/>/api/transactions/total<br/>/api/transactions/primary-currency"]
    end

    subgraph State["React State (useState / useEffect)"]
        S1["transactions[]"]
        S2["categoryTotals{}"]
        S3["monthlyTotals{}"]
        S4["selectedMonth/Year"]
        S5["currency"]
        S6["layout (drag state)"]
    end

    FS --> S4
    FS --> S5
    Layout --> Components
    HERO --> S1
    CTT --> S2
    CTT --> CTM
    SBC --> S2
    TT --> S1
    MYS --> S4

    App -->|"fetch on mount/filter change"| AX
    AX --> S1
    AX --> S2
    AX --> S3
    AX --> S4
    AX --> S5

    style App fill:#e3f2fd,stroke:#1565C0
    style Components fill:#e8f5e9,stroke:#2E7D32
    style API fill:#fff3e0,stroke:#E65100
    style State fill:#fce4ec,stroke:#880E4F
    style Layout fill:#f3e5f5,stroke:#4A148C
```

---

## 4. Kubernetes / Helm Deployment Architecture

```mermaid
graph TB
    subgraph Internet["🌍 External Traffic"]
        USR["Users / Browser"]
    end

    subgraph K8s["☸️ Kubernetes Cluster"]
        direction TB

        subgraph Ingress["Nginx Ingress Controller"]
            ING["Ingress Rules<br/>/ → frontend:80<br/>/api → backend:8081"]
        end

        subgraph FENs["Frontend Namespace"]
            FESVC["frontend-service<br/>NodePort: 30080"]
            FEP["Frontend Pod<br/>Nginx + React<br/>(1 replica)"]
        end

        subgraph BENs["Backend Namespace"]
            BESVC["backend-service<br/>NodePort: 30081"]
            BEP1["Backend Pod 1<br/>Spring Boot<br/>(JVM G1GC)"]
            BEP2["Backend Pod 2<br/>Spring Boot<br/>(JVM G1GC)"]
            HPA["HorizontalPodAutoscaler<br/>(auto-scale)"]
            PVC["PersistentVolumeClaim<br/>5Gi CSV Storage"]
            JOB["Reprocess Job<br/>(manual trigger)"]
        end

        subgraph DBNs["Database Namespace"]
            PGSVC["postgresql-service"]
            PGPOD["PostgreSQL Pod<br/>(Bitnami Chart v18.1.15)"]
            PGPVC["PVC: 8Gi<br/>(persistent data)"]
        end

        subgraph Probes["Health Checks"]
            LIVE["Liveness: /actuator/health/liveness<br/>(90s delay, 20s period)"]
            READY["Readiness: /actuator/health/readiness<br/>(60s delay, 15s period)"]
        end
    end

    USR -->|"HTTPS/HTTP"| ING
    ING -->|"/"| FESVC
    ING -->|"/api"| BESVC
    FESVC --> FEP
    BESVC --> BEP1
    BESVC --> BEP2
    HPA -.->|"scales"| BEP1
    HPA -.->|"scales"| BEP2
    BEP1 & BEP2 -->|"JDBC"| PGSVC
    BEP1 & BEP2 --- PVC
    PGSVC --> PGPOD
    PGPOD --- PGPVC
    JOB --> BESVC
    BEP1 -.-> LIVE
    BEP1 -.-> READY

    style Internet fill:#e3f2fd,stroke:#1565C0
    style K8s fill:#f1f8e9,stroke:#558B2F
    style Ingress fill:#fff3e0,stroke:#E65100
    style FENs fill:#fce4ec,stroke:#AD1457
    style BENs fill:#e8f5e9,stroke:#2E7D32
    style DBNs fill:#e8eaf6,stroke:#3949AB
    style Probes fill:#f3e5f5,stroke:#6A1B9A
```

---

## 5. Data Flow: CSV Import Pipeline

```mermaid
sequenceDiagram
    participant FS as 📁 File System<br/>/app/csv-input/YYYY/MMM/
    participant SC as CsvSchedulerService<br/>(every 5 min)
    participant FM as CsvFileManager
    participant SV as TransactionService
    participant DB as PostgreSQL

    loop Every 5 minutes
        SC->>FM: Scan for CSV files
        FM->>FS: List files matching YYYY/MMM/*.csv
        FS-->>FM: bank.csv found
        FM->>SC: File list returned
        SC->>FM: Read CSV file
        FM->>FS: Read file bytes
        FS-->>FM: Raw CSV data
        FM-->>SC: Parsed CSV rows
        SC->>SV: Save transactions (batch)
        SV->>DB: INSERT INTO transactions
        DB-->>SV: Success
        SV-->>SC: Saved count
        SC->>FM: Move file to processed/
        alt On Failure
            SC->>FM: Move file to unprocessed/
        end
    end
```

---

## 6. Technology Stack Summary

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| **Frontend** | React | 18.2.0 | SPA UI framework |
| **Frontend** | Bootstrap | 5.2.3 | CSS component library |
| **Frontend** | Chart.js | 4.2.1 | Data visualization |
| **Frontend** | Axios | 1.3.4 | HTTP client |
| **Web Server** | Nginx | Alpine | Static files + reverse proxy |
| **Backend** | Java | 11 | Runtime |
| **Backend** | Spring Boot | 2.7.14 | REST API framework |
| **Backend** | Spring Data JPA | 2.7.14 | ORM / data access |
| **Backend** | OpenCSV | 5.6 | CSV parsing |
| **Backend** | ShedLock | 4.44.0 | Distributed scheduler lock |
| **Backend** | Lombok | latest | Boilerplate reduction |
| **Database** | PostgreSQL | 17 | Production database |
| **Database** | H2 | embedded | Local development |
| **Container** | Docker | 3.8 | Containerization |
| **Orchestration** | Kubernetes | - | Container orchestration |
| **Package Mgr** | Helm | - | K8s application packaging |
| **Build** | Maven | 3.x | Backend build tool |
| **Build** | npm / react-scripts | 5.0.1 | Frontend build tool |
| **CI/CD** | GitHub Actions | - | Automated workflows |
