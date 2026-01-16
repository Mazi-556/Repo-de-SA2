-- Generated SQL Data Population Script (Corrected for logical flow)

-- Clean existing data in order (reverse of insertion)
DELETE FROM movimiento;
DELETE FROM factura;
DELETE FROM asiento;
DELETE FROM remito_item;
DELETE FROM remito;
DELETE FROM orden_compra_item;
DELETE FROM orden_compra;
DELETE FROM pedido_cotizacion_item;
DELETE FROM pedido_cotizacion;
DELETE FROM solicitud_compra_item;
DELETE FROM solicitud_compra;
DELETE FROM producto_proveedor;
DELETE FROM producto;
DELETE FROM categoria_producto;
DELETE FROM almacen;
DELETE FROM proveedor;
DELETE FROM tipo_proveedor;
DELETE FROM rubro;
DELETE FROM usuario;
DELETE FROM rol;
DELETE FROM cuenta;


-- 1. permiso (Permisos del sistema)
DELETE FROM rol_permiso;
DELETE FROM permiso;

INSERT INTO permiso (id, codigo, nombre, url_pattern, descripcion, categoria) VALUES
-- Contabilidad
(1, 'CUENTAS_VER', 'Ver Plan de Cuentas', '/cuentas/**', 'Permite ver el plan de cuentas', 'Contabilidad'),
(2, 'CUENTAS_CREAR', 'Crear Cuentas', '/cuentas/nueva', 'Permite crear nuevas cuentas contables', 'Contabilidad'),
(3, 'CUENTAS_EDITAR', 'Editar Cuentas', '/cuentas/editar/**', 'Permite editar cuentas contables', 'Contabilidad'),
(4, 'ASIENTOS_VER', 'Ver Asientos', '/asientos/**', 'Permite ver asientos contables', 'Contabilidad'),
(5, 'ASIENTOS_CREAR', 'Crear Asientos', '/asientos/nuevo', 'Permite crear asientos contables', 'Contabilidad'),
(6, 'REPORTES_CONTABLES', 'Reportes Contables', '/reportes/**', 'Permite ver reportes contables (Libro Diario, Mayor, IVA)', 'Contabilidad'),
-- Abastecimiento
(7, 'SOLICITUDES_VER', 'Ver Solicitudes de Compra', '/solicitudes-compra/**', 'Permite ver solicitudes de compra', 'Abastecimiento'),
(8, 'SOLICITUDES_CREAR', 'Crear Solicitudes de Compra', '/solicitudes-compra/nueva', 'Permite crear solicitudes de compra', 'Abastecimiento'),
(9, 'PEDIDOS_COT_VER', 'Ver Pedidos de Cotización', '/pedidos-cotizacion/**', 'Permite ver pedidos de cotización', 'Abastecimiento'),
(10, 'PEDIDOS_COT_GESTIONAR', 'Gestionar Pedidos de Cotización', '/pedidos-cotizacion/crear/**', 'Permite crear y gestionar pedidos de cotización', 'Abastecimiento'),
(11, 'ORDENES_VER', 'Ver Órdenes de Compra', '/ordenes-compra/**', 'Permite ver órdenes de compra', 'Abastecimiento'),
(12, 'ORDENES_CREAR', 'Crear Órdenes de Compra', '/ordenes-compra/nueva/**', 'Permite crear órdenes de compra', 'Abastecimiento'),
(13, 'REMITOS_VER', 'Ver Remitos', '/remitos/**', 'Permite ver remitos de recepción', 'Abastecimiento'),
(14, 'REMITOS_CREAR', 'Crear Remitos', '/remitos/nuevo/**', 'Permite registrar remitos de recepción', 'Abastecimiento'),
(15, 'FACTURAS_VER', 'Ver Facturas', '/facturas/**', 'Permite ver facturas de compra', 'Abastecimiento'),
(16, 'FACTURAS_CREAR', 'Crear Facturas', '/facturas/nueva/**', 'Permite registrar facturas de compra', 'Abastecimiento'),
-- Inventario
(17, 'PRODUCTOS_VER', 'Ver Productos', '/productos/**', 'Permite ver productos', 'Inventario'),
(18, 'PRODUCTOS_GESTIONAR', 'Gestionar Productos', '/productos/nuevo', 'Permite crear y editar productos', 'Inventario'),
(19, 'CATEGORIAS_VER', 'Ver Categorías', '/categorias/**', 'Permite ver categorías de productos', 'Inventario'),
(20, 'CATEGORIAS_GESTIONAR', 'Gestionar Categorías', '/categorias/nueva', 'Permite crear y editar categorías', 'Inventario'),
(21, 'ALMACENES_VER', 'Ver Almacenes', '/almacenes/**', 'Permite ver almacenes', 'Inventario'),
(22, 'ALMACENES_GESTIONAR', 'Gestionar Almacenes', '/almacenes/nuevo', 'Permite crear y editar almacenes', 'Inventario'),
-- Proveedores
(23, 'PROVEEDORES_VER', 'Ver Proveedores', '/proveedores/**', 'Permite ver proveedores', 'Proveedores'),
(24, 'PROVEEDORES_GESTIONAR', 'Gestionar Proveedores', '/proveedores/nuevo', 'Permite crear y editar proveedores', 'Proveedores'),
-- Ventas
(25, 'VENTAS_VER', 'Ver Ventas', '/ventas/**', 'Permite ver el historial de ventas', 'Ventas'),
(26, 'VENTAS_CREAR', 'Registrar Ventas', '/ventas/nueva', 'Permite registrar ventas', 'Ventas'),
-- Auditoría
(27, 'AUDITORIA_VER', 'Ver Auditoría', '/auditoria/**', 'Permite ver los registros de auditoría', 'Auditoría'),
-- Presupuestos
(28, 'PRESUPUESTOS_VER', 'Ver Presupuestos', '/presupuestos/**', 'Permite ver presupuestos', 'Abastecimiento'),
(29, 'PRESUPUESTOS_GESTIONAR', 'Gestionar Presupuestos', '/presupuestos/confirmar/**', 'Permite gestionar presupuestos', 'Abastecimiento');

