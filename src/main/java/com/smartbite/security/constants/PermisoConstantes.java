package com.smartbite.security.constants;

public final class PermisoConstantes {
    private PermisoConstantes() {}

    // ===== USUARIOS =====
    public static final String VER_USUARIOS = "VER_USUARIOS";
    public static final String CREAR_USUARIO = "CREAR_USUARIO";
    public static final String ACTUALIZAR_USUARIO = "ACTUALIZAR_USUARIO";
    public static final String ELIMINAR_USUARIO = "ELIMINAR_USUARIO";
    public static final String ASIGNAR_ROL = "ASIGNAR_ROL";
    public static final String VER_USUARIOS_SUCURSAL = "VER_USUARIOS_SUCURSAL";

    // ===== ROLES =====
    public static final String VER_ROLES = "VER_ROLES";
    public static final String CREAR_ROL = "CREAR_ROL";
    public static final String ACTUALIZAR_ROL = "ACTUALIZAR_ROL";
    public static final String ELIMINAR_ROL = "ELIMINAR_ROL";
    public static final String ASIGNAR_PERMISO = "ASIGNAR_PERMISO";

    // ===== PRODUCTOS =====
    public static final String VER_PRODUCTOS = "VER_PRODUCTOS";
    public static final String VER_PRODUCTOS_DISPONIBLES = "VER_PRODUCTOS_DISPONIBLES";  // ← AGREGADA
    public static final String CREAR_PRODUCTO = "CREAR_PRODUCTO";
    public static final String ACTUALIZAR_PRODUCTO = "ACTUALIZAR_PRODUCTO";
    public static final String ELIMINAR_PRODUCTO = "ELIMINAR_PRODUCTO";
    public static final String CAMBIAR_DISPONIBILIDAD = "CAMBIAR_DISPONIBILIDAD";

    // ===== CATEGORÍAS =====
    public static final String VER_CATEGORIAS = "VER_CATEGORIAS";
    public static final String CREAR_CATEGORIA = "CREAR_CATEGORIA";
    public static final String ACTUALIZAR_CATEGORIA = "ACTUALIZAR_CATEGORIA";
    public static final String ELIMINAR_CATEGORIA = "ELIMINAR_CATEGORIA";

    // ===== RECETAS =====
    public static final String VER_RECETAS = "VER_RECETAS";
    public static final String CREAR_RECETA = "CREAR_RECETA";
    public static final String ACTUALIZAR_RECETA = "ACTUALIZAR_RECETA";
    public static final String ELIMINAR_RECETA = "ELIMINAR_RECETA";

    // ===== INVENTARIO =====
    public static final String VER_INVENTARIO = "VER_INVENTARIO";
    public static final String VER_STOCK = "VER_STOCK";
    public static final String ACTUALIZAR_STOCK = "ACTUALIZAR_STOCK";
    public static final String VER_MOVIMIENTOS = "VER_MOVIMIENTOS";

    // ===== COMPRAS =====
    public static final String VER_COMPRAS = "VER_COMPRAS";
    public static final String CREAR_COMPRA = "CREAR_COMPRA";
    public static final String RECIBIR_COMPRA = "RECIBIR_COMPRA";
    public static final String ANULAR_COMPRA = "ANULAR_COMPRA";

    // ===== PROVEEDORES =====
    public static final String VER_PROVEEDORES = "VER_PROVEEDORES";
    public static final String CREAR_PROVEEDOR = "CREAR_PROVEEDOR";
    public static final String ACTUALIZAR_PROVEEDOR = "ACTUALIZAR_PROVEEDOR";
    public static final String ELIMINAR_PROVEEDOR = "ELIMINAR_PROVEEDOR";

    // ===== ÓRDENES =====
    public static final String CREAR_ORDEN = "CREAR_ORDEN";
    public static final String VER_ORDEN = "VER_ORDEN";
    public static final String VER_ORDENES_ACTIVAS = "VER_ORDENES_ACTIVAS";
    public static final String CANCELAR_ORDEN = "CANCELAR_ORDEN";

    // ===== MESAS =====
    public static final String VER_MESAS = "VER_MESAS";
    public static final String CAMBIAR_ESTADO_MESA = "CAMBIAR_ESTADO_MESA";

    // ===== PAGOS =====
    public static final String REGISTRAR_PAGO = "REGISTRAR_PAGO";
    public static final String VER_PAGOS = "VER_PAGOS";

    // ===== CLIENTES =====
    public static final String VER_CLIENTES = "VER_CLIENTES";
    public static final String CREAR_CLIENTE = "CREAR_CLIENTE";
    public static final String ACTUALIZAR_CLIENTE = "ACTUALIZAR_CLIENTE";

    // ===== REPORTES =====
    public static final String VER_REPORTES = "VER_REPORTES";
    public static final String GENERAR_REPORTE = "GENERAR_REPORTE";
    public static final String VER_VENTAS = "VER_VENTAS";
}