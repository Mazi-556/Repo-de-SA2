-- ============================================================================
-- SCRIPT DE LIMPIEZA: MÓDULO DE ABASTECIMIENTO (Solo transaccionales)
-- ============================================================================
-- Este script elimina todos los datos transaccionales de Abastecimiento
-- MANTIENE: Productos, Proveedores, Contabilidad completa
-- ELIMINA: Solicitudes, Pedidos, Órdenes, Remitos, Facturas
-- ============================================================================

BEGIN;

-- ============================================================================
-- PASO 1: ELIMINAR FACTURAS Y SU RELACIÓN CON ASIENTOS
-- ============================================================================
-- Nota: NO eliminamos los asientos contables, solo desvinculamos las facturas
UPDATE factura SET asiento_id = NULL WHERE asiento_id IS NOT NULL;

DELETE FROM factura;

RAISE NOTICE 'Facturas eliminadas';

-- ============================================================================
-- PASO 2: ELIMINAR REMITOS Y SUS ITEMS
-- ============================================================================
DELETE FROM remito_item;
DELETE FROM remito;

RAISE NOTICE 'Remitos y sus items eliminados';

-- ============================================================================
-- PASO 3: ELIMINAR ÓRDENES DE COMPRA Y SUS ITEMS
-- ============================================================================
DELETE FROM orden_compra_item;
DELETE FROM orden_compra;

RAISE NOTICE 'Órdenes de compra y sus items eliminados';

-- ============================================================================
-- PASO 4: ELIMINAR PEDIDOS DE COTIZACIÓN Y SUS ITEMS
-- ============================================================================
DELETE FROM pedido_cotizacion_item;
DELETE FROM pedido_cotizacion;

RAISE NOTICE 'Pedidos de cotización y sus items eliminados';

-- ============================================================================
-- PASO 5: ELIMINAR SOLICITUDES DE COMPRA Y SUS ITEMS
-- ============================================================================
DELETE FROM solicitud_compra_item;
DELETE FROM solicitud_compra;

RAISE NOTICE 'Solicitudes de compra y sus items eliminados';

-- ============================================================================
-- PASO 6: REINICIAR SECUENCIAS (IDs vuelven a empezar desde 1)
-- ============================================================================
ALTER SEQUENCE IF EXISTS factura_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS remito_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS orden_compra_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS pedido_cotizacion_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS solicitud_compra_id_seq RESTART WITH 1;

-- También reiniciar secuencias de items
ALTER SEQUENCE IF EXISTS remito_item_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS orden_compra_item_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS pedido_cotizacion_item_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS solicitud_compra_item_id_seq RESTART WITH 1;

RAISE NOTICE 'Secuencias reiniciadas';

-- ============================================================================
-- VERIFICACIÓN: CONTAR REGISTROS RESTANTES
-- ============================================================================
DO $$
DECLARE
    count_productos INTEGER;
    count_proveedores INTEGER;
    count_asientos INTEGER;
    count_cuentas INTEGER;
    count_solicitudes INTEGER;
    count_ordenes INTEGER;
BEGIN
    SELECT COUNT(*) INTO count_productos FROM producto;
    SELECT COUNT(*) INTO count_proveedores FROM proveedor;
    SELECT COUNT(*) INTO count_asientos FROM asiento;
    SELECT COUNT(*) INTO count_cuentas FROM cuenta;
    SELECT COUNT(*) INTO count_solicitudes FROM solicitud_compra;
    SELECT COUNT(*) INTO count_ordenes FROM orden_compra;
    
    RAISE NOTICE '';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'VERIFICACIÓN DE LIMPIEZA COMPLETA';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'MAESTROS MANTENIDOS:';
    RAISE NOTICE '  - Productos: %', count_productos;
    RAISE NOTICE '  - Proveedores: %', count_proveedores;
    RAISE NOTICE '  - Cuentas Contables: %', count_cuentas;
    RAISE NOTICE '  - Asientos Contables: %', count_asientos;
    RAISE NOTICE '';
    RAISE NOTICE 'TRANSACCIONALES ELIMINADOS:';
    RAISE NOTICE '  - Solicitudes de Compra: %', count_solicitudes;
    RAISE NOTICE '  - Órdenes de Compra: %', count_ordenes;
    RAISE NOTICE '========================================';
    RAISE NOTICE '✅ LIMPIEZA COMPLETADA EXITOSAMENTE';
    RAISE NOTICE '========================================';
END $$;

COMMIT;

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
-- IMPORTANTE: Este script es IRREVERSIBLE. 
-- Si necesitas los datos, haz un backup ANTES de ejecutar.
-- ============================================================================
