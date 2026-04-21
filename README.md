# Camera Shop Backend

A full-featured e-commerce backend for a camera equipment marketplace built with Spring Boot.

## Table of Contents
- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Database Setup](#database-setup)
  - [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Authentication](#authentication)
- [Testing](#testing)
- [Deployment](#deployment)
  - [Docker Deployment](#docker-deployment)
  - [Local Deployment](#local-deployment)

## Overview

This is the backend service for a camera equipment marketplace that allows users to browse, rent, and purchase photography equipment. The backend provides RESTful APIs for user authentication, product management, shopping cart, order processing, rental services, and payment integration.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.4**
- **Spring Security + JWT**
- **Spring Data JPA**
- **MySQL**
- **Maven**
- **Docker**

## Features

- 🔐 JWT Authentication & Authorization
- 👤 User Registration & Login (with Email Verification)
- 🛍️ Product Catalog Management
- 🛒 Shopping Cart Functionality
- 📦 Order Processing
- 📱 Rental Services
- 💰 Payment Integration (MoMo, VNPay)
- 🚚 Shipping Integration (GHN)
- ❤️ Favorites/Wishlist
- 📧 Email Verification & Notifications
- 🔔 Notification System
- 📊 Admin Dashboard APIs
- ⭐ Rating & Review System

## Project Structure

```
src/main/java/com/camerashop/
├── CameraShopApplication.java
├── config/
│   ├── DataInitializer.java
│   ├── NotificationScheduler.java
│   ├── OAuth2SuccessHandler.java
│   └── SecurityConfig.java
├── controller/
│   ├── AssetController.java
│   ├── AuthController.java
│   ├── CartController.java
│   ├── CategoryController.java
│   ├── FavoriteController.java
│   ├── NotificationController.java
│   ├── OrderController.java
│   ├── PaymentController.java
│   ├── ProductController.java
│   ├── RentalController.java
│   └── ShippingController.java
├── dto/
├── entity/
│   ├── BaseEntity.java
│   ├── Cart.java
│   ├── Review.java
│   ├── Image.java
│   └── ...
├── exception/
│   └── GlobalExceptionHandler.java
├── filter/
│   └── JwtAuthFilter.java
├── repository/
├── service/
│   ├── JwtService.java
│   └── ...
└── util/
    └── JwtUtil.java
```

## Getting Started

### Prerequisites

- Java 17
- Maven 3.8+
- MySQL 8.0+ (or use Docker)
- Docker & Docker Compose (optional)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/minhphuong150505/Mobile-App_Backend.git
   cd Mobile-App_Backend/Backend
   ```

2. Install dependencies:
   ```bash
   mvn clean compile
   ```

### Database Setup

#### Option 1: Using Docker (Recommended)

```bash
docker-compose up -d
```

MySQL will start on `localhost:3306` with:
- Database: `camera_shop`
- Username: `root`
- Password: `admin123`

#### Option 2: Local MySQL

1. Start MySQL server:
   ```bash
   sudo systemctl start mysql
   ```

2. Create database:
   ```sql
   mysql -u root -p
   CREATE DATABASE camera_shop;
   ```

### Running the Application

#### Local Development

```bash
mvn spring-boot:run
```

Server runs on: http://localhost:8080

#### Using Docker

```bash
# Build and start
docker-compose up -d

# Check logs
docker-compose logs -f

# Stop
docker-compose down
```

## API Endpoints

Base URL: `http://localhost:8080/api`

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/oauth2/google` | Google OAuth2 login |
| GET | `/api/auth/me` | Get current user |
| PUT | `/api/auth/avatar` | Update avatar |
| PUT | `/api/auth/password` | Change password |

### Products
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | Get all products |
| GET | `/api/products/{id}` | Get product by ID |
| POST | `/api/products` | Create new product (ADMIN) |
| PUT | `/api/products/{id}` | Update product (ADMIN) |
| DELETE | `/api/products/{id}` | Delete product (ADMIN) |

### Assets
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/assets` | Get all assets |
| GET | `/api/assets/{id}` | Get asset by ID |
| POST | `/api/assets` | Create new asset (ADMIN) |
| PUT | `/api/assets/{id}` | Update asset (ADMIN) |

### Cart
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/cart` | Get user's cart |
| POST | `/api/cart` | Add item to cart |
| PUT | `/api/cart/{id}` | Update cart item |
| DELETE | `/api/cart/{id}` | Remove item from cart |

### Orders
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/orders` | Get user's orders |
| GET | `/api/orders/{id}` | Get order by ID |
| POST | `/api/orders` | Create new order |
| POST | `/api/orders/{id}/cancel` | Cancel order |

### Rentals
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/rentals` | Get user's rentals |
| GET | `/api/rentals/{id}` | Get rental by ID |
| POST | `/api/rentals` | Create new rental |
| PUT | `/api/rentals/{id}/return` | Return rented item |

### Favorites
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/favorites` | Get user's favorites |
| POST | `/api/favorites` | Add item to favorites |
| DELETE | `/api/favorites/{id}` | Remove item from favorites |

### Notifications
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notifications` | Get user's notifications |
| GET | `/api/notifications/unread` | Get unread notifications |
| POST | `/api/notifications/{id}/read` | Mark as read |
| POST | `/api/notifications/read-all` | Mark all as read |

### Payment
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/payment/momo/create` | Create MoMo payment |
| GET | `/api/payment/momo/callback` | MoMo redirect callback |
| GET | `/api/payment/momo/ipn` | MoMomom IPN (server-to-server) |

### Shipping
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/shipping/locations` | Get shipping locations |
| POST | `/api/shipping/calculate` | Calculate shipping fee |

## Authentication

This application uses JWT (JSON Web Token) for authentication:

1. Register or login to get a JWT token
2. Include the token in the Authorization header:
   ```
   Authorization: Bearer <your-token-here>
   ```

### Token Structure

- **Subject**: userId (UUID)
- **Claims**: username, email, role
- **Expiration**: 24 hours
- **Signature**: HMAC-SHA256

## Testing

Run tests with Maven:

```bash
mvn test
```

## Deployment

### Docker Deployment

```bash
# Build images
docker-compose build

# Start services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Stop with data volume
docker-compose down -v
```

### Local Deployment

1. Update `src/main/resources/application.properties` with production database credentials
2. Build the JAR file:
   ```bash
   mvn clean package
   ```
3. Run the JAR:
   ```bash
   java -jar target/camera-shop-backend-1.0.0.jar
   ```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | MySQL connection string | `jdbc:mysql://localhost:3306/camera_shop` |
| `SPRING_DATASOURCE_USERNAME` | MySQL username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | MySQL password | `admin123` |
| `APP_JWT_SECRET` | JWT signing secret | - |
| `APP_JWT_EXPIRATION_MS` | Token expiration (ms) | `86400000` (24h) |
| `APP_FRONTEND_URL` | Frontend URL for redirects | `http://localhost:8081` |

## Test Users

After first run, the database will be seeded with test users:

| Email | Password | Role |
|-------|----------|------|
| test@example.com | password123 | USER |
| john@example.com | password123 | ADMIN |

## Notes

- JWT tokens expire in 24 hours
- Email verification is required for local registration
- OAuth2 emails are auto-verified
- MoMomom IPN requires public IP (use ngrok for local testing)
- GHN shipping requires shop registration

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.

---

Built with ❤️ using Spring Boot
