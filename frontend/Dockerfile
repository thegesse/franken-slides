ARG NODE_VERSION=22.22.2

FROM node:${NODE_VERSION} AS dev

WORKDIR /app

COPY my-react-app/package*.json ./

RUN npm ci

COPY my-react-app/ ./

RUN chown -R node:node /app

USER node

EXPOSE 5173

CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
