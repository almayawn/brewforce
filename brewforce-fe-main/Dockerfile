# Use official Node.js image as the base image
FROM node:20-alpine AS builder

# Accept environment variables for frontend
ARG NEXT_PUBLIC_API_URL_BE
ARG NEXT_PUBLIC_API_URL_AUTH

ENV NEXT_PUBLIC_API_URL_BE=$NEXT_PUBLIC_API_URL_BE
ENV NEXT_PUBLIC_API_URL_AUTH=$NEXT_PUBLIC_API_URL_AUTH

# Set working directory
WORKDIR /app

# Copy package.json and package-lock.json (or yarn.lock)
COPY package.json package-lock.json ./

# Install dependencies
RUN npm install

# Copy the rest of the application
COPY . .

# Add another debug step after copying files
RUN echo "Build-time environment check:"
RUN echo "NEXT_PUBLIC_API_URL_BE: $NEXT_PUBLIC_API_URL_BE"
RUN echo "NEXT_PUBLIC_API_URL_AUTH: $NEXT_PUBLIC_API_URL_AUTH"

# Build the Next.js application
RUN npm run build

# Use a minimal runtime image
FROM node:20-alpine AS runner

WORKDIR /app

# Set production environment
ENV NODE_ENV=production

# Copy only the necessary files from the builder stage
COPY --from=builder /app/.next .next
COPY --from=builder /app/node_modules node_modules
COPY --from=builder /app/package.json package.json
COPY --from=builder /app/public ./public
COPY --from=builder /app/next.config.* ./

# Final debug check
RUN echo "Runtime environment check:"
RUN echo "NEXT_PUBLIC_API_URL_BE: $NEXT_PUBLIC_API_URL_BE"
RUN echo "NEXT_PUBLIC_API_URL_AUTH: $NEXT_PUBLIC_API_URL_AUTH"

# Expose the port Next.js runs on
EXPOSE 3000

# Start the Next.js application
CMD ["npm", "run", "start"]