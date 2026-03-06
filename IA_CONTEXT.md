# SmartBite — AI Development Context

This file provides persistent context for AI agents (Copilot, Claude, etc.) working on the SmartBite backend project.
It ensures that AI assistance remains consistent even if chat sessions reset.

---

# 1. Project Overview

SmartBite is a backend system for restaurant management.

The project is built using:

* Java 17
* Spring Boot 3.2.5
* Maven
* PostgreSQL
* JPA / Hibernate
* Lombok

The project follows a **modular monolith architecture**, NOT microservices.

Modules are separated by domain responsibility but run within the same Spring Boot application.

---

# 2. System Modules

The system is divided into two logical modules:

## Administrativo Module

Responsible for:

* Usuarios
* Roles
* Productos
* Categorías
* Sucursales
* Inventario
* Configuración

This module owns entities such as:

* Usuario
* Producto
* Categoria
* Sucursal

---

## Operativo Module

Responsible for the operational flow inside the restaurant.

This includes:

* Tables
* Orders
* Order details
* Payments
* QR codes
* Payment methods

Entities in this module:

* Mesa
* Orden
* DetalleOrden
* Pago
* MetodoPago
* CodigoQR

---

# 3. Architecture Rules

IMPORTANT: The project **must remain a modular monolith**.

DO NOT convert the system into microservices.

Architecture style:

Layered Architecture

model
repository
service
controller

Each module contains its own layers.

Example:

com.smartbite.operativo

model
repository
service
controller
dto
mapper

---

# 4. Cross-Module References

Modules must remain loosely coupled.

Entities from different modules **must NOT use JPA relationships between them**.

Instead, they reference external entities using simple foreign key fields.

Example:

Operativo referencing Administrativo:

```
Long sucursalId
Long usuarioId
Long productoId
```

Never use:

```
@ManyToOne Producto
@ManyToOne Usuario
```

This rule is critical to maintain modular separation.

---

# 5. Current Development Status

Current branch:

```
feature/operativo-domain-model
```

The **Operativo domain model has been implemented.**

Entities completed:

* Mesa
* Orden
* DetalleOrden
* Pago
* MetodoPago
* CodigoQR

All entities use:

* JPA annotations
* Lombok
* GenerationType.IDENTITY
* FetchType.LAZY
* EnumType.STRING

Money values use:

```
BigDecimal
```

---

# 6. Entity Relationships

Mesa
← Orden (ManyToOne)

Orden
← DetalleOrden (OneToMany)
← Pago (OneToMany)

Pago
→ MetodoPago (ManyToOne)

CodigoQR
→ Mesa (OneToOne)

External references (Administrativo):

Orden

* sucursalId
* usuarioId

DetalleOrden

* productoId

CodigoQR

* productoId

Mesa

* sucursalId

---

# 7. Enum Definitions

EstadoMesa

* DISPONIBLE
* OCUPADA
* RESERVADA
* INACTIVA

EstadoOrden

* PENDIENTE
* EN_PREPARACION
* LISTA
* ENTREGADA
* PAGADA
* CANCELADA

EstadoPago

* PENDIENTE
* APROBADO
* RECHAZADO
* REEMBOLSADO

TipoQR

* MESA
* PRODUCTO
* PAGO

---

# 8. JPA Mapping Rules

Primary keys:

```
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

Enums:

```
@Enumerated(EnumType.STRING)
```

Relationships:

```
@ManyToOne(fetch = FetchType.LAZY)
@OneToMany(fetch = FetchType.LAZY)
@OneToOne(fetch = FetchType.LAZY)
```

Cascade usage:

CascadeType.ALL + orphanRemoval ONLY for:

Orden → DetalleOrden
Orden → Pago

---

# 9. Development Workflow

Typical implementation order:

1️⃣ model
2️⃣ repository
3️⃣ dto
4️⃣ mapper
5️⃣ service
6️⃣ controller

---

# 10. Coding Standards

* Use Lombok for entities
* Use BigDecimal for monetary values
* Prefer explicit @Table names
* Prefer snake_case column names
* Use LAZY loading by default

---

# 11. AI Agent Instructions

When generating code:

* Follow the modular monolith architecture
* Respect cross-module boundaries
* Do not introduce microservices
* Do not add unnecessary complexity
* Keep entities simple and focused on persistence
* Business logic belongs in services

Always prioritize **clarity, maintainability, and consistency with existing code**.

---
