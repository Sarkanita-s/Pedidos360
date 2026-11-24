# Pedidos360 — Plataforma Integral de Gestión de Pedidos

Plataforma empresarial de comercio y gestión de pedidos basada en arquitectura de microservicios distribuida con **Spring Boot**, frontend reactivo en **Angular**, autenticación y autorización mediante **Microsoft Entra ID (IDaaS + JWT)**, persistencia en **PostgreSQL**, y mensajería orientada a eventos con **RabbitMQ (Clúster de 2 nodos)** y **Apache Kafka (Clúster de 3 Brokers + Ensamble de 3 nodos ZooKeeper con Factor de Replicación 3)**.

---

## 1. Arquitectura General del Sistema

```
                             ┌─────────────────────────┐
                             │   Cliente / Navegador   │
                             │ (Angular 21 - Port 4200)│
                             └────────────┬────────────┘
                                          │ MSAL / Bearer JWT
                                          ▼
                             ┌─────────────────────────┐
                             │  Microsoft Entra ID     │
                             │ (IDaaS / OIDC / OAuth2) │
                             └────────────┬────────────┘
                                          │
                                          ▼
                             ┌─────────────────────────┐
                             │  Spring Boot Backend    │
                             │     (Puerto 8080)       │
                             └──────┬────────────┬─────┘
                                    │            │
            ┌───────────────────────┘            └─────────────────────────┐
            ▼                                                              ▼
┌───────────────────────┐                                      ┌────────────────────────┐
│  RabbitMQ Clúster     │                                      │  Apache Kafka Clúster  │
│  (rabbit1: 5672/15672)│                                      │  (3 Brokers: 9092-9094)│
│  (rabbit2: 5673/15673)│                                      │  (3 ZK: 2181-2183)     │
├───────────────────────┤                                      ├────────────────────────┤
│ • pedidos.direct.ex   │                                      │ • pedidos.order.events │
│ • pedidos.topic.ex    │                                      │   (3 part, RF=3, DLT)  │
│ • pedidos.dlx.ex      │                                      │ • pedidos.audit.events │
│ • 3 Queues + 3 DLQs   │                                      │   (3 part, RF=3, DLT)  │
└───────────────────────┘                                      │ • Kafka UI (Port 8085) │
                                                               └────────────────────────┘
```

---

## 2. Mapa de Servicios y Puertos

| Servicio / Componente | Contenedor / Host | Puerto Interno | Puerto Host | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| **Frontend Angular** | Host local | 4200 | `4200` | Interfaz SPA Angular con Microsoft Entra ID |
| **Backend Spring Boot** | Host local | 8080 | `8080` | API REST, Seguridad JWT, JPA, RabbitMQ y Kafka |
| **PostgreSQL** | Local / Cloud | 5432 | `5432` | Base de datos relacional `pedidos360` |
| **RabbitMQ Nodo 1** | `pedidos360-rabbit1` | 5672 / 15672 | `5672` / `15672` | Broker AMQP principal y panel de gestión |
| **RabbitMQ Nodo 2** | `pedidos360-rabbit2` | 5672 / 15672 | `5673` / `15673` | Segundo nodo en clúster RabbitMQ |
| **ZooKeeper Nodo 1** | `pedidos360-zookeeper-1` | 2181 | `2181` | Ensamble ZK nodo 1 |
| **ZooKeeper Nodo 2** | `pedidos360-zookeeper-2` | 2181 | `2182` | Ensamble ZK nodo 2 |
| **ZooKeeper Nodo 3** | `pedidos360-zookeeper-3` | 2181 | `2183` | Ensamble ZK nodo 3 |
| **Kafka Broker 1** | `pedidos360-kafka-1` | 29092 | `9092` | Broker Kafka 1 (RF=3) |
| **Kafka Broker 2** | `pedidos360-kafka-2` | 29093 | `9093` | Broker Kafka 2 (RF=3) |
| **Kafka Broker 3** | `pedidos360-kafka-3` | 29094 | `9094` | Broker Kafka 3 (RF=3) |
| **Kafka UI** | `pedidos360-kafka-ui` | 8080 | `8085` | Panel de observación y administración Kafka |

---

## 3. Requisitos Previos

- **Docker y Docker Compose** (versión v2 o superior).
- **Java OpenJDK 25** (o JDK 21+ compatible).
- **Node.js** v20+ y **npm** v10+.
- **PostgreSQL** instalado y ejecutándose en el puerto 5432 con base de datos `pedidos360`.

