FROM node:14
WORKDIR /usr/src/app
COPY package*.json server.js ./
RUN npm install
EXPOSE 9090
CMD ["node", "server.js"]
