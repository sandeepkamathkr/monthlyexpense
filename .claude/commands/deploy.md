Deploy the application using Docker Compose.

Steps:
1. From frontend/: `rm -rf build && npx react-scripts build`
2. Copy build: `rm -rf public/static && cp -r build/* public/`
3. Verify single main JS: `find public/static/js -name "main.*.js" | wc -l` (must be 1)
4. Run: `./docker-build.sh` (from frontend/)
5. Run: `docker-compose up -d` (from project root)
6. Verify: `docker-compose ps` — all 3 services should be Up
7. Test backend: `curl -s http://localhost:8081/actuator/health`
8. Test frontend: `curl -s -o /dev/null -w "%{http_code}" http://localhost/`

For Helm (Kubernetes):
- From monthly-expense-app/: `helm upgrade my-expense-app .`