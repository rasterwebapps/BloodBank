# Stage 1: Build
FROM node:22-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npx ng build --configuration=production

# Stage 2: Serve
FROM nginx:alpine-slim
RUN addgroup -S nginx-app && adduser -S nginx-app -G nginx-app
COPY --chown=nginx-app:nginx-app --from=builder /app/dist/bloodbank-ui/browser /usr/share/nginx/html
COPY --chown=nginx-app:nginx-app nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget -q --spider http://localhost/health || exit 1
