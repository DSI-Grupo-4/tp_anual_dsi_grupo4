-- =====================================================================
-- DonaTrack - Servicio de Notificaciones - esquema físico (MySQL 8)
-- Se monta en docker-entrypoint-initdb.d/: corre solo cuando el volumen
-- de MySQL se crea desde cero. ddl-auto=validate: Hibernate no lo modifica.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS notificaciones;
USE notificaciones;

CREATE TABLE notificacion (
  id              CHAR(36)      NOT NULL,
  mensaje         VARCHAR(2000) NOT NULL,
  medio           ENUM('WHATSAPP','EMAIL','SMS') NOT NULL,
  contacto        VARCHAR(150)  NOT NULL,
  estado          ENUM('PENDIENTE','COMPLETADA','FALLIDA') NOT NULL DEFAULT 'PENDIENTE',
  servicio_origen VARCHAR(50)   NOT NULL,
  tipo_evento     VARCHAR(50)   NOT NULL,
  evento_id       VARCHAR(200)  NOT NULL,
  fecha_creacion  DATETIME      NOT NULL,
  fecha_envio     DATETIME      NULL,
  intentos        INT           NOT NULL DEFAULT 0,
  detalle_error   VARCHAR(500)  NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_notificacion_origen_evento (servicio_origen, evento_id),
  KEY idx_notificacion_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;