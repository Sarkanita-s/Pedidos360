# Pedidos360 — Guía de instalación en Windows

## 1. Requisitos previos

Para ejecutar el backend en Windows necesitas:

- Windows 10 u 11.
- Java JDK instalado.
- Maven instalado o Maven Wrapper incluido en el proyecto.
- PostgreSQL instalado.
- Git, opcionalmente, para clonar el repositorio.
- Postman para probar la API.

Puedes comprobar Java y Maven desde PowerShell:

```powershell
java -version
mvn -version
```

---

## 2. Instalar Java

1. Descarga e instala un JDK compatible con el proyecto.
2. Durante la instalación, configura la variable `JAVA_HOME` si el instalador lo permite.
3. Cierra y vuelve a abrir PowerShell.

Comprueba la instalación:

```powershell
java -version
javac -version
```

Si Windows no reconoce el comando, revisa que la carpeta `bin` del JDK esté incluida en la variable de entorno `Path`.

---

## 3. Instalar Maven

1. Descarga Apache Maven desde su sitio oficial.
2. Extrae el archivo ZIP, por ejemplo, en:

```text
C:\Program Files\Apache\maven
```

3. Crea o modifica las variables de entorno:

```text
MAVEN_HOME=C:\Program Files\Apache\maven
```

Agrega esta ruta al `Path`:

```text
%MAVEN_HOME%\bin
```

4. Abre una nueva ventana de PowerShell.

Comprueba la instalación:

```powershell
mvn -version
```

> Si el proyecto incluye `mvnw.cmd`, puedes utilizar Maven Wrapper sin instalar Maven globalmente.

---

## 4. Instalar PostgreSQL en Windows

1. Descarga el instalador de PostgreSQL para Windows.
2. Ejecuta el instalador como administrador.
3. Selecciona los componentes:
   - PostgreSQL Server.
   - pgAdmin 4.
   - Command Line Tools.
4. Define una contraseña para el usuario administrador `postgres`.
5. Mantén el puerto predeterminado:

```text
5432
```

6. Finaliza la instalación.

La contraseña del usuario `postgres` debe guardarse de forma segura. No la publiques en el repositorio.

---

## 5. Inicializar y comprobar PostgreSQL

El instalador de Windows normalmente inicializa el clúster automáticamente.

Para comprobar que PostgreSQL está funcionando:

1. Abre el menú Inicio.
2. Busca `Services`.
3. Localiza un servicio parecido a:

```text
postgresql-x64-18
```

4. Comprueba que su estado sea `Running`.
5. Si está detenido, selecciona `Start`.

También puedes revisar el servicio desde PowerShell como administrador:

```powershell
Get-Service *postgres*
```

---

## 6. Crear el usuario y la base de datos

Puedes utilizar **SQL Shell (psql)** o **pgAdmin 4**.

### 6.1. Opción A: usando SQL Shell (psql)

Abre `SQL Shell (psql)` desde el menú Inicio e introduce los datos solicitados:

```text
Server: localhost
Database: postgres
Port: 5432
Username: postgres
Password: la contraseña definida durante la instalación
```

Dentro de `psql`, crea el usuario de la aplicación:

```sql
CREATE USER pedidos_user WITH PASSWORD 'CAMBIAR_ESTA_CONTRASENA';
```

Si el usuario ya existe:

```sql
ALTER USER pedidos_user WITH PASSWORD 'CAMBIAR_ESTA_CONTRASENA';
```

Crea la base de datos:

```sql
CREATE DATABASE pedidos360 OWNER pedidos_user;
```

Conéctate a la base de datos:

```sql
\c pedidos360
```

Otorga los permisos:

```sql
GRANT USAGE, CREATE ON SCHEMA public TO pedidos_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO pedidos_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO pedidos_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT ALL PRIVILEGES ON TABLES TO pedidos_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT ALL PRIVILEGES ON SEQUENCES TO pedidos_user;
```

Salir de `psql`:

```sql
\q
```

### 6.2. Opción B: usando pgAdmin 4

1. Abre pgAdmin 4.
2. Conéctate al servidor PostgreSQL.
3. Haz clic derecho en `Login/Group Roles`.
4. Selecciona `Create > Login/Group Role`.
5. Crea el usuario:

```text
pedidos_user
```

6. En la pestaña de seguridad, define una contraseña.
7. Haz clic derecho en `Databases`.
8. Selecciona `Create > Database`.
9. Define:

```text
Database: pedidos360
Owner: pedidos_user
```

10. Guarda los cambios.

Después, abre el Query Tool de la base de datos `pedidos360` y ejecuta:

```sql
GRANT USAGE, CREATE ON SCHEMA public TO pedidos_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO pedidos_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO pedidos_user;
```

---

## 7. Configurar el backend

Abre el archivo:

```text
backend\src\main\resources\application.properties
```

