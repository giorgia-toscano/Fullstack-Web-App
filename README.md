# Project Economic Monitoring System

A full-stack web application designed to support users in monitoring project costs, revenues, and profit margins in real time, providing key business indicators to improve decision-making and daily workflow management.

Developed as a Bachelor's Thesis at the University of Turin in collaboration with a corporate partner.

Built and delivered in three months, the project focused on an MVP strategy to validate business value, usability, and future scalability.

---

## Overview

The platform helps companies track the economic performance of ongoing projects through dashboards, analytics, and real-time updates.

### Main Objectives

* Monitor project profitability
* Track labor costs and revenues
* Detect critical projects early
* Support management decision-making
* Improve resource allocation efficiency

---

## Tech Stack

### Backend

* Java 25
* Spring Boot 4
* Spring Security
* JWT Authentication
* Spring Data JPA
* Hibernate
* WebSocket + STOMP
* Gradle
* Lombok

### Frontend

* Angular 21
* TypeScript
* Angular Material
* Bootstrap 5
* RxJS
* Internationalization (i18n)

### Database

* MySQL

### Tools

* Git
* IntelliJ IDEA
* VS Code
* Postman
* Mailtrap

---

## Key Features

### Authentication & Security

* Secure JWT-based authentication
* Role-Based Access Control (Admin / Manager)
* Password recovery flow with OTP via email
* Protected API endpoints

### Dashboard & Monitoring

* Live updates via WebSocket communication
* Economic KPI dashboard
* Revenue / Cost / Margin monitoring
* Critical project detection
* Live notifications without page refresh

### Management Features

* Project creation and editing
* User management
* Resource assignment
* Department/project visibility by role

---

## Architecture & Design Patterns

The application was structured using common enterprise software principles.

### Backend Architecture

* Controller Layer → REST API endpoints
* Service Layer → Business logic isolation
* Repository Layer → Database access abstraction
* DTO Pattern → Separation between API and persistence models
* Global Exception Handling → Consistent error responses
* Backend pagination for project endpoints to efficiently handle large datasets and optimize API performance

### Frontend Architecture

* Modular Angular structure
* Service-based API communication
* Reactive state handling with RxJS
* Route Guards for protected pages
* Responsive UI across devices
* Internationalization (i18n) with translation keys

### Real-Time Communication

WebSocket + STOMP used for live system updates:

* Project creation
* Status changes
* Dashboard refresh events
* Administrative notifications

---

## Main Challenges Solved

* Designing a secure authentication system
* Managing role permissions across frontend and backend
* Implementing real-time system updates
* Structuring maintainable service layers
* Handling relational database design
* Integrating frontend and backend cleanly

---

## Future Improvements

* Completion of additional core business modules
* Dockerized deployment
* CI/CD pipeline
* Unit and integration testing
* Advanced chart analytics
* Cloud deployment with AWS / Azure / GCP
* Audit logging system
* Extension of internationalization support with additional languages
* Performance optimization and scalability improvements

---

## Final Thoughts

This project represents a complete full-stack engineering experience combining backend architecture, frontend development, security, database design, and real-time distributed communication.

It demonstrates the ability to rapidly learn technologies, design scalable systems, and deliver business-oriented software within a tight development timeframe using modern engineering practices.

Following the initial delivery, the project continued to evolve with the introduction of automated frontend and backend testing.
