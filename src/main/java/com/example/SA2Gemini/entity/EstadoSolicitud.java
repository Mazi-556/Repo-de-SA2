package com.example.SA2Gemini.entity;

public enum EstadoSolicitud {
    PENDIENTE,           // Se creó la solicitud de compra
    COTIZANDO,          // Se envió pedido de cotización al proveedor
    COTIZADA,           // El proveedor respondió con precios
    COMPROMETIDA,       // Se creó la Orden de Compra
    INGRESADA_PARCIAL,  // Se recibió mercadería parcialmente (remito parcial)
    INGRESADA,          // Se recibió toda la mercadería (remito)
    FINALIZADA          // Se registró la factura
}
