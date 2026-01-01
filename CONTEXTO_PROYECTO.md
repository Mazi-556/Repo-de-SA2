# Contexto Completo del Proyecto SA2Gemini

## 🎯 Descripción General
Sistema contable y de abastecimiento para un proyecto universitario. El objetivo es que **funcione correctamente**, no necesita código perfecto.

**Stack:** Java Spring Boot, PostgreSQL, Thymeleaf, Bootstrap 5

**Repositorio:** https://github.com/Mazi-556/Repo-de-SA2

---

## 📋 Estructura del Proyecto

El sistema se divide en **dos módulos principales:**

### 1️⃣ Módulo Contable
- **Plan de Cuentas** (`/cuentas`) - Árbol jerárquico expandible con relación padre-hijo
- **Registrar Asiento** (`/asientos/nuevo`)
- **Reportes** - Libro Diario (`/reportes/libro-diario`) y Libro Mayor (`/reportes/libro-mayor`)

### 2️⃣ Módulo Abastecimiento
- **Solicitudes de Compra** (`/solicitudes-compra`) - Con expansión dinámica de observaciones
- **Órdenes de Compra** (`/ordenes-compra`)
- **Pedidos de Cotización** (`/pedidos-cotizacion`)
- **Remitos** (`/remitos`)
- **Facturas** (`/facturas`)
- **Productos** (`/productos`)
- **Proveedores** (`/proveedores`)
- **Ventas** (`/ventas`)

### 3️⃣ Administración (Solo ADMIN)
- **Usuarios** (`/admin/usuarios`)
- **Almacenes** (`/almacenes`)

---

## 🔧 Instrucciones de Ejecución

### Iniciar el Servidor
```bash
mvn spring-boot:run &
```
- **Puerto:** 8081
- **URL:** http://localhost:8081/
- **Base de datos:** PostgreSQL (SA2_BD)

### Reiniciar el Servidor
**Solo reiniciar cuando sea NECESARIO:**
- ✅ Cambios en clases Java (controllers, services, entities, repositories)
- ✅ Cambios en estructura de base de datos
- ❌ NO reiniciar para cambios en templates HTML/CSS/JS (DevTools recarga automáticamente)

### Credentials por Defecto
- **Usuario:** admin
- **Contraseña:** user (ambos usuarios tienen la misma contraseña hasheada)

---

## 💻 Configuración de DevTools
El archivo `application.properties` está configurado para:
- `spring.devtools.restart.enabled=true`
- `spring.devtools.livereload.enabled=true`
- `spring.thymeleaf.cache=false`
- `spring.web.resources.cache.period=0`

Esto permite recarga automática de templates sin reiniciar el servidor.

---

## 📝 Control de Versiones

### Workflow de Commits
1. **Hacer cambios** en el código
2. **Probar en el navegador** (sin reiniciar si es HTML/CSS/JS)
3. **Usuario confirma** que funciona correctamente
4. **SOLO ENTONCES:** hacer commit y push
5. Usar mensajes de commit **claros y en español**

### Comandos Git
```bash
git add -A
git commit -m "Descripción clara en español"
git push origin main
```

**IMPORTANTE:** NO hacer commit/push sin confirmación del usuario

---

## 🎨 Cambios Realizados (Esta Sesión)

### 1. Plan de Cuentas - Árbol Visual
- ✅ Entidad `Cuenta` tiene nuevo campo `cuentaPadreId` para relación jerárquica
- ✅ Formulario permite seleccionar cuenta padre al crear/editar
- ✅ Listado muestra árbol expandible con botones ▼/▶
- ✅ Indentación visual según nivel de profundidad
- ✅ El código de la cuenta NUNCA cambia, solo la relación visual

### 2. Solicitudes de Compra - Observaciones Expandibles
- ✅ Columna "Observaciones" con ancho dinámico
- ✅ Botón "Ver más" aparece SOLO cuando el texto excede el ancho
- ✅ Al expandir, el texto se despliega hacia abajo sin cambiar ancho de columna
- ✅ Botón "Ver menos" para contraer
- ✅ Encabezados centrados con líneas verticales que delimitan columnas

### 3. Home - Dashboard Épico
- ✅ Banner hero con gradiente (667eea → 764ba2)
- ✅ Tarjetas de módulos por sección (Contable, Abastecimiento, Admin)
- ✅ Botones info (ⓘ) discretos que muestran descripción al hacer clic
- ✅ Tooltips con información de cada módulo
- ✅ Todos los endpoints verificados y funcionales

