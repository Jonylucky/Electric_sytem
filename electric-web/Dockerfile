# ===== Stage 1: Build React =====
FROM --platform=linux/amd64 node:20-bookworm AS build
WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# ===== Stage 2: Run Nginx =====
FROM --platform=linux/amd64 nginx:1.27-bookworm

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]