# SmartBite — Módulo Operativo (Contrato de API para Frontend)

> Estado actual: **los Controllers aún no están implementados**.
>
> Este documento define una **propuesta de endpoints** basada estrictamente en los *Services* y *DTOs* existentes en `com.smartbite.operativo`.
> La intención es que, al crear Controllers en Fase 4, se adopten estas rutas para mantener un contrato claro con frontend.

---

## Convenciones

- Base sugerida: `/api/operativo`
- Formato de error (según `GlobalExceptionHandler`):
  - `404` → `ResourceNotFoundException`
  - `400` → `InvalidStateException` / `IllegalArgumentException`
  - `422` → `BusinessException`
  - `500` → error no controlado

---

## 1) Crear Orden

**URL (propuesta)**: `POST /api/operativo/ordenes`

**Service**: `OrdenService.crearOrden(CrearOrdenRequestDTO)`

**Request (DTO)**: `CrearOrdenRequestDTO`
```json
{
  "mesaId": 10,
  "usuarioId": 5,
  "sucursalId": 2,
  "productos": [
    { "productoId": 1, "cantidad": 2 }
  ]
}
```

**Response (DTO)**: `OrdenResponseDTO`

**Errores típicos**:
- `404` Mesa no existe
- `400` Request inválido (@Valid)

---

## 2) Agregar producto a Orden (crea DetalleOrden con snapshot de precio)

**URL (propuesta)**: `POST /api/operativo/ordenes/{ordenId}/detalles`

**Service**: `OrdenService.agregarProducto(Long ordenId, AgregarProductoRequestDTO)`

**Request (DTO)**: `AgregarProductoRequestDTO`
```json
{ "productoId": 1, "cantidad": 2 }
```

**Response (DTO)**: `DetalleOrdenResponseDTO`

**Errores típicos**:
- `404` Orden no existe
- `404` Producto no existe (vía Administrativo)
- `400` Orden en estado inválido (CANCELADA/PAGADA)
- `422` Error externo consultando Administrativo

---

## 3) Registrar pago

**URL (propuesta)**: `POST /api/operativo/pagos`

**Service**: `PagoService.registrarPago(CrearPagoRequestDTO)`

**Request (DTO)**: `CrearPagoRequestDTO`
```json
{
  "ordenId": 100,
  "metodoPagoId": 1,
  "monto": 50000,
  "referenciaTransaccion": "TRX-123"
}
```

**Response (DTO)**: `PagoResponseDTO`

**Errores típicos**:
- `404` Orden no existe
- `400` monto inválido o supera el total

---

## 4) Generar venta (y factura automáticamente si `clienteId` viene informado)

**URL (propuesta)**: `POST /api/operativo/ventas`

**Service**: `VentaService.crearVentaDesdeOrden(Long ordenId, Long clienteId)`

**Request (propuesta mínima)**
```json
{ "ordenId": 100, "clienteId": 200 }
```

**Response (DTO)**: `VentaResponseDTO`

**Notas**:
- Venta solo se genera si la orden está **totalmente pagada**.
- La factura se genera automáticamente en el mismo flujo (idempotente).

**Errores típicos**:
- `404` Orden no existe
- `400` Orden no pagada completamente

---

## 5) Obtener factura

**URL (propuesta)**: `GET /api/operativo/facturas/{facturaId}`

**Service**: `FacturaService.obtenerPorId(Long facturaId)`

**Response (DTO)**: `FacturaResponseDTO`

**Errores típicos**:
- `404` Factura no existe

---

# Flujo End-to-End (simulado) para Postman / QA

1. **Crear orden**
   - `POST /api/operativo/ordenes`
   - Genera Orden + Detalles con precio snapshot.

2. **Agregar producto**
   - `POST /api/operativo/ordenes/{ordenId}/detalles`
   - Llama a Administrativo vía Feign (`GET /api/productos/{id}`) para obtener precio real.

3. **Registrar pagos**
   - `POST /api/operativo/pagos`
   - Se valida coherencia de pagos; al completarse el total, la orden queda pagada.

4. **Generar venta**
   - `POST /api/operativo/ventas`
   - Valida orden totalmente pagada (fuente única de verdad: `PagoService`).

5. **Generar factura (automática)**
   - Interno: `VentaService` invoca `FacturaService`.
   - Se genera número de factura y se asocia a Cliente y Venta.

---

## Servicios involucrados y validaciones clave

- `OrdenService`
  - Valida estado de Orden al agregar productos.
  - Calcula total por suma de subtotales.

- `ProductoClient` (Feign)
  - Contrato: `GET /api/productos/{id}`
  - Errores Feign se traducen a excepciones del dominio Operativo.

- `PagoService`
  - Valida suma de pagos vs total orden.
  - Fuente única de verdad de “orden totalmente pagada”.

- `VentaService`
  - Solo crea venta si `PagoService.estaOrdenTotalmentePagada(ordenId)`.
  - Crea factura automáticamente (idempotente).

- `FacturaService`
  - Requiere clienteId.
  - Total = subtotal + impuestos (impuestos default 0).

