# 🚗 GarageBook 🛠️

[![Java Version](https://img.shields.io/badge/Java-17-orange.svg?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot Version](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/MySQL-8.0-blue.svg?style=flat-square&logo=mysql)](https://www.mysql.com/)
[![Security](https://img.shields.io/badge/Security-Spring%20Security%20%2B%20JWT-red.svg?style=flat-square&logo=springsecurity)](https://spring.io/projects/spring-security)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

**GarageBook** is a modern, production-ready Multi-Tenant Garage & Service Booking Management platform. Built using **Spring Boot**, **Spring Security JWT**, and **MySQL**, it empowers garage owners to manage their workshops, mechanic rosters, customer directories, vehicles, and parts inventory seamlessly. 

The application is integrated with **Meta's WhatsApp Cloud API** to deliver real-time service updates to vehicle owners and includes a **Thymeleaf-powered HTML invoicing system** for professional billing.

---

## 🌟 Key Features

*   **🔒 Secure Authentication & Authorization**: Custom JWT authentication filter (`JwtAuthenticationFilter`) guarding resource endpoints with token expiration and security contexts.
*   **🏢 Multi-Tenant Data Boundaries (RBAC)**: Multi-tenancy is enforced at the service level. A registered user corresponds to a single `Garage`. All mechanics, bookings, and sales are automatically isolated and scoped to the user's specific garage.
*   **🔧 Mechanics Roster Management**: Maintain staff profiles, contact numbers, and identification records (Aadhaar cards) mapped directly to your garage entity.
*   **🚗 Customer & Vehicle Directory**: Register owners with their contact details and associate multiple vehicle profiles (CAR, BIKE, TRUCK, SUV, SEDAN) under their profiles.
*   **📅 Booking Engine & Workflow**: Track service logs through stages: `CREATED` ➔ `IN_SERVICE` ➔ `COMPLETED` ➔ `CANCELLED`. Supports services like `GENERAL_SERVICE`, `WASH`, `REPAIR`, and `OIL_CHANGE`.
*   **📦 Interactive Parts Inventory Allocation**: Add replacement parts to booking jobs. The system automatically reduces available stock quantities, computes line item prices (`quantity * pricePerUnit`), and updates the booking's `totalAmount` in real-time.
*   **💬 WhatsApp Cloud API Integration**: Automated triggers that send message templates (via Meta API) to vehicle owners when a booking is created or when its status shifts (e.g., service completion alerts).
*   **📄 Dynamic PDF-Friendly HTML Invoicing**: Auto-generates clean, print-friendly invoices with structured metadata, list of allocated parts, tax descriptors, and billing summaries.

---

## 🛠️ Technology Stack

| Component | Technology | Version | Description |
| :--- | :--- | :--- | :--- |
| **Core Framework** | Spring Boot | 4.0.6 | Core application framework |
| **Language** | Java | 17 | JDK 17 |
| **Database** | MySQL | 8.x | Relational DB for schemas and persistence |
| **Security** | Spring Security | 4.0.6 | Authentication & Endpoint authorization |
| **Auth Tokens** | JJWT (Java JWT) | 0.11.5 | Token creation, signing, and verification |
| **Templating Engine**| Thymeleaf | 4.0.6 | Dynamic invoice template rendering |
| **Boilerplate** | Project Lombok | 1.18.x | Auto-generates getters, setters, constructors, builders |
| **Build Tool** | Apache Maven | 3.x | Build and dependency management |

---

## 📁 Directory Structure

```text
GarageBook/
├── .env                          # Local environment secret variables
├── pom.xml                       # Maven dependencies and plugins configuration
└── src/
    ├── main/
    │   ├── java/GarageBook/GarageBook/
    │   │   ├── Authentication/   # Sign-up & Sign-in logic (controllers & services)
    │   │   ├── Controller/       # REST Controllers mapping business resources
    │   │   ├── Dto/              # Request and Response Data Transfer Objects
    │   │   │   ├── Request/      # Deserialization models (Payload Validation)
    │   │   │   └── Response/     # Serialization models (Custom output structures)
    │   │   ├── Enums/            # System-wide enum states (BookingStatus, ServiceType, VehicleType)
    │   │   ├── Models/           # Hibernate JPA database entities
    │   │   ├── Repository/       # Spring Data JPA repositories (Database interfaces)
    │   │   ├── Service/          # Core transactional business services
    │   │   ├── WebSecurity/      # Spring Security configurations & JWT Filters
    │   │   └── WhatsApp/         # RestTemplate-based Meta Graph API integration
    │   └── resources/
    │       ├── templates/        # Thymeleaf invoice templates (invoice.html)
    │       ├── static/           # Static assets (images, CSS styles)
    │       └── application.properties # Main application properties config
```

---

## 💾 Entity-Relationship Overview

The relational structure enforces clean database boundaries:
*   **`User` 1 ── 1 `Garage`**: Users sign up and register their garage.
*   **`Garage` 1 ── 🔗 `Mechanic`**: Mechanics belong to a single garage.
*   **`Owner` 1 ── 🔗 `Vehicle`**: An owner can register multiple vehicles.
*   **`Vehicle` 1 ── 🔗 `ServiceBooking`**: Bookings track services for a vehicle.
*   **`ServiceBooking` 1 ── 🔗 `ServicePart` 🔗 ── 1 `Part`**: Maps which catalog parts and what quantity are consumed during a specific booking service.

---

## 🚀 Local Development Setup

### 📋 Prerequisites
*   **Java Development Kit (JDK) 17** installed.
*   **MySQL Server** running locally (default port `3306`).
*   **Maven** installed (or utilize the provided Maven Wrapper `./mvnw`).

### 🔧 1. Configure the Environment Files
Create a `.env` file in the root directory of the project (adjacent to `pom.xml`) containing your credentials:

```properties
SECRET_KEY=YOUR_JWT_HMAC_SHA_256_SECRET_KEY_AT_LEAST_256_BITS_LONG
WHATSAPP_API_GRAPH_API_VERSION=https://graph.facebook.com/v25.0
WHATSAPP_API_PHONE_NUMBER_ID=YOUR_META_PHONE_NUMBER_ID
WHATSAPP_API_ACCESS_TOKEN=YOUR_META_PERMANENT_ACCESS_TOKEN
```

### 💾 2. Setup the Database
Create a MySQL database schema named `garagebook`:
```sql
CREATE DATABASE garagebook;
```
Configure your database username and password inside the `src/main/resources/application.properties` if they differ from the defaults:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/garagebook
spring.datasource.username=username
spring.datasource.password=password
```

### 🏃 3. Run the Application
Start the Spring Boot application using the Maven wrapper:

**On Windows (PowerShell/CMD):**
```bash
./mvnw.cmd spring-boot:run
```

**On Linux/macOS:**
```bash
chmod +x mvnw
./mvnw spring-boot:run
```
The application will boot up on `http://localhost:8080`.

---

## 🛡️ Authentication Protocol

All API requests except registration and login endpoints (under `/auth/**`) must carry a valid **Bearer JWT Token** in their request headers:

```http
Authorization: Bearer <your-jwt-token>
```

---

## 📡 API Reference

Below is a detailed specification of the REST endpoints available.

<details>
<summary>🔑 Authentication Endpoints (<code>/auth</code>)</summary>

### 1. Signup / Register
*   **URL**: `POST /auth/signup`
*   **Authentication**: None
*   **Payload**:
    ```json
    {
      "username": "supergarage",
      "password": "securepassword123"
    }
    ```
*   **Response**:
    ```json
    {
      "id": 1,
      "username": "supergarage",
      "createdAt": "2026-06-09T16:00:00.000",
      "updatedAt": "2026-06-09T16:00:00.000",
      "garage": null
    }
    ```

### 2. Login / Authenticate
*   **URL**: `POST /auth/login`
*   **Authentication**: None
*   **Payload**:
    ```json
    {
      "username": "supergarage",
      "password": "securepassword123"
    }
    ```
*   **Response**:
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiJ9.ey...",
      "expiresIn": 2592000000
    }
    ```
</details>

<details>
<summary>👤 User Profile Endpoints (<code>/users</code>)</summary>

### 1. Get Logged-in User Info
*   **URL**: `GET /users/me`
*   **Response**:
    ```json
    {
      "id": 1,
      "username": "supergarage",
      "createdAt": "2026-06-09T16:00:00.000",
      "updatedAt": "2026-06-09T16:00:00.000",
      "garage": {
        "garageId": 1,
        "name": "Super Garage Labs"
      }
    }
    ```

### 2. List All Users
*   **URL**: `GET /users/`
*   **Response**: Array of registered users.
</details>

<details>
<summary>🏢 Garages API (<code>/api/garages</code>) — Tenant Resource</summary>

*User context only permits operating on the Garage linked to the authenticated profile.*

### 1. Register a Garage
*   **URL**: `POST /api/garages`
*   **Payload**:
    ```json
    {
      "name": "Super Garage Labs",
      "address": "123 Main St, New York, NY",
      "phoneNumber": "+15550199",
      "email": "contact@supergaragelabs.com",
      "gstnumber": "29AAAAA1111A1Z1"
    }
    ```
*   **Response**:
    ```json
    {
      "garageId": 1,
      "name": "Super Garage Labs",
      "address": "123 Main St, New York, NY",
      "phoneNumber": "+15550199",
      "email": "contact@supergaragelabs.com",
      "gstnumber": "29AAAAA1111A1Z1"
    }
    ```

### 2. Get Garage Details
*   **URL**: `GET /api/garages` or `GET /api/garages/{id}`
*   **Response**: Returns the garage details corresponding to the user.

### 3. Update Garage Details
*   **URL**: `PUT /api/garages/{id}`
*   **Payload**: `CreateGarageRequestDto` (similar fields as POST)
</details>

<details>
<summary>🔧 Mechanics API (<code>/api/mechanics</code>)</summary>

*Mechanics are automatically assigned to the logged-in user's garage.*

### 1. Register a Mechanic
*   **URL**: `POST /api/mechanics`
*   **Payload**:
    ```json
    {
      "name": "John Doe",
      "phoneNumber": "+15550245",
      "adhaarNumber": "1234-5678-9012",
      "address": "Brooklyn, NY"
    }
    ```
*   **Response**:
    ```json
    {
      "mechanicId": 5,
      "name": "John Doe",
      "phoneNumber": "+15550245",
      "adhaarNumber": "1234-5678-9012",
      "address": "Brooklyn, NY",
      "garageId": 1,
      "garageName": "Super Garage Labs"
    }
    ```

### 2. List Mechanics
*   **URL**: `GET /api/mechanics`
*   **Response**: List of all mechanics registered under the user's garage.
</details>

<details>
<summary>🚗 Vehicle Owners API (<code>/api/owners</code>)</summary>

### 1. Register Owner
*   **URL**: `POST /api/owners`
*   **Payload**:
    ```json
    {
      "name": "Alice Peterson",
      "email": "alice@peterson.com",
      "phoneNumber": "+15550812"
    }
    ```
*   **Response**:
    ```json
    {
      "id": 1,
      "name": "Alice Peterson",
      "email": "alice@peterson.com",
      "phoneNumber": "+15550812"
    }
    ```

### 2. List Owners
*   **URL**: `GET /api/owners`
*   **Response**: Returns lists of customer owner profiles.
</details>

<details>
<summary>🚘 Vehicles API (<code>/vehicles</code>)</summary>

### 1. Register a Vehicle
*   **URL**: `POST /vehicles`
*   **Payload**:
    ```json
    {
      "ownerId": 1,
      "vehicleType": "SEDAN",
      "vehicleNumber": "NY-772A"
    }
    ```
    *(Supported `vehicleType` values: `CAR`, `BIKE`, `TRUCK`, `SUV`, `SEDAN`)*
*   **Response**:
    ```json
    {
      "id": 2,
      "ownerId": 1,
      "ownerName": "Alice Peterson",
      "vehicleType": "SEDAN",
      "vehicleNumber": "NY-772A"
    }
    ```

### 2. List Vehicles
*   **URL**: `GET /vehicles`
</details>

<details>
<summary>📦 Parts Catalog API (<code>/api/parts</code>)</summary>

### 1. Create Catalog Part
*   **URL**: `POST /api/parts`
*   **Payload**:
    ```json
    {
      "partName": "Oil Filter",
      "partSize": "Medium",
      "partNumber": "OF-9921",
      "stockQuantity": 150,
      "defaultPrice": 450
    }
    ```
*   **Response**: Same structure including `partId`.
</details>

<details>
<summary>📅 Service Bookings API (<code>/service-bookings</code>)</summary>

### 1. Create a Booking
*   **URL**: `POST /service-bookings`
*   **Payload**:
    ```json
    {
      "vehicleId": 2,
      "serviceType": "OIL_CHANGE",
      "bookingTime": "2026-06-15T10:00:00",
      "bookingStatus": "CREATED",
      "totalAmount": 0
    }
    ```
    *(Supported `serviceType`: `GENERAL_SERVICE`, `WASH`, `REPAIR`, `OIL_CHANGE`)*  
    *(Supported `bookingStatus`: `CREATED`, `IN_SERVICE`, `COMPLETED`, `CANCELLED`)*
*   **Response**:
    ```json
    {
      "id": 10,
      "vehicleId": 2,
      "vehicleNumber": "NY-772A",
      "serviceType": "OIL_CHANGE",
      "bookingTime": "2026-06-15T10:00:00",
      "bookingStatus": "CREATED",
      "totalAmount": 0,
      "garageId": 1,
      "garageName": "Super Garage Labs",
      "serviceParts": []
    }
    ```
    *(Note: Triggering booking creation automatically fires a WhatsApp notification to the owner)*

### 2. List & Filter Bookings
*   **URL**: `GET /service-bookings`
*   **Response**: Scoped lists matching the tenant's garage context.

### 3. Update Service Booking
*   **URL**: `PUT /service-bookings/{id}`
*   **Payload**: `UpdateServiceBookingRequestDto`
    *(Modifying the booking status to `COMPLETED` sends a final billing status alert via WhatsApp)*
</details>

<details>
<summary>🛠️ Service Parts API (<code>/service-parts</code>)</summary>

*Enables assigning specific parts from inventory to active repair bookings.*

### 1. Add Part to Booking
*   **URL**: `POST /service-parts`
*   **Payload**:
    ```json
    {
      "partId": 3,
      "serviceBookingId": 10,
      "quantity": 2,
      "pricePerUnit": 450
    }
    ```
*   **Response**:
    ```json
    {
      "id": 15,
      "partId": 3,
      "partName": "Oil Filter",
      "serviceBookingId": 10,
      "quantity": 2,
      "pricePerUnit": 450,
      "totalPrice": 900
    }
    ```
    *(This operation automatically reduces part inventory stock quantities and adds the line total to the booking total)*
</details>
<br>

---

## 💬 WhatsApp Meta Cloud API Integration

The application integrates with Meta's Graph API to send structured templates directly to clients. The message configurations are routed through `WhatsAppNotificationService`.

### Prerequisites for Production Notifications
1.  Register a **Meta Developer Account** and create a WhatsApp-enabled application.
2.  Set up your sandbox/live test phone number and extract the **Phone Number ID**.
3.  Generate an **Access Token** (Permanent token recommended for deployment).
4.  Create and submit templates inside the Meta WhatsApp Business Manager corresponding to notifications for **Booking Creation** and **Booking Status Updates**.

---

## 📄 Invoicing System

Invoices are served as print-ready HTML layouts dynamically populated via **Thymeleaf**. 

*   **URL**: `GET /service-bookings/invoice/{bookingId}`
*   **Method Handler**: Returns `ModelAndView` mapping data parameters (Customer Info, Vehicle details, Garage details, GST Number, Part descriptions, Quantity details, and Total Amount) into `invoice.html`.

---