### 4. Navbar - Estilos Globales
- ✅ Gradiente de color épico en navbar (667eea → 764ba2)
- ✅ Fuente más grande y legible (1.1rem, font-weight 700)
- ✅ Texto centrado dinámicamente
- ✅ Estilos aplicados globalmente a TODAS las páginas
- ✅ CSS externo en `src/main/resources/static/css/navbar-style.css`
- ✅ Dropdowns funcionan correctamente

---

## 📌 Acuerdos Importantes

### Sobre Cambios
1. **Solo cambios en HTML/CSS/JS:** Recargar página en navegador
2. **Cambios en Java:** Reiniciar servidor
3. **Siempre probar primero** antes de hacer commit

### Sobre Commits
1. No hacer commit sin confirmar que funciona
2. Mensajes en español, claros y descriptivos
3. Un commit = un cambio coherente

### Sobre UI/UX
1. **Centrar dinámicamente** - no dejar espacios en blanco inútiles
2. **Agregar color** - usar gradientes #667eea → #764ba2
3. **Delimitar con líneas** - separar secciones visualmente
4. **Iconos de Bootstrap** - usar `<i class="bi bi-..."></i>`
5. **Responsive** - todo debe funcionar en mobile también

---

## 🔗 Endpoints Principales

| Módulo | Endpoint | Descripción |
|--------|----------|-------------|
| Contable | `/cuentas` | Plan de cuentas |
| Contable | `/asientos/nuevo` | Registrar asiento |
| Contable | `/reportes/libro-diario` | Libro diario |
| Contable | `/reportes/libro-mayor` | Libro mayor |
| Abastecimiento | `/solicitudes-compra` | Solicitudes |
| Abastecimiento | `/ordenes-compra` | Órdenes de compra |
| Abastecimiento | `/remitos` | Remitos |
| Abastecimiento | `/productos` | Productos |
| Abastecimiento | `/proveedores` | Proveedores |
| Admin | `/admin/usuarios` | Gestión de usuarios |

---

## 🛠️ Solución de Problemas Comunes

### DevTools no recarga
- Solución: `mvn clean spring-boot:run` (limpia caché)

### Estilos no se aplican
- Verificar que el archivo CSS esté en `src/main/resources/static/css/`
- Verificar que el `<link>` esté en el `<head>` de la página

### Dropdowns no funcionan
- Usar `data-bs-toggle="dropdown"` en Bootstrap
- Evitar `href="#"` sin JavaScript adecuado

### Cambios en Java no se aplican
- Reiniciar el servidor: `pkill -f "mvn spring-boot" && mvn spring-boot:run &`

---

## 📊 Base de Datos

**Nombre:** SA2_BD
**Usuario:** postgres
**Contraseña:** 123
**Host:** localhost:5432

### Tablas Principales
- `cuenta` - Plan de cuentas (con campo `cuenta_padre_id`)
- `solicitud_compra` - Solicitudes de compra
- `orden_compra` - Órdenes de compra
- `remito` - Remitos
- `factura` - Facturas
- `producto` - Productos
- `proveedor` - Proveedores
- `usuario` - Usuarios del sistema

---

## 🎯 Próximos Pasos Sugeridos

1. **Mejorar otros módulos** (Contable tiene menos trabajo visual)
2. **Agregar validaciones** en formularios
3. **Mejorar reportes** (hacer más visualmente atractivos)
4. **Agregar búsqueda/filtros** en listados
5. **Optimizar performance** de grandes consultas

---

## 📞 Información del Desarrollador

**Proyecto:** SA2Gemini (Sistema Contable y Abastecimiento)
**Desarrollador:** Facundo Moreno (fdmoreno@comuinidad.unnoba.edu.ar)
**Repositorio:** https://github.com/Mazi-556/Repo-de-SA2
**Estado:** En desarrollo activo

---

## ⚡ Tips para la Próxima Sesión

1. Leer este documento completamente
2. Ejecutar `mvn spring-boot:run &` para iniciar
3. Verificar que la navbar se vea bien en todos lados
4. Probar cada cambio **antes** de hacer commit
5. Mantener el código funcional, no perfecto
6. Usar este documento como referencia constantemente

---

**Última actualización:** 31/12/2025
**Estado del sistema:** ✅ Funcionando correctamente
**Tokens usados en sesión anterior:** 176K/200K
