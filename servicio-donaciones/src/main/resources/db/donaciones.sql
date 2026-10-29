-- =====================================================================
-- DonaTrack - Servicio de Donaciones - esquema físico (MySQL 8)
-- Generado a partir de diagramas/der/donaciones.txt (con persona.id
-- agregado -- ver D-030: el DER daba identidad propia a Persona pero el
-- código no la tenía, se agregó para poder mapear la herencia JOINED).
-- Se monta en docker-entrypoint-initdb.d/: solo corre una vez, cuando el
-- volumen de MySQL se crea desde cero (ver docker-compose.integration.yml).
-- spring.jpa.hibernate.ddl-auto=validate: Hibernate valida que las
-- entidades coincidan con este esquema, no lo crea ni lo modifica.
--
-- Entrega 5 (persistencia) -- integridad referencial al borrar, alineada
-- con las reglas de negocio de baja/borrado y con diagramas/der/donaciones.txt:
--   - entidad_beneficiaria.activa / donante.activo: baja lógica. Ninguna de
--     las dos tablas se borra NUNCA físicamente desde la aplicación (ver
--     EntidadBeneficiariaService.eliminar(), GestorDonantes.eliminar()) --
--     por eso las FK que referencian a estas dos tablas NO llevan ON DELETE
--     SET NULL/CASCADE: si alguna vez se intentara un borrado físico real
--     (ej. a mano, fuera de la app), MySQL debe rechazarlo por FK (RESTRICT,
--     el comportamiento por defecto sin cláusula ON DELETE) en vez de
--     desvincular donaciones históricas o borrar necesidades en cascada.
--   - donacion.entidad_beneficiaria_id / necesidad_asignada_id: por lo
--     mismo, SIN ON DELETE (RESTRICT). Nunca se desvincula una donación
--     histórica (ENTREGADA/ENTREGA_FALLIDA/VENCIDA incluidas) solo para
--     permitir el borrado de la necesidad o la entidad que referencia --
--     el borrado de una Necesidad referenciada por necesidadAsignada se
--     bloquea a nivel aplicación (ver NecesidadService.eliminar(), en
--     CUALQUIER estado de la donación), y entidad_beneficiaria nunca se
--     borra físicamente.
--   - necesidad.entidad_beneficiaria_id: por lo mismo, SIN ON DELETE
--     (RESTRICT) -- ya no CASCADE: el borrado en cascada de las
--     necesidades de una entidad solo tenía sentido si la entidad se
--     borraba físicamente, y eso ya no ocurre.
--   - candidata_matchmaking.entidad_beneficiaria_id: se mantiene ON DELETE
--     CASCADE -- es una foto descartable del último matchmaking corrido
--     (se puede re-ejecutar), no historial; en la práctica es inalcanzable
--     igual, porque entidad_beneficiaria nunca se borra.
--   - CHECK de cantidades/posición y de columnas por discriminador:
--     documentan en el esquema invariantes que el dominio ya exige en Java
--     (Necesidad.actualizar, ItemDonado.validar, constructores de
--     NecesidadRecurrente/NecesidadExtraordinaria).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS donaciones;
USE donaciones;

-- ---------------------------------------------------------------------
-- Personas (herencia JOINED)
-- ---------------------------------------------------------------------

