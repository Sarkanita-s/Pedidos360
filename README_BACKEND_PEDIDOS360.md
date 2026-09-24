# Pedidos360 --- Guía del backend

## 1. Descripción general

Pedidos360 es una aplicación backend desarrollada con **Spring Boot**,
**Spring Data JPA** y **PostgreSQL**. Gestiona un catálogo de productos
y pedidos, incluyendo estados de pedido, validación de transiciones y
actualización automática del stock al aceptar un pedido.

Actualmente, la API utiliza autenticación **Basic Auth** de Spring
Security para desarrollo local. Esta autenticación deberá reemplazarse o
integrarse con Microsoft Entra ID y JWT en una etapa posterior.

------------------------------------------------------------------------

## 2. Arquitectura del proyecto

El backend utiliza una arquitectura por capas:

-   **Controller:** recibe las solicitudes HTTP y devuelve respuestas al
    cliente.
-   **Service:** contiene la lógica de negocio, como las transiciones de
    estado y el descuento de stock.
-   **Repository:** comunica la aplicación con la base de datos mediante
    Spring Data JPA.
-   **Model:** contiene las entidades y enumeraciones del dominio.
-   **Exception:** contiene las excepciones personalizadas y el
    manejador global de errores.
-   **Config:** contiene la configuración de seguridad de Spring
    Security.

### Flujo general de una solicitud

``` text
Cliente / Postman
        |
        v
Controller
        |
        v
Service
        |
        v
Repository
        |
        v
PostgreSQL
```

------------------------------------------------------------------------

## 3. Requisitos previos

Antes de levantar el backend, se debe contar con:

-   Java instalado.
-   Maven instalado.
-   PostgreSQL instalado y ejecutándose.
-   Base de datos `pedidos360` creada.
-   Usuario de base de datos configurado.
-   Archivo `application.properties` con los datos correctos de
    conexión.

------------------------------------------------------------------------

## 4. Cómo levantar el backend

### 4.1. Abrir una terminal

Ingresar a la carpeta del backend:

``` bash
cd ~/Pedidos360/backend
```

### 4.2. Ejecutar la aplicación

``` bash
mvn spring-boot:run
```

Cuando el backend se inicia correctamente, la terminal muestra un
mensaje similar a:

``` text
Started BackendApplication
```

La API queda disponible en:

``` text
http://localhost:8080
```

### 4.3. Detener el backend

En la terminal donde se está ejecutando la aplicación, presionar:

``` text
Ctrl + C
```

### 4.4. Contraseña de desarrollo

Spring Security genera una contraseña temporal para el usuario:

``` text
user
```

La contraseña aparece en la terminal al iniciar el backend, en una línea
similar a:

``` text
Using generated security password: ...
```

No se debe publicar esta contraseña en repositorios ni utilizarla como
mecanismo definitivo en producción.

------------------------------------------------------------------------

## 5. Configuración de autenticación en Postman

Las solicitudes protegidas utilizan autenticación Basic Auth.

En Postman:

1.  Abrir la solicitud.
2.  Ir a la pestaña **Authorization**.
3.  Seleccionar **Basic Auth**.
4.  En `Username`, escribir:

``` text
user
```

5.  En `Password`, ingresar la contraseña generada por Spring Security
    al levantar el backend.

------------------------------------------------------------------------

## 6. Organización recomendada en Postman

Se recomienda crear una colección llamada:

``` text
Pedidos360
```

Dentro de ella, crear dos carpetas:

``` text
Pedidos360
├── Catálogo
└── Pedidos
```

------------------------------------------------------------------------

## 7. Endpoints del catálogo

La ruta base del catálogo es:

``` text
/api/catalog/products
```

### 7.1. Listar productos

**Método:** `GET`

``` http
GET http://localhost:8080/api/catalog/products
```

Devuelve todos los productos registrados.

### 7.2. Consultar un producto

**Método:** `GET`

``` http
GET http://localhost:8080/api/catalog/products/{id}
```

Ejemplo:

``` http
GET http://localhost:8080/api/catalog/products/2
```

### 7.3. Crear un producto

**Método:** `POST`

``` http
POST http://localhost:8080/api/catalog/products
```

En `Body > raw > JSON`:

``` json
{
  "name": "Pizza familiar",
  "description": "Pizza familiar con queso y pepperoni",
  "price": 12990.00,
  "stock": 25
}
```

### 7.4. Actualizar un producto

**Método:** `PUT`

``` http
PUT http://localhost:8080/api/catalog/products/{id}
```

Ejemplo de body:

``` json
{
  "name": "Pizza familiar actualizada",
  "description": "Pizza familiar con queso extra",
  "price": 13990.00,
  "stock": 20
}
```

### 7.5. Eliminar un producto

**Método:** `DELETE`

``` http
DELETE http://localhost:8080/api/catalog/products/{id}
```

Ejemplo:

``` http
DELETE http://localhost:8080/api/catalog/products/2
```

------------------------------------------------------------------------

## 8. Endpoints de pedidos

La ruta base de pedidos es:

``` text
/api/orders
```

### 8.1. Listar pedidos

**Método:** `GET`

``` http
GET http://localhost:8080/api/orders
```

### 8.2. Consultar un pedido

**Método:** `GET`

``` http
GET http://localhost:8080/api/orders/{id}
```

Ejemplo:

``` http
GET http://localhost:8080/api/orders/4
```

### 8.3. Crear un pedido

**Método:** `POST`

``` http
POST http://localhost:8080/api/orders
```

