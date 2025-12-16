FROM node:20-alpine AS build
WORKDIR /app

# 1) Dependencies (cache-friendly)
COPY package*.json ./
RUN npm ci


# 2) App sources
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
