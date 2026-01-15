# 🚀 Guía de Deployment - SA2Gemini

## 📋 Resumen de Cambios para Producción

Este proyecto ha sido configurado para ser desplegado fácilmente en plataformas cloud.

### Archivos creados:
- `application-prod.properties` - Configuración de producción
- `Dockerfile` - Imagen Docker optimizada (multi-stage build)
- `docker-compose.yml` - Para desarrollo local con Docker
- `railway.json` - Configuración para Railway
- `render.yaml` - Blueprint para Render.com
- `.dockerignore` - Optimiza el build de Docker

---

## 🚂 Opción 1: Railway (Recomendado)

Railway es la opción más simple y tiene un tier gratuito.

### Pasos:

1. **Crear cuenta en Railway**
   - Ve a [railway.app](https://railway.app) y crea una cuenta con GitHub

2. **Crear nuevo proyecto**
   - Click en "New Project"
   - Selecciona "Deploy from GitHub repo"
   - Conecta tu repositorio

3. **Agregar base de datos PostgreSQL**
   - En el proyecto, click en "New"
   - Selecciona "Database" → "PostgreSQL"
   - Railway creará automáticamente la BD

4. **Configurar variables de entorno**
   - Click en tu servicio (la app)
   - Ve a "Variables"
   - Agrega las siguientes variables (reemplaza `Postgres` por el nombre de tu servicio de BD):
   
   ```
   PGHOST=${{Postgres.PGHOST}}
   PGPORT=${{Postgres.PGPORT}}
   PGDATABASE=${{Postgres.PGDATABASE}}
   PGUSER=${{Postgres.PGUSER}}
   PGPASSWORD=${{Postgres.PGPASSWORD}}
   SPRING_PROFILES_ACTIVE=prod
   ```
   
   > **Tip**: También puedes hacer click en "Add Reference Variable" para seleccionar las variables de PostgreSQL directamente.

5. **Deploy!**
   - Railway detectará el Dockerfile automáticamente
   - El deploy comenzará automáticamente

### URL de tu app:
Railway te dará una URL como: `https://tu-app.up.railway.app`

---

## 🎨 Opción 2: Render.com

Render también tiene un tier gratuito generoso.

### Pasos:

1. **Crear cuenta en Render**
   - Ve a [render.com](https://render.com) y crea una cuenta

2. **Usar el Blueprint (automático)**
   - Ve a Dashboard → "New" → "Blueprint"
   - Conecta tu repositorio de GitHub
   - Render leerá el archivo `render.yaml` y creará todo automáticamente

3. **O configurar manualmente:**
   
   **Crear la base de datos:**
   - "New" → "PostgreSQL"
   - Nombre: `sa2gemini-db`
   - Plan: Free
   
   **Crear el Web Service:**
   - "New" → "Web Service"
   - Conecta tu repo
   - Runtime: Docker
   - Plan: Free
   
   **Variables de entorno:**
   ```
   DATABASE_URL=<copiar Internal Database URL de PostgreSQL>
   DATABASE_USERNAME=<usuario de la BD>
   DATABASE_PASSWORD=<password de la BD>
   SPRING_PROFILES_ACTIVE=prod
   PORT=8080
   ```

---

## 🐳 Opción 3: Docker (VPS propio)

Si tienes tu propio servidor (DigitalOcean, AWS EC2, etc.):

### Build y run manual:

```bash
# Construir imagen
docker build -t sa2gemini .

# Ejecutar con variables de entorno
docker run -d \
  -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://tu-host:5432/tu_bd \
  -e DATABASE_USERNAME=tu_usuario \
  -e DATABASE_PASSWORD=tu_password \
  -e SPRING_PROFILES_ACTIVE=prod \
  --name sa2gemini \
  sa2gemini
```

### Con Docker Compose (desarrollo local):

```bash
# Levanta PostgreSQL + App
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar todo
docker-compose down
```

---

## ⚙️ Variables de Entorno Requeridas

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `DATABASE_URL` | URL de conexión JDBC | `jdbc:postgresql://host:5432/db` |
| `DATABASE_USERNAME` | Usuario de PostgreSQL | `postgres` |
| `DATABASE_PASSWORD` | Contraseña de PostgreSQL | `secreto123` |
| `PORT` | Puerto del servidor | `8080` |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring | `prod` |

---

## 🔒 Consideraciones de Seguridad

1. **Cambiar contraseñas por defecto**
   - El usuario `admin` tiene contraseña `admin`
   - Cámbiala después del primer login

2. **HTTPS**
   - Railway y Render proveen HTTPS automáticamente
   - Si usas VPS propio, configura nginx + Let's Encrypt

3. **Variables de entorno**
   - NUNCA commitees contraseñas reales al repositorio
   - Usa siempre variables de entorno en producción

---

## 🐛 Troubleshooting

### La app no inicia
- Verifica que `DATABASE_URL` sea accesible desde la app
- Revisa los logs: `railway logs` o en el dashboard de Render

### Error de conexión a BD
- Asegúrate de que la BD esté en la misma red/región
- Verifica que el usuario tenga permisos

### La app es muy lenta
- El tier gratuito tiene recursos limitados
- La app puede "dormirse" después de inactividad (cold start)

### Errores de memoria
- Agrega `-Xmx256m` al comando Java si hay límites de RAM

---

## 📊 Comandos Útiles

```bash
# Build local para producción
./mvnw package -Pprod -DskipTests

# Ejecutar JAR localmente con perfil prod
java -Dspring.profiles.active=prod -jar target/SA2Gemini-0.0.1-SNAPSHOT.jar

# Ver tamaño de la imagen Docker
docker images sa2gemini
```

---

## 🎯 Checklist Pre-Deploy

- [ ] Variables de entorno configuradas
- [ ] Base de datos PostgreSQL creada
- [ ] Contraseñas por defecto cambiadas
- [ ] `ddl-auto` en `update` (no `create`)
- [ ] Perfil `prod` activado
- [ ] HTTPS habilitado

---

**¡Listo para deploy!** 🎉
