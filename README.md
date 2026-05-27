# Stock Management System

A comprehensive stock management system built with Spring Boot, featuring inventory and supplier management, user
authentication, and role-based access control.

## Features

- **Inventory Management**: Track items, stock levels, prices, and categories
- **Supplier Management**: Manage suppliers with contact details, ratings, and specializations
- **User Management**: Create, edit, and manage users with role-based permissions
- **Role-Based Access Control**: Different access levels for administrators and stock managers
- **Security**: Authentication and authorization using Spring Security
- **Responsive UI**: Modern interface built with Bootstrap and Thymeleaf templates

## User Roles

### Administrator (ADMIN)

- Full access to all features
- Can manage users
- Can access admin dashboard, reports, logs, and analytics
- Can manage inventory and suppliers

### Stock Manager (STOCK_MANAGER)

- Can manage inventory items (add, edit, delete)
- Can manage suppliers (add, edit, delete)
- Cannot access administrative features

## Navigation

### For Administrators

- **Admin Dashboard**: Main administrative interface with system statistics
- **Inventory Management**: Manage stock items
- **Supplier Management**: Manage product suppliers
- **User Management**: Manage system users
- **Reports, Logs, Analytics**: Access system data and performance metrics

### For Stock Managers

- **Inventory Dashboard**: Overview of current stock levels
- **Item List**: Detailed list of all inventory items
- **Supplier Management**: Access to supplier information and management

The navigation bar changes based on your role, showing only the features you have permission to access.

## Technologies Used

- **Backend**: Java with Spring Boot, Spring MVC, Spring Security, Spring Data JPA
- **Frontend**: HTML, CSS, JavaScript, Bootstrap 5, Thymeleaf
- **Database**: Relational database (configured for MySQL)
- **Build Tool**: Maven

## Getting Started

1. Clone the repository
2. Configure the database connection in `application.properties`
3. Run the application using Maven: `./mvnw spring-boot:run`
4. Access the application at `http://localhost:8080`
5. Log in using the default admin credentials (username: admin, password: admin)