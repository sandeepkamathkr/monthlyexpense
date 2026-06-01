Debug checklist for this Spring Boot MVC + React application.

**Backend not starting:**
- Check `docker-compose logs backend-service | tail -30`
- Verify PostgreSQL is ready: `docker-compose ps db`
- Check datasource env vars: SPRING_DATASOURCE_URL, USERNAME, PASSWORD

**CSV upload failing:**
- Date format must be dd/MM/yyyy (strict — not yyyy-MM-dd)
- Check OpenCSV bean annotations on TransactionDTO.java
- Verify column headers: Date, Description, Amount, Category (case-sensitive)

**Scheduler not processing files:**
- Confirm CSV_SCHEDULER_ENABLED=true
- Check folder structure: <csv-input>/<year>/<Mon>/<file>.csv (e.g. 2025/Jan/file.csv)
- Look for .done files (already processed) or check unprocessed/ subfolder
- ShedLock: check shedlock table in DB for stuck locks

**Frontend blank / API errors:**
- Confirm exactly 1 main JS file: `find public/static/js -name "main.*.js" | wc -l`
- Multiple main JS files crash Chrome — rebuild React and copy to public/
- Check CORS: backend CORS_ALLOWED_ORIGINS must include frontend origin

**Category totals returning wrong data:**
- /category-totals with no params = all-time; with month+year = filtered
- month/year fields on Transaction entity are derived via @PrePersist — check entity hooks