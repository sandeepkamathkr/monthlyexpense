# Monthly Expense App Helm Chart

This Helm chart deploys the Monthly Expense Tracker application, a full-stack application for tracking and managing monthly expenses.

## Chart Structure

The chart consists of the following components:

- **Main Chart**: Provides the overall application deployment and ingress configuration
- **Backend Subchart**: Deploys the Java Spring Boot backend API service
- **Frontend Subchart**: Deploys the frontend web application
- **PostgreSQL Subchart**: Deploys a PostgreSQL database using the Bitnami chart

## Prerequisites

- Kubernetes 1.19+
- Helm 3.2.0+
- Ingress controller (like NGINX Ingress Controller) installed in your cluster

## Installation

To install the chart with the release name `my-expense-app`:

```bash
# Add the Bitnami repository for PostgreSQL dependency
helm repo add bitnami https://charts.bitnami.com/bitnami

# Update dependencies
helm dependency update .

# Install the chart
helm install my-expense-app .
```

## Configuration

The following table lists the configurable parameters of the chart and their default values.

| Parameter                           | Description                                      | Default                  |
|-------------------------------------|--------------------------------------------------|--------------------------|
| `backend.replicaCount`              | Number of backend replicas                       | `2`                      |
| `frontend.replicaCount`             | Number of frontend replicas                      | `1`                      |
| `postgresql.auth.database`          | PostgreSQL database name                         | `monthly_expense_db`     |
| `postgresql.auth.username`          | PostgreSQL username                              | `postgres`               |
| `postgresql.auth.password`          | PostgreSQL password                              | `<your-secure-password>` |
| `postgresql.primary.persistence.enabled` | Enable PostgreSQL persistence               | `true`                   |
| `postgresql.primary.persistence.size`    | PostgreSQL PVC size                         | `8Gi`                    |
| `ingress.enabled`                   | Enable ingress resource                          | `true`                   |

You can specify each parameter using the `--set key=value[,key=value]` argument to `helm install`.

For example:

```bash
helm install my-expense-app ./monthly-expense-app \
  --set backend.replicaCount=3 \
  --set postgresql.auth.password=mySecurePassword
```

Alternatively, a YAML file that specifies the values for the parameters can be provided while installing the chart:

```bash
helm install my-expense-app ./monthly-expense-app -f values-custom.yaml
```

## Accessing the Application

Once deployed, the application will be available through the Ingress resource. The exact URL depends on your Ingress controller configuration and DNS setup.

The application routes are:
- Frontend UI: `/`
- Backend API: `/api/*`

## Upgrading

To upgrade the chart:

```bash
helm upgrade my-expense-app ./monthly-expense-app
```

## Uninstalling

To uninstall/delete the deployment:

```bash
helm uninstall my-expense-app
```

## Notes

- The PostgreSQL password is stored in plain text in the values.yaml file. For production deployments, consider using Kubernetes secrets or a secret management solution.
- For production use, adjust the resource requests and limits according to your workload requirements.