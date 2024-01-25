# pull official base image
FROM node:18-alpine AS nebulaStreamUIBuilder
LABEL builder=true

# set working directory
WORKDIR /app

# add `/app/node_modules/.bin` to $PATH
ENV PATH /app/node_modules/.bin:$PATH

# install app dependencies
COPY package.json tsconfig.json ./
RUN npm cache clean --force
RUN npm i 

# add app
COPY . ./
RUN npm install -g typescript
RUN npm link typescript
EXPOSE 3000
EXPOSE 8080 
EXPOSE 8443
#CMD ["npm", "start"]

RUN npm run build

#FROM golang:alpine3.15 AS binaryBuilder
#LABEL builder=true

# copy the build directory
#COPY --from=nebulaStreamUIBuilder /app /app

# set working directory
#WORKDIR /app

# build the binary
#RUN go build nebulaStreamUI.go

#FROM alpine:3.15

# copy the binary
#COPY --from=binaryBuilder /app/nebulaStreamUI  /app/nebulaStreamUI
#EXPOSE 3000
#EXPOSE 8080 
#EXPOSE 8443

# start app
#CMD ["./app/nebulaStreamUI", "3000"]

# Bundle static assets with nginx
FROM nginx:1.23.2-alpine as production
ENV NODE_ENV production
# Copy built assets from builder
COPY --from=nebulaStreamUIBuilder /app/build /usr/share/nginx/html
# Add your nginx.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Expose port
EXPOSE 3000
# Start nginx
CMD ["nginx", "-g", "daemon off;"]