---

## 4. Puesta en Marcha Rápida

### Paso 1: Levantar Infraestructura Completa (RabbitMQ + ZooKeeper + Kafka + Kafka UI)

```bash
# En la raíz del proyecto Pedidos360:
docker compose up -d
```

Verificar que todos los contenedores se encuentren activos y saludables:
```bash
docker compose ps
```

Acceder a las interfaces gráficas:
- **RabbitMQ Management**: [http://localhost:15672](http://localhost:15672) (Usuario: `pedidos_admin` / Password: `Pedidos360_Rabbit2026!`)
- **Kafka UI**: [http://localhost:8085](http://localhost:8085)

### Paso 2: Ejecutar Backend Spring Boot

```bash
cd backend
./mvnw spring-boot:run
```
La API queda disponible en `http://localhost:8080`.

### Paso 3: Ejecutar Frontend Angular

```bash
cd frontend
npm start
```
La aplicación web queda disponible en `http://localhost:4200`.

---

## 5. Arquitectura de Mensajería y Eventos

### 5.1. Apache Kafka (EP5 / EP6)
- **Topología**: 3 brokers de Kafka (`kafka-1`, `kafka-2`, `kafka-3`) coordinados por un ensamble de 3 nodos ZooKeeper (`zookeeper-1`, `zookeeper-2`, `zookeeper-3`).
- **Tópicos Evaluados**:
  1. `pedidos.order.events`: Particiones: 3, Factor de Replicación: 3, Mínimo ISR: 2, Retención: 7 días (`retention.ms=604800000`). Eventos del ciclo de vida del pedido particionados por `orderId`.
  2. `pedidos.audit.events`: Particiones: 3, Factor de Replicación: 3, Mínimo ISR: 2, Retención: 7 días. Registro inmutable de auditoría y trazabilidad.
  3. `pedidos.order.events.DLT` y `pedidos.audit.events.DLT`: Dead Letter Topics para captura de mensajes fallidos con factor de replicación 3.
- **Consumer Groups**:
  - `pedidos360-order-group`: Procesamiento reactivo de órdenes.
  - `pedidos360-audit-group`: Registro de auditoría independiente.
  - `pedidos360-dlt-group`: Auditoría y alertas sobre eventos en cola de fallos.
- **Manejo de Errores**: `DefaultErrorHandler` con reintentos `FixedBackOff` (2 reintentos con intervalo de 1s) y desvío automático a DLT mediante `DeadLetterPublishingRecoverer`.

### 5.2. RabbitMQ (EP3 / EP4)
- **Topología**: Clúster de 2 nodos con cookie Erlang compartida y auto-clustering.
- **Exchanges**:
  - `pedidos.direct.exchange` (Direct)
  - `pedidos.topic.exchange` (Topic)
  - `pedidos.dlx.exchange` (Dead Letter Exchange)
- **Colas y DLQ**:
  - `pedidos.created.queue` -> DLQ: `pedidos.created.dlq`
  - `pedidos.status.queue` -> DLQ: `pedidos.status.dlq`
  - `pedidos.notification.queue` -> DLQ: `pedidos.notification.dlq`

---

## 6. Endpoints de Administración y Pruebas

### 6.1. Administración Kafka (`/api/admin/kafka`)
- `GET /api/admin/kafka/status`: Inspecciona estado de los brokers, tópicos, particiones, réplicas y consumer groups.
- `POST /api/admin/kafka/simulate`: Simula la publicación de eventos hacia Kafka con opción `forceError=true` para evidenciar en vivo la redirección a Dead Letter Topic (DLT).

### 6.2. Administración RabbitMQ (`/api/admin/queues`)
- `GET /api/admin/queues/status`: Muestra número de mensajes en colas principales y DLQs.
- `POST /api/admin/queues/simulate`: Simula flujo AMQP hacia colas y DLQ forzada.
- `POST /api/admin/queues/purge/{queueName}`: Purga mensajes acumulados.

---

## 7. Pruebas Automatizadas

Para ejecutar la suite de pruebas unitarias y de integración de mensajería:

```bash
cd backend
./mvnw test
```
Resultados esperados: 5 pruebas exitosas (`KafkaAndRabbitEventTests` y `BackendApplicationTests`), 0 fallos, 0 errores.
