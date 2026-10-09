-- =====================================================================
-- DonaTrack - Servicio de Logística - esquema físico (MySQL 8)
-- Generado a partir de diagramas/der/logistica.txt.
-- Se monta en docker-entrypoint-initdb.d/: solo corre una vez, cuando el
-- volumen de MySQL se crea desde cero (ver docker-compose.integration.yml).
-- spring.jpa.hibernate.ddl-auto=validate: Hibernate valida que las
-- entidades coincidan con este esquema, no lo crea ni lo modifica.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS logistica;
USE logistica;

CREATE TABLE camion (
  id_camion            INT NOT NULL AUTO_INCREMENT,
  patente              VARCHAR(10) NOT NULL,
  capacidad_volumen_m3 INT NOT NULL,
  altura_m             INT NOT NULL,
  capacidad_carga_kg   INT NOT NULL,
  estado_camion        ENUM('DISPONIBLE','ASIGNADO','EN_RUTA','FUERA_DE_SERVICIO') NOT NULL DEFAULT 'DISPONIBLE',
  PRIMARY KEY (id_camion),
  UNIQUE KEY uk_camion_patente (patente),
  KEY idx_camion_estado_camion (estado_camion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE chofer (
  id_chofer  INT NOT NULL AUTO_INCREMENT,
  nombre     VARCHAR(100) NOT NULL,
  dni        INT NOT NULL,
  habilitado BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_chofer),
  UNIQUE KEY uk_chofer_dni (dni)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ruta (
  id_ruta     INT NOT NULL AUTO_INCREMENT,
  id_camion   INT NOT NULL,
  id_chofer   INT NULL,
  fecha       DATE NOT NULL,
  estado_ruta ENUM('PLANIFICADA','INICIADA','FINALIZADA','CANCELADA') NOT NULL DEFAULT 'PLANIFICADA',
  PRIMARY KEY (id_ruta),
  KEY idx_ruta_fecha_estado_ruta (fecha, estado_ruta),
  CONSTRAINT fk_ruta_camion FOREIGN KEY (id_camion) REFERENCES camion (id_camion),
  CONSTRAINT fk_ruta_chofer FOREIGN KEY (id_chofer) REFERENCES chofer (id_chofer)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE parada (
  id_parada               INT NOT NULL AUTO_INCREMENT,
  id_ruta                 INT NOT NULL,
  numero_parada           INT NOT NULL,
  id_entidad_beneficiaria INT NOT NULL,
  direccion_calle         VARCHAR(120) NOT NULL,
  direccion_numero        VARCHAR(10) NOT NULL,
  direccion_ciudad        VARCHAR(80) NOT NULL,
  direccion_provincia     VARCHAR(80) NOT NULL,
  PRIMARY KEY (id_parada),
  UNIQUE KEY uk_parada_ruta_numero (id_ruta, numero_parada),
  CONSTRAINT fk_parada_ruta FOREIGN KEY (id_ruta) REFERENCES ruta (id_ruta) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE entrega (
  id_entrega                       INT NOT NULL AUTO_INCREMENT,
  id_donacion_asociada              INT NOT NULL,
  id_entidad_beneficiaria_asociada  INT NOT NULL,
  direccion_destino_calle           VARCHAR(120) NOT NULL,
  direccion_destino_numero          VARCHAR(10) NOT NULL,
  direccion_destino_ciudad          VARCHAR(80) NOT NULL,
  direccion_destino_provincia       VARCHAR(80) NOT NULL,
  estado_entrega                    ENUM('PENDIENTE','ASIGNADA_A_RUTA','EN_TRASLADO','ENTREGADA','NO_RECIBIDA','FALLIDA','REPLANIFICABLE') NOT NULL DEFAULT 'PENDIENTE',
  fecha                             DATE NOT NULL,
  fecha_hora_entrega                DATETIME NULL,
  seguimiento_url                   VARCHAR(500) NULL,
  peso_kg                           INT NOT NULL,
  volumen_m3                        INT NOT NULL,
  altura_m                          INT NOT NULL,
  justificacion_fallo               VARCHAR(500) NULL,
  id_parada                         INT NULL,
  id_camion                         INT NULL,
  PRIMARY KEY (id_entrega),
  UNIQUE KEY uk_entrega_donacion_asociada (id_donacion_asociada),
  KEY idx_entrega_estado_entrega (estado_entrega),
  KEY idx_entrega_fecha (fecha),
  CONSTRAINT fk_entrega_parada FOREIGN KEY (id_parada) REFERENCES parada (id_parada),
  CONSTRAINT fk_entrega_camion FOREIGN KEY (id_camion) REFERENCES camion (id_camion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE foto_entrega (
  id_foto    INT NOT NULL AUTO_INCREMENT,
  id_entrega INT NOT NULL,
  url        VARCHAR(500) NOT NULL,
  fecha      DATE NOT NULL,
  PRIMARY KEY (id_foto),
  CONSTRAINT fk_foto_entrega_entrega FOREIGN KEY (id_entrega) REFERENCES entrega (id_entrega) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE evento_logistico (
  id_evento        CHAR(36) NOT NULL,
  tipo_evento      ENUM('RUTA_PLANIFICADA','RUTA_INICIADA','ENTREGA_EN_TRASLADO','ENTREGA_CONFIRMADA','ENTREGA_NO_RECIBIDA','ENTREGA_FALLIDA','ENTREGA_REPLANIFICADA') NOT NULL,
  publicado        BOOLEAN NOT NULL DEFAULT FALSE,
  id_entrega       INT NOT NULL,
  fecha_generacion DATETIME NOT NULL,
  PRIMARY KEY (id_evento),
  KEY idx_evento_logistico_publicado_fecha (publicado, fecha_generacion),
  CONSTRAINT fk_evento_logistico_entrega FOREIGN KEY (id_entrega) REFERENCES entrega (id_entrega)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