Configura la conexión:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pedidos360
spring.datasource.username=pedidos_user
spring.datasource.password=CAMBIAR_ESTA_CONTRASENA
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8080
```

La contraseña de `spring.datasource.password` debe coincidir con la contraseña de `pedidos_user`.

> No subas contraseñas reales a GitHub. Para un proyecto real, utiliza variables de entorno o un gestor de secretos.

---

## 8. Probar la conexión desde PowerShell

Si `psql` está incluido en el `Path`, ejecuta:

```powershell
psql -h localhost -U pedidos_user -d pedidos360 -W
```

Ingresa la contraseña del usuario `pedidos_user`.

Dentro de PostgreSQL, puedes listar las tablas:

```sql
\dt
```

Salir:

```sql
\q
```

Si Windows no reconoce `psql`, ejecuta el programa usando su ruta completa. Un ejemplo habitual es:

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -U pedidos_user -d pedidos360 -W
```

La versión de la carpeta puede cambiar según la versión instalada.

---

## 9. Levantar el backend en Windows

Abre PowerShell y navega a la carpeta del proyecto:

```powershell
cd "$HOME\Pedidos360\backend"
```

Si el proyecto se encuentra en otra ubicación, utiliza esa ruta.

### Opción A: Maven instalado

```powershell
mvn spring-boot:run
```

### Opción B: Maven Wrapper

Si existe `mvnw.cmd`:

```powershell
.\mvnw.cmd spring-boot:run
```

Cuando la aplicación se inicia correctamente, aparecerá un mensaje parecido a:

```text
Started BackendApplication
```

La API estará disponible en:

```text
http://localhost:8080
```

Para detenerla:

```text
Ctrl + C
```

---

## 10. Contraseña de Spring Security

En el modo actual de desarrollo, Spring Security genera una contraseña temporal al iniciar el backend.

En la terminal aparecerá una línea similar a:

```text
Using generated security password: ...
```

En Postman, utiliza:

```text
Username: user
Password: contraseña generada en la terminal
```

Esta contraseña es temporal y no debe utilizarse como solución de autenticación para producción.

---

## 11. Probar con Postman

### Configuración común

En cada solicitud protegida:

1. Abre la pestaña `Authorization`.
2. Selecciona `Basic Auth`.
3. En `Username`, escribe:

```text
user
```

4. En `Password`, utiliza la contraseña generada por Spring Boot.

### Listar productos

```http
GET http://localhost:8080/api/catalog/products
```

### Crear un producto

```http
POST http://localhost:8080/api/catalog/products
```

Body `raw` con tipo `JSON`:

```json
{
  "name": "Pizza familiar",
  "description": "Pizza familiar con queso y pepperoni",
  "price": 12990.00,
  "stock": 25
}
```

### Listar pedidos

```http
GET http://localhost:8080/api/orders
```

### Crear un pedido

```http
POST http://localhost:8080/api/orders
```

Body:

```json
{
  "customerName": "Sofia",
  "total": 25980.00,
  "items": [
    {
      "quantity": 2,
      "unitPrice": 12990.00,
      "product": {
        "id": 2
      }
    }
  ]
}
```

### Cambiar el estado de un pedido

```http
PUT http://localhost:8080/api/orders/4/status
```

Body:

```json
"ACEPTADO"
```

---

## 12. Problemas frecuentes en Windows

### Puerto 8080 ocupado

Comprobar qué proceso utiliza el puerto:

```powershell
netstat -ano | findstr :8080
```

Finalizar el proceso utilizando su PID:

```powershell
taskkill /PID NUMERO_DEL_PID /F
```

### PostgreSQL no inicia

1. Abre `Services`.
2. Busca el servicio `postgresql-x64-*`.
3. Comprueba que esté iniciado.
4. Revisa los registros de PostgreSQL si falla.

### `mvn` no se reconoce

Comprueba:

```powershell
mvn -version
```

Si falla, revisa `MAVEN_HOME` y `Path`, o utiliza:

```powershell
.\mvnw.cmd spring-boot:run
```

### `psql` no se reconoce

Utiliza la ruta completa del ejecutable:

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -U pedidos_user -d pedidos360 -W
```

### Error de conexión a PostgreSQL

Comprueba:

- Que PostgreSQL esté ejecutándose.
- Que el puerto sea `5432`.
- Que la base de datos sea `pedidos360`.
- Que el usuario sea `pedidos_user`.
- Que la contraseña coincida con `application.properties`.
- Que el servidor sea `localhost`.

---

## 13. Notas de seguridad

- No publiques contraseñas en GitHub.
- No compartas contraseñas generadas por Spring Security.
- No uses `ddl-auto=update` como estrategia de migración en producción.
- La autenticación Basic Auth actual es para desarrollo.
- La versión final deberá integrar Microsoft Entra ID, JWT y una configuración de seguridad apropiada para producción.
