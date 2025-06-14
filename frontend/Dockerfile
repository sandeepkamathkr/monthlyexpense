# Stage 1: Use a minimal image for preparing files
FROM alpine:3.16 as builder

# Set working directory
WORKDIR /app

# Copy only the necessary files
COPY public/ ./public/
COPY nginx.conf ./nginx.conf

# Stage 2: Final lightweight image
FROM nginx:alpine

# Add metadata labels
LABEL maintainer="Monthly Expense App Team"
LABEL description="Frontend for Monthly Expense Tracker"

# Copy files from builder stage
COPY --from=builder /app/public/ /usr/share/nginx/html/
COPY --from=builder /app/nginx.conf /etc/nginx/conf.d/default.conf

# Clean up nginx default logs to save space and use stdout/stderr instead
RUN ln -sf /dev/stdout /var/log/nginx/access.log && \
    ln -sf /dev/stderr /var/log/nginx/error.log

# Expose port 80 (Nginx default)
EXPOSE 80

# Start Nginx when the container launches
CMD ["nginx", "-g", "daemon off;"]
