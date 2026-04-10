# Migración de Tabla Domiciliario

## 📋 Cambios Realizados

### 1. **Base de Datos (SQL)**
- ✅ Agregar columnas: `user_id`, `vehiculo`, `placa`, `calificacion`
- ✅ Agregar FK: `domiciliario.user_id` → `users.id`
- ✅ Agregar columnas a `users`: `numero_documento`, `telefono`

### 2. **Backend (Java)**
- ✅ Actualizar entidad `Domiciliario.java` con relación `@ManyToOne` a `User`
- ✅ Crear DTO `DomiciliarioDTO.java` para respuesta con datos completos
- ✅ Actualizar `DomiciliarioController.java` para devolver DTO
- ✅ Actualizar `DomiciliarioService.java` con método `findNearbyDTO()`
- ✅ Actualizar entidad `User.java` con campo `numeroDocumento`

### 3. **Frontend (React Native)**
- ✅ Actualizar interfaz `Driver` en `app/(cliente)/mapa.tsx`

---

## 🚀 Pasos para Aplicar los Cambios

### **Paso 1: Ejecutar Script SQL**

```bash
# 1. Abre pgAdmin o psql
# 2. Conecta a la base de datos: neondb?sslmode
# 3. Ejecuta el archivo:
```

```sql
-- src/main/resources/db/migration/add_domiciliario_fields.sql

-- Agregar campos faltantes a DOMICILIARIO
ALTER TABLE public.domiciliario
ADD COLUMN IF NOT EXISTS user_id INTEGER,
ADD COLUMN IF NOT EXISTS vehiculo VARCHAR(50),
ADD COLUMN IF NOT EXISTS placa VARCHAR(20),
ADD COLUMN IF NOT EXISTS calificacion DECIMAL(3,2) DEFAULT 5.0;

-- Agregar FK a users
ALTER TABLE public.domiciliario
ADD CONSTRAINT fk_domiciliario_user
FOREIGN KEY (user_id) REFERENCES public.users(id)
ON DELETE CASCADE;

-- Agregar campos faltantes a USERS  
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS numero_documento VARCHAR(20),
ADD COLUMN IF NOT EXISTS telefono VARCHAR(20);

-- Conectar domiciliario existente con user (id=2 es "Domiciliario Test")
UPDATE public.domiciliario
SET user_id = 2,
    vehiculo = 'Moto',
    placa = 'ABC-123',
    calificacion = 4.8
WHERE id_domiciliario = 1;
```

### **Paso 2: Verificar BD**

```sql
-- Ver estructura actualizada
\d domiciliario

-- Ver datos
SELECT d.id_domiciliario, d.latitud, d.longitud, d.vehiculo, d.placa, 
       d.calificacion, u.nombre, u.email
FROM domiciliario d
LEFT JOIN users u ON d.user_id = u.id;
```

**Resultado esperado:**
```
 id_domiciliario | latitud | longitud | vehiculo | placa   | calificacion | nombre            | email
-----------------+---------+----------+----------+---------+--------------+-------------------+------------------------
               1 |  7.8939 |  -72.508 | Moto     | ABC-123 |         4.80 | Domiciliario Test | domiciliario@example.com
```

### **Paso 3: Reiniciar Backend**

```bash
cd C:\Prueba\p2p-domicilios-backend

# Limpiar y compilar
./mvnw clean install

# Ejecutar
./mvnw spring-boot:run
```

### **Paso 4: Probar el Endpoint**

```bash
# Test 1: Domiciliarios cercanos
curl "http://localhost:8080/drivers/nearby?lat=7.8939&lon=-72.5078"

# Respuesta esperada:
[
  {
    "id": 1,
    "nombre": "Domiciliario Test",
    "email": "domiciliario@example.com",
    "latitud": 7.8939,
    "longitud": -72.5078,
    "disponible": true,
    "verificado": false,
    "vehiculo": "Moto",
    "placa": "ABC-123",
    "calificacion": 4.8,
    "distancia": null
  }
]
```

### **Paso 5: Probar Frontend**

```bash
cd C:\Prueba\p2p-domicilios-mobile

# Limpiar caché
npm start -- --clear

# Abrir en iOS/Android
# El mapa ahora mostrará:
# - ✅ Nombre del domiciliario
# - ✅ Calificación (⭐ 4.8)
# - ✅ Vehículo (Moto)
# - ✅ Sin errores de "key" prop
```

---

## ✅ Verificación Final

### **Checklist:**

- [ ] SQL ejecutado sin errores
- [ ] FK creada correctamente
- [ ] Backend compila sin errores
- [ ] Endpoint `/drivers/nearby` devuelve nombre, vehiculo, placa, calificacion
- [ ] Frontend muestra datos completos en DriverCard
- [ ] No hay errores de "key" prop en React

---

## 🔄 Crear Nuevos Domiciliarios

```sql
-- 1. Crear usuario tipo DOMICILIARIO
INSERT INTO users (username, email, password, role, nombre, telefono, enabled, fecha_registro)
VALUES (
  'carlos.mendoza',
  'carlos@example.com', 
  '$2a$10$...', -- password encriptado
  'DOMICILIARIO',
  'Carlos Mendoza',
  '3001234567',
  true,
  NOW()
) RETURNING id;

-- 2. Crear registro en domiciliario (usar el ID del usuario creado)
INSERT INTO domiciliario (user_id, latitud, longitud, disponible, verificado, vehiculo, placa, calificacion)
VALUES (
  3, -- ID del usuario creado arriba
  7.8945,
  -72.5085,
  true,
  true,
  'Moto',
  'XYZ-789',
  4.9
);
```

---

## 🐛 Troubleshooting

### **Error: FK constraint fails**
```
ERROR: insert or update on table "domiciliario" violates foreign key constraint
```
**Solución**: Verifica que el `user_id` exista en la tabla `users` y tenga `role='DOMICILIARIO'`

### **Error: Column does not exist**
```
ERROR: column "user_id" does not exist
```
**Solución**: El script SQL no se ejecutó. Ejecuta manualmente el `ALTER TABLE`.

### **Backend: Cannot find symbol User**
```
error: cannot find symbol class User
```
**Solución**: Asegúrate de que `User.java` está en el mismo paquete `entities`.

---

## 📝 Notas Adicionales

- La tabla `usuario` parece ser legacy y NO se está usando
- Spring Security usa la tabla `users`
- El campo `location` (geometry) se actualiza automáticamente con `@PrePersist/@PreUpdate`
- El endpoint sigue soportando `radiusKm` como parámetro opcional (default: 3km)

---

**Última actualización**: 2026-04-10
**Autor**: Claude Code
