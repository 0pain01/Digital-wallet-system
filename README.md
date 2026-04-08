# 💳 Digital Wallet System

> **A polyglot microservices-based Digital Wallet backend** — Built with Spring Boot (Java) and FastAPI (Python), backed by MongoDB and Redis, and fully containerized with Docker Compose. Handles user authentication, wallet operations, and financial transactions across independent, loosely coupled services.

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Microservices Breakdown](#-microservices-breakdown)
- [Project Structure](#-project-structure)
- [Features](#-features)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
  - [Clone the Repository](#1-clone-the-repository)
  - [Configure Each Service](#2-configure-each-service)
  - [Run with Docker Compose](#3-run-with-docker-compose)
  - [Run Services Individually (without Docker)](#4-run-services-individually-without-docker)
- [Service Ports](#-service-ports)
- [Inter-Service Dependencies](#-inter-service-dependencies)
- [Contributing](#-contributing)

---

## 📋 Overview

The **Digital Wallet System** is a backend platform that simulates a real-world digital payment wallet. It is designed using a **microservices architecture**, where each concern — authentication, wallet management, transaction processing, and analytics — is handled by a dedicated, independently deployable service.

The system is **polyglot**: the core services are written in **Java (Spring Boot)** while a supplementary analytics/utility service is written in **Python (FastAPI)**. All services communicate over a shared Docker bridge network and persist data in **MongoDB**, with **Redis** used for caching and session/token management.

---

## 🏗 Architecture

```
                        ┌──────────────────────────────────────────┐
                        │            wallet_network (Bridge)        │
                        │                                          │
          ┌─────────────┤   ┌──────────────┐  ┌────────────────┐  │
  Client  │  :8080      │   │  Auth Service│  │  MongoDB       │  │
  ───────►│  Auth       ├──►│  (Spring     │  │  :27017        │  │
          │  Service    │   │   Boot/Java) │  │                │  │
          └─────────────┤   └──────┬───────┘  └────────┬───────┘  │
                        │          │ JWT               │           │
          ┌─────────────┤   ┌──────▼───────┐           │           │
  Client  │  :8081      │   │ Wallet Svc   │◄──────────┤           │
  ───────►│  Wallet     ├──►│ (Spring      │           │           │
          │  Service    │   │  Boot/Java)  │           │           │
          └─────────────┤   └──────┬───────┘  ┌────────┴───────┐  │
                        │          │           │  Redis         │  │
          ┌─────────────┤   ┌──────▼───────┐  │  :6379         │  │
  Client  │  :8082      │   │ Transaction  │◄─┘                │  │
  ───────►│  Transaction├──►│ Service      │                    │  │
          │  Service    │   │ (Spring      │                    │  │
          └─────────────┤   │  Boot/Java)  │                    │  │
                        │   └──────────────┘                    │  │
          ┌─────────────┤   ┌──────────────┐                    │  │
  Client  │  :8000      │   │ FastAPI Svc  │                    │  │
  ───────►│  FastAPI    ├──►│ (Python)     │                    │  │
          │  Service    │   └──────────────┘                    │  │
          └─────────────┴──────────────────────────────────────────┘
```

---

## 🛠 Tech Stack

| Category | Technology |
|---|---|
| Language (Core Services) | Java 21+ |
| Language (Analytics/Utility) | Python 3.x |
| Framework (Java) | Spring Boot |
| Framework (Python) | FastAPI |
| Database | MongoDB |
| Cache / Session Store | Redis |
| Containerization | Docker + Docker Compose |
| Build Tool (Java) | Apache Maven |
| Package Manager (Python) | pip / requirements.txt |
| Inter-service Network | Docker Bridge Network (`wallet_network`) |

---

## 🔧 Microservices Breakdown

### 1. `auth` — Authentication Service
- **Language:** Java (Spring Boot)
- **Port:** `8080`
- **Responsibilities:**
  - User registration and login
  - Password hashing and verification
  - JWT token generation and validation
  - Session management via Redis (token blacklisting / refresh tokens)
- **Dependencies:** MongoDB (user store), Redis (token cache)

---

### 2. `walletService` — Wallet Management Service
- **Language:** Java (Spring Boot)
- **Port:** `8081`
- **Responsibilities:**
  - Create and manage user digital wallets
  - Check wallet balances
  - Credit and debit wallet accounts
  - Enforce business rules (e.g., sufficient balance checks)
- **Dependencies:** MongoDB (wallet store), Redis (caching), Auth Service (JWT verification)

---

### 3. `transaction` — Transaction Processing Service
- **Language:** Java (Spring Boot)
- **Port:** `8082`
- **Responsibilities:**
  - Process peer-to-peer fund transfers between wallets
  - Record all transaction history (debit/credit logs)
  - Ensure transactional consistency across wallet operations
  - Expose transaction history and status endpoints
- **Dependencies:** MongoDB (transaction records), Wallet Service

---

### 4. `fastapi-service` — Analytics / Utility Service
- **Language:** Python (FastAPI)
- **Port:** `8000`
- **Responsibilities:**
  - Supplementary read-heavy or analytics-oriented endpoints
  - Lightweight query layer on top of MongoDB data
  - Swagger/OpenAPI docs available out of the box at `/docs`
- **Dependencies:** MongoDB

---

### Infrastructure Services

| Service | Image | Port | Purpose |
|---|---|---|---|
| MongoDB | `mongo:latest` | `27017` | Primary database for all services |
| Redis | `redis:latest` | `6379` | Caching, JWT session store, rate limiting |

---

## 📁 Project Structure

```
Digital-wallet-system/
│
├── auth/                          # Authentication Microservice (Spring Boot / Java)
│   ├── src/
│   │   └── main/java/...          # Controllers, Services, Repositories, Models
│   ├── pom.xml                    # Maven dependencies (Spring Security, JWT, MongoDB, Redis)
│   └── Dockerfile                 # Container build for auth service
│
├── walletService/                 # Wallet Management Microservice (Spring Boot / Java)
│   ├── src/
│   │   └── main/java/...          # Wallet controllers, services, entities
│   ├── pom.xml                    # Maven dependencies
│   └── Dockerfile
│
├── transaction/                   # Transaction Processing Microservice (Spring Boot / Java)
│   ├── src/
│   │   └── main/java/...          # Transaction logic, history, transfer operations
│   ├── pom.xml                    # Maven dependencies
│   └── Dockerfile
│
├── fastapi-service/               # Analytics / Utility Microservice (Python / FastAPI)
│   ├── main.py                    # FastAPI app entry point
│   ├── requirements.txt           # Python dependencies
│   └── Dockerfile
│
├── docker-compose.yml             # Orchestrates all services + MongoDB + Redis
└── .gitignore
```

---

## ✨ Features

**Authentication & Security**
- User registration and secure login via the Auth Service
- JWT-based stateless authentication issued on login
- Redis-backed token storage for session management and revocation
- Each downstream service validates tokens issued by the Auth Service

**Wallet Management**
- Create a personal digital wallet on account creation
- Real-time balance enquiry
- Credit wallet (deposit funds) and debit wallet (withdraw/pay)
- Balance validation before any transaction is processed

**Transaction Processing**
- Peer-to-peer (P2P) fund transfers between user wallets
- Full transaction history with timestamps, amounts, and status
- Transactional integrity — wallet debits and credits are applied atomically
- Transaction status tracking (pending, successful, failed)

**Analytics & API Documentation**
- FastAPI service exposes a lightweight query API over MongoDB
- Auto-generated interactive API docs via Swagger UI at `http://localhost:8000/docs`
- ReDoc alternative docs at `http://localhost:8000/redoc`

**Infrastructure & DevOps**
- Fully containerized — every service, database, and cache runs in Docker
- Single-command startup via `docker-compose up`
- Services are isolated in a dedicated `wallet_network` Docker bridge network
- `restart: always` policy ensures high availability in development and staging

---

## ✅ Prerequisites

| Tool | Purpose |
|---|---|
| Docker (v20+) | Run all services as containers |
| Docker Compose (v2+) | Orchestrate multi-container setup |
| Java JDK 21+ | Only needed if running services without Docker |
| Maven 3.8+ | Only needed if building Java services without Docker |
| Python 3.9+ | Only needed if running FastAPI service without Docker |
| Git | Clone the repository |

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/0pain01/Digital-wallet-system.git
cd Digital-wallet-system
```

---

### 2. Configure Each Service

Each service reads its configuration from its own `application.properties` (Java) or environment variables (Python). Before starting, update connection strings as needed.

**For each Spring Boot service** (`auth`, `walletService`, `transaction`), edit the respective `src/main/resources/application.properties`:

```properties
# MongoDB
spring.data.mongodb.uri=mongodb://mongodb:27017/walletdb

# Redis
spring.redis.host=redis
spring.redis.port=6379

# JWT Secret (auth service)
jwt.secret=YOUR_SECRET_KEY_HERE
jwt.expiration=86400000
```

> **Note:** When running via Docker Compose, `mongodb` and `redis` resolve via Docker's internal DNS — no changes needed for hostnames. Only update secrets and other custom values.

**For the FastAPI service**, create a `.env` file inside `fastapi-service/`:

```env
MONGO_URI=mongodb://mongodb:27017/walletdb
```

---

### 3. Run with Docker Compose

This is the recommended way to run the entire system:

```bash
docker-compose up --build
```

Docker Compose will:
1. Pull `mongo:latest` and `redis:latest` images
2. Build Docker images for all 4 microservices
3. Start all containers connected to the `wallet_network`
4. Enforce startup order (Auth → Wallet → Transaction; FastAPI → MongoDB)

To run in detached (background) mode:

```bash
docker-compose up --build -d
```

To stop all services:

```bash
docker-compose down
```

To stop and remove all volumes (wipe database data):

```bash
docker-compose down -v
```

---

### 4. Run Services Individually (without Docker)

If you prefer to run services directly on your machine:

**Start MongoDB and Redis first** (Docker is the easiest way):
```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
docker run -d -p 6379:6379 --name redis redis:latest
```

**Auth Service (Spring Boot):**
```bash
cd auth
./mvnw spring-boot:run
```

**Wallet Service (Spring Boot):**
```bash
cd walletService
./mvnw spring-boot:run
```

**Transaction Service (Spring Boot):**
```bash
cd transaction
./mvnw spring-boot:run
```

**FastAPI Service (Python):**
```bash
cd fastapi-service
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

---

## 🌐 Service Ports

| Service | Port | Base URL |
|---|---|---|
| Auth Service | `8080` | `http://localhost:8080` |
| Wallet Service | `8081` | `http://localhost:8081` |
| Transaction Service | `8082` | `http://localhost:8082` |
| FastAPI Service | `8000` | `http://localhost:8000` |
| FastAPI Swagger Docs | `8000` | `http://localhost:8000/docs` |
| MongoDB | `27017` | `mongodb://localhost:27017` |
| Redis | `6379` | `redis://localhost:6379` |

---

## 🔗 Inter-Service Dependencies

The startup order enforced by Docker Compose reflects real runtime dependencies:

```
MongoDB ──────────────────────────────────────────► (all services)
Redis ────────────────────────────────────────────► auth, walletService
auth ─────────────────────────────────────────────► walletService
walletService ────────────────────────────────────► transaction
```

| Service | Depends On |
|---|---|
| `auth-service` | MongoDB, Redis |
| `wallet-service` | MongoDB, Redis, auth-service |
| `transaction-service` | MongoDB, wallet-service |
| `fastapi-service` | MongoDB |

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m 'Add some feature'`
4. Push to the branch: `git push origin feature/your-feature-name`
5. Open a Pull Request

---

<p align="center">Built with ☕ Java · 🐍 Python · 🍃 MongoDB · ⚡ Redis · 🐳 Docker</p>