-- 2. rol
INSERT INTO rol (id, name, editable) VALUES
(1, 'ADMIN', false),
(2, 'CONTADOR', true),
(3, 'DEPOSITO', true),
(4, 'COMERCIAL', true);

-- 2.1 rol_permiso (Asignar permisos a roles)
-- CONTADOR: Acceso a contabilidad
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), -- Contabilidad completa
(2, 15), (2, 16); -- Facturas

-- DEPOSITO: Acceso a inventario y recepción
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES
(3, 13), (3, 14), -- Remitos
(3, 17), (3, 18), -- Productos
(3, 19), (3, 20), -- Categorías
(3, 21), (3, 22); -- Almacenes

-- COMERCIAL: Acceso a abastecimiento y ventas
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES
(4, 7), (4, 8), -- Solicitudes
(4, 9), (4, 10), -- Pedidos cotización
(4, 11), (4, 12), -- Órdenes compra
(4, 23), (4, 24), -- Proveedores
(4, 25), (4, 26), -- Ventas
(4, 28), (4, 29); -- Presupuestos

-- 2. usuario
-- Contraseñas: admin=admin, contador1=contador1, deposito1=deposito1, comercial1=comercial1
INSERT INTO usuario (id, username, password, rol_id) VALUES
(1, 'admin', '$2a$10$DOwdDPyYOiEGIP9f5UmOTOlyoMJu/33FhEiV9WcSh1/vKyTOxAF7O', 1),
(2, 'contador1', '$2a$10$0zj5WdanPLYn8O5SvE4YpOX9b6jKYGFw1WN1gJF5BVYaUWf4WjjTq', 2),
(3, 'deposito1', '$2a$10$FQ3V1BafQTj9Z2MhJYj5GOmKF4VqYgLxJFhPz9MdKf5Y2QjKYfJmu', 3),
(4, 'comercial1', '$2a$10$Xy5P9FgHYjT5Q4fKlJjZAOLkHf3g2RnM9WcXVjY8dP6QzF5yJkT3e', 4);

-- 3. cuenta (Plan de Cuentas Básico)
INSERT INTO cuenta (id, codigo, nombre, tipo_cuenta, activo) VALUES
(1, '1.1.1', 'Caja', 'ACTIVO', true),
(2, '1.1.2', 'Banco Nación C/C', 'ACTIVO', true),
(3, '1.2.1', 'Mercaderías', 'ACTIVO', true),
(4, '1.2.2', 'IVA Crédito Fiscal', 'ACTIVO', true),
(5, '2.1.1', 'Proveedores', 'PASIVO', true),
(6, '2.2.1', 'IVA Débito Fiscal', 'PASIVO', true),
(7, '4.1.1', 'Costo de Mercaderías Vendidas', 'RESULTADO_NEGATIVO', true),
(8, '5.1.1', 'Ventas', 'RESULTADO_POSITIVO', true);

-- 4. rubro
INSERT INTO rubro (id, nombre) VALUES
(1, 'Informática'),
(2, 'Librería'),
(3, 'Limpieza'),
(4, 'Mobiliario');

-- 5. tipo_proveedor
INSERT INTO tipo_proveedor (id, nombre) VALUES
(1, 'Monotributista'),
(2, 'Responsable Inscripto');