En `Body > raw > JSON`:

``` json
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

Los pedidos nuevos se crean en estado:

``` text
PENDIENTE
```

### 8.4. Cambiar el estado de un pedido

**Método:** `PUT`

``` http
PUT http://localhost:8080/api/orders/{id}/status
```

Ejemplo:

``` http
PUT http://localhost:8080/api/orders/4/status
```

En `Body > raw > JSON`, escribir el estado como texto:

``` json
"ACEPTADO"
```

Estados disponibles:

``` text
PENDIENTE
ACEPTADO
PREPARANDO
DESPACHADO
CANCELADO
```

------------------------------------------------------------------------

## 9. Flujo de estados permitido

El flujo principal de un pedido es:

``` text
PENDIENTE → ACEPTADO → PREPARANDO → DESPACHADO
     |
     └──────────────→ CANCELADO
```

Reglas implementadas:

-   `PENDIENTE` puede pasar a `ACEPTADO` o `CANCELADO`.
-   `ACEPTADO` puede pasar a `PREPARANDO` o `CANCELADO`.
-   `PREPARANDO` puede pasar a `DESPACHADO` o `CANCELADO`.
-   `DESPACHADO` es un estado final.
-   `CANCELADO` es un estado final.
-   No se permite pasar directamente de `PENDIENTE` a `PREPARANDO`.
-   No se permite cambiar un pedido que ya está en un estado final.

Si se intenta una transición inválida, la API devuelve un error
`409 Conflict`.

Ejemplo de respuesta:

``` json
{
  "error": "Transición no permitida: PENDIENTE -> PREPARANDO"
}
```

------------------------------------------------------------------------

## 10. Descuento de stock

Cuando un pedido pasa de `PENDIENTE` a `ACEPTADO`:

1.  Se busca el producto en la base de datos.
2.  Se comprueba que exista stock suficiente.
3.  Se descuenta la cantidad solicitada.
4.  Se guarda el stock actualizado.
5.  Se cambia el estado del pedido a `ACEPTADO`.

El stock no se descuenta nuevamente si se vuelve a enviar el estado
`ACEPTADO` para un pedido que ya estaba aceptado.

### Ejemplo

Stock inicial:

``` text
25
```

Cantidad solicitada:

``` text
2
```

Stock final:

``` text
23
```

Si no existe stock suficiente, la API devuelve `409 Conflict`:

``` json
{
  "error": "Stock insuficiente para el producto: Pizza familiar"
}
```

------------------------------------------------------------------------

## 11. Resumen de archivos
  ------------------------------------------------------------------------
  Archivo                              Descripción
  ------------------------------------ -----------------------------------
  `BackendApplication.java`            Clase principal que inicia la
                                       aplicación Spring Boot.

  `Product.java`                       Entidad que representa un producto,
                                       su nombre, descripción, precio y
                                       stock.

  `Order.java`                         Entidad que representa un pedido,
                                       su cliente, estado, total, fecha y
                                       productos asociados.

  `OrderItem.java`                     Entidad que representa un producto
                                       incluido dentro de un pedido, su
                                       cantidad y precio unitario.

  `OrderStatus.java`                   Enumeración con los estados
                                       posibles de un pedido.

  `ProductRepository.java`             Repositorio JPA para consultar,
                                       guardar, actualizar y eliminar
                                       productos.

  `OrderRepository.java`               Repositorio JPA para consultar,
                                       guardar, actualizar y eliminar
                                       pedidos.

  `ProductService.java`                Contiene la lógica de negocio
                                       relacionada con las operaciones del
                                       catálogo.

  `OrderService.java`                  Contiene la lógica de pedidos,
                                       transiciones de estado y descuento
                                       de stock.

  `ProductController.java`             Expone los endpoints HTTP para
                                       administrar productos.

  `OrderController.java`               Expone los endpoints HTTP para
                                       crear, consultar y actualizar
                                       pedidos.

  `InvalidOrderStatusException.java`   Excepción utilizada cuando se
                                       intenta realizar una transición de
                                       estado inválida.

  `InsufficientStockException.java`    Excepción utilizada cuando la
                                       cantidad solicitada supera el stock
                                       disponible.

  `GlobalExceptionHandler.java`        Convierte excepciones específicas
                                       en respuestas HTTP con mensajes
                                       JSON claros.

  `SecurityConfig.java`                Configura la autenticación Basic
                                       Auth y desactiva CSRF para las
                                       pruebas actuales.

  `application.properties`             Contiene la configuración de la
                                       aplicación, conexión a PostgreSQL y
                                       puerto del servidor.

  `pom.xml`                            Define las dependencias, plugins y
                                       configuración de compilación de
                                       Maven.
  ------------------------------------------------------------------------

------------------------------------------------------------------------

## 12. Errores HTTP frecuentes

  -----------------------------------------------------------------------
  Código                              Significado
  ----------------------------------- -----------------------------------
  `200 OK`                            La solicitud se ejecutó
                                      correctamente.

  `201 Created`                       Se creó un recurso correctamente.

  `204 No Content`                    La operación fue exitosa y no
                                      devuelve contenido.

  `401 Unauthorized`                  Faltan credenciales o son
                                      incorrectas.

  `404 Not Found`                     El recurso solicitado no existe.

  `409 Conflict`                      La operación no es válida por una
                                      regla de negocio.

  `500 Internal Server Error`         Ocurrió un error no controlado en
                                      el servidor.
  -----------------------------------------------------------------------

------------------------------------------------------------------------
