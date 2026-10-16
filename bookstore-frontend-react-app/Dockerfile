# Stage 1: Build the React application
FROM node:18-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Frontend configuration is embedded at build time.
ARG REACT_APP_BACKEND_API_GATEWAY_URL=http://localhost:8765
ARG REACT_APP_STRIPE_PUBLISHABLE_KEY

ENV REACT_APP_BACKEND_API_GATEWAY_URL=${REACT_APP_BACKEND_API_GATEWAY_URL}
ENV REACT_APP_STRIPE_PUBLISHABLE_KEY=${REACT_APP_STRIPE_PUBLISHABLE_KEY}

RUN npm run build

# Stage 2: Serve the compiled frontend using Nginx
FROM nginx:stable-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
