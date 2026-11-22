FROM node:24.22.1-bookworm-slim
ENV NODE_ENV=production HOST=0.0.0.0 PORT=4173 DATABASE_PATH=/app/.data/tapviz.sqlite
WORKDIR /app
COPY package.json pnpm-lock.yaml ./
RUN corepack enable && corepack prepare pnpm@11.25.0 --activate && pnpm install --prod --frozen-lockfile
COPY server.mjs ./server.mjs
COPY src ./src
COPY public ./public
RUN mkdir -p /app/.data && chown -R node:node /app
USER node
EXPOSE 4173
CMD ["node", "server.mjs"]
