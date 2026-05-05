package com.smartbite.operativo.client.contract;

/**
 * CONTRATO DE INTEGRACIÓN (OBLIGATORIO) — Operativo ↔ Administrativo
 *
 * <p>Objetivo: obtener información de producto para generar un snapshot de precio en DetalleOrden
 * (módulo Operativo) sin acoplarse a entidades del módulo Administrativo.</p>
 *
 * <h3>Endpoint</h3>
 * <pre>
 * GET /api/productos/{id}
 * </pre>
 *
 * <h3>Respuesta exitosa (200)</h3>
 * JSON estándar con los campos obligatorios:
 * <ul>
 *   <li>id (Long)</li>
 *   <li>nombre (String)</li>
 *   <li>precio (BigDecimal / number)</li>
 *   <li>activo (Boolean)</li>
 * </ul>
 *
 * <h3>Errores esperados</h3>
 * <ul>
 *   <li>404 Not Found — producto inexistente</li>
 *   <li>5xx — error interno del módulo Administrativo o error de red</li>
 * </ul>
 *
 * <p>Nota: este contrato debe mantenerse estable entre equipos para evitar fallos de integración.</p>
 */
public interface AdministrativoProductoContract {

    String BASE_PATH = "/api/productos";

    String GET_BY_ID = BASE_PATH + "/{id}";
}