CREATE TABLE persona (
  id           BIGINT NOT NULL AUTO_INCREMENT,
  tipo_persona VARCHAR(31) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE persona_humana (
  id        BIGINT NOT NULL,
  nombre    VARCHAR(100) NOT NULL,
  apellido  VARCHAR(100) NOT NULL DEFAULT '',
  edad      INT NULL,
  documento VARCHAR(20) NULL,
  genero    ENUM('MASCULINO','FEMENINO','X') NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_persona_humana_persona FOREIGN KEY (id) REFERENCES persona (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE persona_juridica (
  id                BIGINT NOT NULL,
  razon_social      VARCHAR(150) NOT NULL,
  tipo_organizacion ENUM('GUBERNAMENTAL','ONG','EMPRESA','INSTITUCION') NULL,
  rubro             VARCHAR(100) NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_persona_juridica_persona FOREIGN KEY (id) REFERENCES persona (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE representante (
  persona_juridica_id BIGINT NOT NULL,
  persona_humana_id   BIGINT NOT NULL,
  PRIMARY KEY (persona_juridica_id, persona_humana_id),
  CONSTRAINT fk_representante_juridica FOREIGN KEY (persona_juridica_id) REFERENCES persona_juridica (id) ON DELETE CASCADE,
  CONSTRAINT fk_representante_humana FOREIGN KEY (persona_humana_id) REFERENCES persona_humana (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE medio_contacto (
  id            BIGINT NOT NULL AUTO_INCREMENT,
  persona_id    BIGINT NOT NULL,
  tipo          ENUM('EMAIL','TELEFONO','WHATSAPP') NOT NULL,
  valor         VARCHAR(150) NOT NULL,
  es_preferido  BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id),
  UNIQUE KEY uk_medio_contacto_persona_tipo_valor (persona_id, tipo, valor),
  KEY idx_medio_contacto_tipo_valor (tipo, valor),
  CONSTRAINT fk_medio_contacto_persona FOREIGN KEY (persona_id) REFERENCES persona (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE donante (
  id                           BIGINT NOT NULL AUTO_INCREMENT,
  persona_id                   BIGINT NOT NULL,
  ultima_actividad             DATE NOT NULL,
  notificado_por_inactividad   BOOLEAN NOT NULL DEFAULT FALSE,
  -- Baja lógica -- nunca se borra físicamente (ver GestorDonantes.eliminar()).
  -- Inactivo: no puede registrar nuevas solicitudes/donaciones ni modificar
  -- sus datos; reactivar() es la única modificación permitida.
  activo                       BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  UNIQUE KEY uk_donante_persona (persona_id),
  CONSTRAINT fk_donante_persona FOREIGN KEY (persona_id) REFERENCES persona (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE entidad_beneficiaria (
  id                   BIGINT NOT NULL AUTO_INCREMENT,
  persona_juridica_id  BIGINT NOT NULL,
  descripcion          VARCHAR(255) NULL,
  dir_calle            VARCHAR(150) NULL,
  dir_numero           VARCHAR(20) NULL,
  dir_ciudad           VARCHAR(100) NULL,
  dir_provincia        VARCHAR(100) NULL,
  -- Baja lógica -- nunca se borra físicamente (ver
  -- EntidadBeneficiariaService.eliminar()). Inactiva: no puede iniciar
  -- operaciones nuevas ni modificar sus datos/necesidades, y queda
  -- excluida de nuevas asignaciones y del matchmaking; reactivar() es la
  -- única modificación permitida.
  activa               BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  UNIQUE KEY uk_entidad_beneficiaria_persona_juridica (persona_juridica_id),
  CONSTRAINT fk_entidad_beneficiaria_persona_juridica FOREIGN KEY (persona_juridica_id) REFERENCES persona_juridica (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- Necesidades (herencia SINGLE_TABLE)
-- ---------------------------------------------------------------------

CREATE TABLE necesidad (
  id                      BIGINT NOT NULL AUTO_INCREMENT,
  tipo_necesidad          VARCHAR(31) NOT NULL,
  entidad_beneficiaria_id BIGINT NULL,
  descripcion             VARCHAR(255) NOT NULL,
  subcategoria            ENUM('SILLA','MESA','BANCO','COLCHON','FIDEOS_SECOS','ARROZ','LEGUMBRES_SECAS','ACEITE_VEGETAL','TOMATE','FRUTA','CAMPERA','REMERA','PANTALON','ROPA_INFANTIL','FRAZADA') NOT NULL,
  unidad_medida           ENUM('UNIDAD','KILOGRAMO','CAJA','PAQUETE','LITRO') NOT NULL,
  cantidad_requerida      DECIMAL(12,3) NOT NULL,
  cantidad_recibida       DECIMAL(12,3) NOT NULL DEFAULT 0,
  periodicidad            ENUM('DIARIA','SEMANAL','MENSUAL','ANUAL') NULL,
  inicio_periodo_actual   DATE NULL,
  tipo_extraordinario     ENUM('INUNDACION','SISMO','OLA_POLAR') NULL,
  PRIMARY KEY (id),
  -- Sin ON DELETE: entidad_beneficiaria nunca se borra físicamente (baja
  -- lógica), así que no hace falta -- y si se intentara, debe rechazarse.
  CONSTRAINT fk_necesidad_entidad_beneficiaria FOREIGN KEY (entidad_beneficiaria_id) REFERENCES entidad_beneficiaria (id),
  CONSTRAINT chk_necesidad_cantidad_requerida CHECK (cantidad_requerida > 0),
  CONSTRAINT chk_necesidad_cantidad_recibida CHECK (cantidad_recibida >= 0),
  -- RECURRENTE exige periodicidad y no admite tipo_extraordinario; EXTRAORDINARIA al revés
  -- (ver constructores de NecesidadRecurrente/NecesidadExtraordinaria, que son obligatorios).
  CONSTRAINT chk_necesidad_discriminador CHECK (
    (tipo_necesidad = 'RECURRENTE' AND periodicidad IS NOT NULL AND tipo_extraordinario IS NULL)
    OR (tipo_necesidad = 'EXTRAORDINARIA' AND tipo_extraordinario IS NOT NULL AND periodicidad IS NULL)
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- Donaciones
-- ---------------------------------------------------------------------

CREATE TABLE solicitud_donacion (
  id             BIGINT NOT NULL AUTO_INCREMENT,
  donante_id     BIGINT NOT NULL,
  descripcion    VARCHAR(255) NOT NULL,
  fecha_registro DATETIME NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_solicitud_donacion_donante FOREIGN KEY (donante_id) REFERENCES donante (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE donacion (
  id                          BIGINT NOT NULL AUTO_INCREMENT,
  solicitud_id                BIGINT NOT NULL,
  donante_id                  BIGINT NOT NULL,
  entidad_beneficiaria_id     BIGINT NULL,
  necesidad_asignada_id       BIGINT NULL,
  fecha_creacion               DATETIME NOT NULL,
  estado_actual                ENUM('EN_DEPOSITO','ASIGNACION_REALIZADA','LISTA_PARA_ENTREGAR','EN_TRASLADO','ENTREGADA','ENTREGA_FALLIDA','VENCIDA') NOT NULL DEFAULT 'EN_DEPOSITO',
  matchmaking_ejecutado_en     DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_donacion_estado_actual (estado_actual),
  KEY idx_donacion_entidad_beneficiaria (entidad_beneficiaria_id),
  CONSTRAINT fk_donacion_solicitud FOREIGN KEY (solicitud_id) REFERENCES solicitud_donacion (id),
  CONSTRAINT fk_donacion_donante FOREIGN KEY (donante_id) REFERENCES donante (id),
  -- Sin ON DELETE en ninguna de estas dos: nunca se desvincula una
  -- donación histórica (en CUALQUIER estado) solo para permitir el
  -- borrado de la entidad o la necesidad que referencia -- ver
  -- NecesidadService.eliminar() y el comentario de Entrega 5 más arriba.
  CONSTRAINT fk_donacion_entidad_beneficiaria FOREIGN KEY (entidad_beneficiaria_id) REFERENCES entidad_beneficiaria (id),
  CONSTRAINT fk_donacion_necesidad_asignada FOREIGN KEY (necesidad_asignada_id) REFERENCES necesidad (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE item_donado (
  id             BIGINT NOT NULL AUTO_INCREMENT,
  solicitud_id   BIGINT NULL,
  donacion_id    BIGINT NULL,
  descripcion    VARCHAR(255) NOT NULL,
  foto           VARCHAR(500) NULL,
  categoria      ENUM('ALIMENTOS','MOBILIARIO','VESTIMENTA') NOT NULL,
  subcategoria   ENUM('SILLA','MESA','BANCO','COLCHON','FIDEOS_SECOS','ARROZ','LEGUMBRES_SECAS','ACEITE_VEGETAL','TOMATE','FRUTA','CAMPERA','REMERA','PANTALON','ROPA_INFANTIL','FRAZADA') NOT NULL,
  unidad_medida  ENUM('UNIDAD','KILOGRAMO','CAJA','PAQUETE','LITRO') NOT NULL,
  cantidad       DECIMAL(12,3) NOT NULL,
  fecha_vencimiento DATE NULL,
  condicion      ENUM('NUEVO','USADO') NULL,
  peso_kg        INT NULL,
  volumen_m3     INT NULL,
  altura_m       INT NULL,
  PRIMARY KEY (id),
  -- Sin ON DELETE CASCADE a propósito: un item_donado es historial
  -- compartido con su solicitud_donacion original (ver GestorDonaciones.eliminar,
  -- que ahora desvincula donacion_id=null antes de borrar la donación en
  -- vez de dejar que el cascade de JPA borre también estos registros).
  CONSTRAINT fk_item_donado_solicitud FOREIGN KEY (solicitud_id) REFERENCES solicitud_donacion (id),
  CONSTRAINT fk_item_donado_donacion FOREIGN KEY (donacion_id) REFERENCES donacion (id),
  CONSTRAINT chk_item_donado_cantidad CHECK (cantidad > 0),
  CONSTRAINT chk_item_donado_peso_kg CHECK (peso_kg IS NULL OR peso_kg > 0),
  CONSTRAINT chk_item_donado_volumen_m3 CHECK (volumen_m3 IS NULL OR volumen_m3 > 0),
  CONSTRAINT chk_item_donado_altura_m CHECK (altura_m IS NULL OR altura_m > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE cambio_estado (
  id             BIGINT NOT NULL AUTO_INCREMENT,
  donacion_id    BIGINT NOT NULL,
  estado_nuevo   ENUM('EN_DEPOSITO','ASIGNACION_REALIZADA','LISTA_PARA_ENTREGAR','EN_TRASLADO','ENTREGADA','ENTREGA_FALLIDA','VENCIDA') NOT NULL,
  fecha_cambio   DATETIME NOT NULL,
  justificacion  VARCHAR(255) NULL,
  PRIMARY KEY (id),
  KEY idx_cambio_estado_donacion (donacion_id, id),
  CONSTRAINT fk_cambio_estado_donacion FOREIGN KEY (donacion_id) REFERENCES donacion (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE candidata_matchmaking (
  donacion_id              BIGINT NOT NULL,
  lista                    ENUM('COMPATIBILIDAD','SUBATENCION','INTERSECCION') NOT NULL,
  posicion                 INT NOT NULL,
  entidad_beneficiaria_id  BIGINT NOT NULL,
  PRIMARY KEY (donacion_id, lista, posicion),
  CONSTRAINT fk_candidata_matchmaking_donacion FOREIGN KEY (donacion_id) REFERENCES donacion (id) ON DELETE CASCADE,
  -- entidad_beneficiaria_id es NOT NULL acá (a diferencia de donacion.*):
  -- una fila "candidata" sin entidad no tiene sentido, así que ante el
  -- borrado de la entidad se borra esta fila (no la donación ni su
  -- historial). En la práctica hoy es inalcanzable: entidad_beneficiaria
  -- nunca se borra físicamente (baja lógica) -- se deja CASCADE igual
  -- porque esta fila es un snapshot descartable, no historial.
  CONSTRAINT fk_candidata_matchmaking_entidad FOREIGN KEY (entidad_beneficiaria_id) REFERENCES entidad_beneficiaria (id) ON DELETE CASCADE,
  CONSTRAINT chk_candidata_matchmaking_posicion CHECK (posicion >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Idempotencia de eventos de Logística (antes un Set<String> en memoria en DonacionService).
CREATE TABLE evento_aplicado (
  evento_id   VARCHAR(36) NOT NULL,
  aplicado_en DATETIME NOT NULL,
  PRIMARY KEY (evento_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
