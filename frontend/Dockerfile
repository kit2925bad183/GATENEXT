# syntax=docker/dockerfile:1

# Stage 1: Build React SPA
FROM node:20-alpine AS builder
WORKDIR /workspace/frontend

COPY package*.json ./
RUN npm ci

COPY . ./
RUN npm run build

# Stage 2: Serve via Lightweight Nginx
FROM nginx:1.27-alpine AS runner

# Remove default nginx html
RUN rm -rf /usr/share/nginx/html/*

# Copy built frontend assets
COPY --from=builder /workspace/frontend/dist /usr/share/nginx/html

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=20s --timeout=3s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost/index.html || exit 1

CMD ["nginx", "-g", "daemon off;"]