-- 6. proveedor
INSERT INTO proveedor (id, nombre, razon_social, cuit, telefono, correo, provincia, ciudad, codigo_postal, direccion, banco, numero_cuenta, tipo_proveedor_id, rubro_id, activo) VALUES
(1, 'Tech Solutions', 'Tech Solutions S.A.', '30-11223344-5', '11-4567-8901', 'ventas@techsolutions.com', 'Buenos Aires', 'CABA', '1425', 'Av. Corrientes 1234', 'Santander', '123456789', 2, 1, true),
(2, 'Librería El Saber', 'El Saber de Juan Perez', '20-22334455-6', '11-9876-5432', 'contacto@elsaber.com.ar', 'Buenos Aires', 'CABA', '1426', 'Av. Santa Fe 4321', 'Galicia', '987654321', 1, 2, true),
(3, 'Limpiamax', 'Limpiamax S.R.L.', '30-55667788-9', '11-1234-5678', 'info@limpiamax.net', 'Buenos Aires', 'Avellaneda', '1870', 'Mitre 500', 'Provincia', '555444333', 2, 3, true);

-- 7. almacen
INSERT INTO almacen (id, nombre, ubicacion) VALUES
(1, 'Depósito Central', 'Nave 1, Sector A'),
(2, 'Depósito Secundario', 'Nave 2, Sector B');

-- 8. categoria_producto
INSERT INTO categoria_producto (id, nombre) VALUES
(1, 'Hardware'),
(2, 'Software'),
(3, 'Insumos de Oficina'),
(4, 'Artículos de Limpieza');

-- 9. producto
INSERT INTO producto (id, codigo, nombre, marca, modelo, descripcion, precio_costo, precio_venta, iva, stock_actual, stock_maximo, stock_minimo, punto_reposicion, categoria_id, almacen_id, activo) VALUES
(1, 'HW-001', 'Notebook 15"', 'HP', 'Pavilion 15', 'Notebook 15.6" Full HD, Core i5, 8GB RAM, 256GB SSD', 850.00, 1200.00, 0.21, 10, 50, 5, 10, 1, 1, true),
(2, 'HW-002', 'Monitor 24"', 'Samsung', 'F24T35', 'Monitor LED 24" Full HD, panel IPS', 180.00, 250.00, 0.21, 25, 100, 10, 20, 1, 1, true),
(3, 'OF-001', 'Resma A4 75g', 'Ledesma', 'NAT', 'Resma de papel A4, 75 gramos, 500 hojas', 5.00, 8.50, 0.21, 200, 500, 50, 100, 3, 2, true),
(4, 'LP-001', 'Lavandina 1L', 'Ayudín', 'Clásica', 'Lavandina clásica con cloro, 1 Litro', 1.50, 2.50, 0.21, 150, 300, 40, 80, 4, 2, true);

-- 10. producto_proveedor (Linking table)
INSERT INTO producto_proveedor (id, producto_id, proveedor_id) VALUES
(1, 1, 1), -- Notebook from Tech Solutions
(2, 2, 1), -- Monitor from Tech Solutions
(3, 3, 2), -- Resma from Librería El Saber
(4, 4, 3); -- Lavandina from Limpiamax

-- 11. solicitud_compra
INSERT INTO solicitud_compra (id, fecha, observaciones, estado, proveedor_sugerido) VALUES
(1, '2025-11-20', 'Solicitud de hardware.', 'PRESUPUESTADA', 'Tech Solutions'),
(2, '2025-11-20', 'Solicitud de insumos de oficina.', 'PRESUPUESTADA', 'Librería El Saber');

-- 12. solicitud_compra_item
INSERT INTO solicitud_compra_item (id, solicitud_compra_id, producto_id, cantidad, descripcion, precio_unitario) VALUES
(1, 1, 1, 5, 'Notebook 15" HP Pavilion 15', 850.00),
(2, 2, 3, 50, 'Resma A4 75g Ledesma NAT', 5.00);

-- 13. pedido_cotizacion (NEW STEP)
INSERT INTO pedido_cotizacion (id, fecha, proveedor_id, solicitud_compra_id, estado) VALUES
(1, '2025-11-21', 1, 1, 'FINALIZADO'), -- Cotización para la SC 1, que ya se convirtió en OC.
(2, '2025-11-21', 2, 2, 'COTIZADO');  -- Cotización para la SC 2, para que aparezca en el listado.

-- 14. pedido_cotizacion_item (NEW STEP)
-- Items para la cotización 1 (Notebooks)
INSERT INTO pedido_cotizacion_item (id, pedido_cotizacion_id, producto_id, cantidad, precio_unitario_cotizado, total_item_cotizado) VALUES
(1, 1, 1, 5, 850.00, 4250.00);
-- Items para la cotización 2 (Papel)
INSERT INTO pedido_cotizacion_item (id, pedido_cotizacion_id, producto_id, cantidad, precio_unitario_cotizado, total_item_cotizado) VALUES
(2, 2, 3, 50, 5.50, 275.00); -- El proveedor cotizó un poco más caro

