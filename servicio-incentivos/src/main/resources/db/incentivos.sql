-- =====================================================================
-- DonaTrack - Servicio de Incentivos - esquema físico (MySQL 8)
-- Generado a partir de diagramas/der/incentivos.txt (con el agregado real
-- de datos_donacion.donacion_id, que faltaba en el DER -- ver D-028).
-- Se monta en docker-entrypoint-initdb.d/: solo corre una vez, cuando el
-- volumen de MySQL se crea desde cero (ver docker-compose.integration.yml).
-- spring.jpa.hibernate.ddl-auto=validate: Hibernate valida que las
-- entidades coincidan con este esquema, no lo crea ni lo modifica.
--
-- progreso_asociado <-> progreso_mision (vía progreso_categoria) es una
-- referencia circular real (ProgresoAsociado.misionActual). Se resuelve
-- creando progreso_asociado sin esa FK y agregándola recién al final,
-- cuando progreso_mision ya existe.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS incentivos;
USE incentivos;

-- ---------------------------------------------------------------------
-- Catálogo (GestorMisiones.catalogoCategorias)
-- ---------------------------------------------------------------------

CREATE TABLE categoria (
  id_categoria INT NOT NULL AUTO_INCREMENT,
  nombre       VARCHAR(50) NOT NULL,
  orden        INT NOT NULL,
  PRIMARY KEY (id_categoria),
  UNIQUE KEY uk_categoria_nombre (nombre),
  UNIQUE KEY uk_categoria_orden (orden)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE insignia (
  id_insignia INT NOT NULL AUTO_INCREMENT,
  nombre      VARCHAR(100) NOT NULL,
  imagen_url  VARCHAR(500) NOT NULL,
  PRIMARY KEY (id_insignia),
  UNIQUE KEY uk_insignia_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE mision (
  id_mision     INT NOT NULL AUTO_INCREMENT,
  id_categoria  INT NOT NULL,
  orden         INT NOT NULL,
  nombre_mision VARCHAR(100) NOT NULL,
  id_insignia   INT NOT NULL,
  PRIMARY KEY (id_mision),
  UNIQUE KEY uk_mision_nombre (nombre_mision),
  UNIQUE KEY uk_mision_insignia (id_insignia),
  UNIQUE KEY uk_mision_categoria_orden (id_categoria, orden),
  CONSTRAINT fk_mision_categoria FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria),
  CONSTRAINT fk_mision_insignia FOREIGN KEY (id_insignia) REFERENCES insignia (id_insignia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Herencia JOINED: una fila en mision + una fila en la subtabla correspondiente.
CREATE TABLE mision_racha (
  id_mision        INT NOT NULL,
  meses_requeridos INT NOT NULL,
  PRIMARY KEY (id_mision),
  CONSTRAINT fk_mision_racha_mision FOREIGN KEY (id_mision) REFERENCES mision (id_mision) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE mision_completitud (
  id_mision             INT NOT NULL,
  categorias_requeridas INT NOT NULL,
  PRIMARY KEY (id_mision),
  CONSTRAINT fk_mision_completitud_mision FOREIGN KEY (id_mision) REFERENCES mision (id_mision) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE mision_habil_donador (
  id_mision                 INT NOT NULL,
  cantidad_bienes_requerida INT NOT NULL,
  PRIMARY KEY (id_mision),
  CONSTRAINT fk_mision_habil_donador_mision FOREIGN KEY (id_mision) REFERENCES mision (id_mision) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE mision_donaciones_exitosas (
  id_mision             INT NOT NULL,
  donaciones_requeridas INT NOT NULL,
  PRIMARY KEY (id_mision),
  CONSTRAINT fk_mision_donaciones_exitosas_mision FOREIGN KEY (id_mision) REFERENCES mision (id_mision) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- Progreso (referencia circular progreso_asociado <-> progreso_mision,
-- resuelta al final de este script -- ver cabecera)
-- ---------------------------------------------------------------------

CREATE TABLE progreso_asociado (
  id_progreso_asociado      INT NOT NULL AUTO_INCREMENT,
  indice_mision_actual      INT NOT NULL DEFAULT 0,
  id_progreso_mision_actual INT NULL,
  PRIMARY KEY (id_progreso_asociado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE progreso_categoria (
  id_progreso_categoria INT NOT NULL AUTO_INCREMENT,
  id_progreso_asociado  INT NOT NULL,
  orden                 INT NOT NULL,
  nombre                VARCHAR(50) NOT NULL,
  PRIMARY KEY (id_progreso_categoria),
  UNIQUE KEY uk_progreso_categoria_asociado_orden (id_progreso_asociado, orden),
  CONSTRAINT fk_progreso_categoria_asociado FOREIGN KEY (id_progreso_asociado) REFERENCES progreso_asociado (id_progreso_asociado) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE progreso_insignia (
  id_progreso_insignia INT NOT NULL AUTO_INCREMENT,
  id_insignia          INT NOT NULL,
  fecha_obtencion      DATE NOT NULL,
  visible              BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_progreso_insignia),
  CONSTRAINT fk_progreso_insignia_insignia FOREIGN KEY (id_insignia) REFERENCES insignia (id_insignia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE progreso_mision (
  id_progreso_mision    INT NOT NULL AUTO_INCREMENT,
  id_progreso_categoria INT NOT NULL,
  orden                 INT NOT NULL,
  id_mision             INT NOT NULL,
  id_progreso_insignia  INT NULL,
  PRIMARY KEY (id_progreso_mision),
  UNIQUE KEY uk_progreso_mision_insignia (id_progreso_insignia),
  UNIQUE KEY uk_progreso_mision_categoria_orden (id_progreso_categoria, orden),
  CONSTRAINT fk_progreso_mision_categoria FOREIGN KEY (id_progreso_categoria) REFERENCES progreso_categoria (id_progreso_categoria) ON DELETE CASCADE,
  CONSTRAINT fk_progreso_mision_mision FOREIGN KEY (id_mision) REFERENCES mision (id_mision),
  CONSTRAINT fk_progreso_mision_insignia FOREIGN KEY (id_progreso_insignia) REFERENCES progreso_insignia (id_progreso_insignia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Herencia JOINED: una fila en progreso_mision + una fila en la subtabla correspondiente.
CREATE TABLE progreso_racha (
  id_progreso_mision          INT NOT NULL,
  meses_consecutivos_actuales INT NOT NULL DEFAULT 0,
  ultima_donacion_registrada  DATE NULL,
  PRIMARY KEY (id_progreso_mision),
  CONSTRAINT fk_progreso_racha_progreso_mision FOREIGN KEY (id_progreso_mision) REFERENCES progreso_mision (id_progreso_mision) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE progreso_completitud (
  id_progreso_mision INT NOT NULL,
  PRIMARY KEY (id_progreso_mision),
  CONSTRAINT fk_progreso_completitud_progreso_mision FOREIGN KEY (id_progreso_mision) REFERENCES progreso_mision (id_progreso_mision) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE progreso_completitud_categoria_cubierta (
  id_progreso_mision INT NOT NULL,
  categoria_bien     VARCHAR(50) NOT NULL,
  PRIMARY KEY (id_progreso_mision, categoria_bien),
  CONSTRAINT fk_progreso_completitud_cat_cubierta FOREIGN KEY (id_progreso_mision) REFERENCES progreso_completitud (id_progreso_mision) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE progreso_habil_donador (
  id_progreso_mision        INT NOT NULL,
  mejor_donacion_registrada DECIMAL(12,3) NOT NULL DEFAULT 0,
  PRIMARY KEY (id_progreso_mision),
  CONSTRAINT fk_progreso_habil_donador_progreso_mision FOREIGN KEY (id_progreso_mision) REFERENCES progreso_mision (id_progreso_mision) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE progreso_donaciones_exitosas (
  id_progreso_mision           INT NOT NULL,
  donaciones_exitosas_actuales INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id_progreso_mision),
  CONSTRAINT fk_progreso_donaciones_exitosas_progreso_mision FOREIGN KEY (id_progreso_mision) REFERENCES progreso_mision (id_progreso_mision) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Cierra la referencia circular: progreso_asociado.misionActual -> progreso_mision.
ALTER TABLE progreso_asociado
  ADD CONSTRAINT fk_progreso_asociado_mision_actual FOREIGN KEY (id_progreso_mision_actual) REFERENCES progreso_mision (id_progreso_mision);

-- ---------------------------------------------------------------------
-- Donante y su actividad
-- ---------------------------------------------------------------------

CREATE TABLE beneficiario (
  id_beneficiario BIGINT NOT NULL,
  nombre          VARCHAR(150) NULL,
  PRIMARY KEY (id_beneficiario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE donante (
  id_donante                  BIGINT NOT NULL,
  nombre                      VARCHAR(100) NULL,
  medio_contacto_preferido    VARCHAR(20) NULL,
  contacto_preferido          VARCHAR(150) NULL,
  solicitudes_donacion_hechas INT NOT NULL DEFAULT 0,
  id_progreso_asociado        INT NOT NULL,
  PRIMARY KEY (id_donante),
  UNIQUE KEY uk_donante_progreso_asociado (id_progreso_asociado),
  CONSTRAINT fk_donante_progreso_asociado FOREIGN KEY (id_progreso_asociado) REFERENCES progreso_asociado (id_progreso_asociado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE donante_beneficiario (
  id_donante      BIGINT NOT NULL,
  id_beneficiario BIGINT NOT NULL,
  PRIMARY KEY (id_donante, id_beneficiario),
  CONSTRAINT fk_donante_beneficiario_donante FOREIGN KEY (id_donante) REFERENCES donante (id_donante) ON DELETE CASCADE,
  CONSTRAINT fk_donante_beneficiario_beneficiario FOREIGN KEY (id_beneficiario) REFERENCES beneficiario (id_beneficiario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE datos_donacion (
  id_datos_donacion INT NOT NULL AUTO_INCREMENT,
  -- No está en el DER original: el código la necesita para no duplicar
  -- cantidades si Donaciones reenvía el mismo evento (D-028).
  donacion_id       BIGINT NULL,
  id_donante        BIGINT NOT NULL,
  fecha             DATE NOT NULL,
  categoria_bien    VARCHAR(50) NULL,
  cantidad_bienes   DECIMAL(12,3) NOT NULL,
  donacion_exitosa  BOOLEAN NOT NULL,
  id_beneficiario   BIGINT NULL,
  PRIMARY KEY (id_datos_donacion),
  KEY idx_datos_donacion_donante_fecha (id_donante, fecha),
  CONSTRAINT fk_datos_donacion_donante FOREIGN KEY (id_donante) REFERENCES donante (id_donante) ON DELETE CASCADE,
  CONSTRAINT fk_datos_donacion_beneficiario FOREIGN KEY (id_beneficiario) REFERENCES beneficiario (id_beneficiario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- Ranking (HistorialRanking)
-- ---------------------------------------------------------------------

CREATE TABLE ranking (
  id_ranking    INT NOT NULL AUTO_INCREMENT,
  fecha_emision DATE NOT NULL,
  PRIMARY KEY (id_ranking)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- id_ranking NULL = contador del mes en curso (HistorialRanking.donantesMisionesMensuales);
-- con valor = ya forma parte de un Ranking.topDonantes cerrado.
CREATE TABLE actividad_mensual_donante (
  id_actividad_mensual_donante INT NOT NULL AUTO_INCREMENT,
  id_donante                   BIGINT NOT NULL,
  cantidad                     INT NOT NULL DEFAULT 0,
  id_ranking                   INT NULL,
  posicion                     INT NULL,
  PRIMARY KEY (id_actividad_mensual_donante),
  UNIQUE KEY uk_actividad_mensual_ranking_posicion (id_ranking, posicion),
  CONSTRAINT fk_actividad_mensual_donante FOREIGN KEY (id_donante) REFERENCES donante (id_donante) ON DELETE CASCADE,
  CONSTRAINT fk_actividad_mensual_ranking FOREIGN KEY (id_ranking) REFERENCES ranking (id_ranking)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