-- 15. orden_compra
UPDATE solicitud_compra SET estado = 'COMPROMETIDA' WHERE id = 1; -- Actualizamos el estado de la SC que tiene OC
INSERT INTO orden_compra (id, fecha, proveedor_id, estado, forma_pago, plazo_pago, con_envio, subtotal, iva, total, solicitud_compra_id) VALUES
(1, '2025-11-22', 1, 'RECIBIDA_PARCIAL', 'Transferencia Bancaria', '30 días', true, 4250.00, 892.50, 5142.50, 1); -- For Notebooks from SC 1

-- 16. orden_compra_item
INSERT INTO orden_compra_item (id, orden_compra_id, producto_id, cantidad, precio_unitario, total) VALUES
(1, 1, 1, 5, 850.00, 4250.00);

-- 17. remito (Receipt of goods)
INSERT INTO remito (id, fecha, orden_compra_id) VALUES
(1, '2025-11-28', 1); -- Remito for the notebooks OC

-- 18. remito_item
INSERT INTO remito_item (id, remito_id, producto_id, cantidad) VALUES
(1, 1, 1, 3); -- Received 3 out of 5 notebooks

-- Update stock for received products
UPDATE producto SET stock_actual = stock_actual + 3 WHERE id = 1;

-- 19. asiento (Accounting entries)
-- Asiento for the first invoice (Notebooks)
INSERT INTO asiento (id, fecha, descripcion, usuario_creador_id) VALUES
(1, '2025-12-01', 'Factura A-0001-00001234 - Tech Solutions S.A.', 3);

-- 20. factura
INSERT INTO factura (id, numero_factura, fecha, orden_compra_id, total, asiento_id) VALUES
(1, 'A-0001-00001234', '2025-12-01', 1, 5142.50, 1);

-- 21. movimiento (Details of the accounting entry)
-- Mercaderías (Activo que aumenta por el DEBE)
INSERT INTO movimiento (id, asiento_id, cuenta_id, debe, haber) VALUES
(1, 1, 3, 4250.00, 0.00);
-- IVA Crédito Fiscal (Activo que aumenta por el DEBE)
INSERT INTO movimiento (id, asiento_id, cuenta_id, debe, haber) VALUES
(2, 1, 4, 892.50, 0.00);
-- Proveedores (Pasivo que aumenta por el HABER)
INSERT INTO movimiento (id, asiento_id, cuenta_id, debe, haber) VALUES
(3, 1, 5, 0.00, 5142.50);

-- Note: Sequence updates are needed after manual ID insertion.
-- This ensures that the next auto-generated ID is correct.
SELECT setval('permiso_id_seq', (SELECT MAX(id) FROM permiso));
SELECT setval('rol_id_seq', (SELECT MAX(id) FROM rol));
SELECT setval('usuario_id_seq', (SELECT MAX(id) FROM usuario));
SELECT setval('cuenta_id_seq', (SELECT MAX(id) FROM cuenta));
SELECT setval('rubro_id_seq', (SELECT MAX(id) FROM rubro));
SELECT setval('tipo_proveedor_id_seq', (SELECT MAX(id) FROM tipo_proveedor));
SELECT setval('proveedor_id_seq', (SELECT MAX(id) FROM proveedor));
SELECT setval('almacen_id_seq', (SELECT MAX(id) FROM almacen));
SELECT setval('categoria_producto_id_seq', (SELECT MAX(id) FROM categoria_producto));
SELECT setval('producto_id_seq', (SELECT MAX(id) FROM producto));
SELECT setval('producto_proveedor_id_seq', (SELECT MAX(id) FROM producto_proveedor));
SELECT setval('solicitud_compra_id_seq', (SELECT MAX(id) FROM solicitud_compra));
SELECT setval('solicitud_compra_item_id_seq', (SELECT MAX(id) FROM solicitud_compra_item));
SELECT setval('pedido_cotizacion_id_seq', (SELECT MAX(id) FROM pedido_cotizacion));
SELECT setval('pedido_cotizacion_item_id_seq', (SELECT MAX(id) FROM pedido_cotizacion_item));
SELECT setval('orden_compra_id_seq', (SELECT MAX(id) FROM orden_compra));
SELECT setval('orden_compra_item_id_seq', (SELECT MAX(id) FROM orden_compra_item));
SELECT setval('remito_id_seq', (SELECT MAX(id) FROM remito));
SELECT setval('remito_item_id_seq', (SELECT MAX(id) FROM remito_item));
SELECT setval('asiento_id_seq', (SELECT MAX(id) FROM asiento));
SELECT setval('movimiento_id_seq', (SELECT MAX(id) FROM movimiento));
