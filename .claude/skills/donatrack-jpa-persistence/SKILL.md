---
name: donatrack-jpa-persistence
description: Buenas prácticas de persistencia con Spring Data JPA/Hibernate sobre MySQL para los servicios de DonaTrack. Usar al crear o modificar entidades JPA, repositorios, migraciones de esquema, mapeos de herencia (persona humana/jurídica, necesidad recurrente/extraordinaria) o al diseñar el diagrama entidad-relación físico de un servicio. También usar cuando el usuario pida separar el modelo de dominio del modelo de persistencia.
---

# Persistencia JPA — DonaTrack (Spring Boot + MySQL)

## Reglas de esquema
- Motor único (MySQL8) compartido por todos los servicios, **un esquema por servicio** (ej. `donaciones`, `logistica`, `incentivos`, `notificaciones`). Configurar `spring.jpa.properties.hibernate.default_schema` por servicio, no una base física separada.
- Migraciones versionadas con **Flyway** (o Liquibase): nunca `ddl-auto: update` en un entregable — usar `validate` y scripts `V{n}__descripcion.sql` versionados en el repo. `ddl-auto: create-drop` solo para tests.
- Antes de escribir el DER físico de un servicio, generarlo desde las migraciones reales, no a mano — evita que el diagrama entregado se desincronice del código.

## Dominio vs. persistencia
- Cuando el modelo de dominio tenga comportamiento rico (ej. `Donacion.segmentar()`, la máquina de estados, el cálculo de progreso de una misión), **no** exponer directamente la entidad `@Entity` como agregado de dominio si eso obliga a meter lógica de negocio en clases anotadas con JPA. Preferir:
  - Entidad JPA como modelo de persistencia (anémico, solo mapeo).
  - Clase de dominio separada con la lógica, mapeada por un `Mapper`/`Converter` explícito (manual o MapStruct).
  - Esto es una decisión de diseño a justificar en el documento de arquitectura, no aplicarlo "porque sí" en todos lados si el equipo decidió otra cosa — chequear consistencia con lo ya implementado en Entregas 1-3 antes de refactorizar.

## Mapeos específicos del dominio DonaTrack
- **Persona donante humana/jurídica**: herencia — evaluar `JOINED` (tablas separadas, más normalizado, mejor para el TP dado que los atributos difieren bastante) vs `SINGLE_TABLE` (más simple, columnas nulleables). Justificar la elección en el documento de diseño.
- **Donación original vs. donaciones segmentadas**: modelar la relación 1-a-N explícita (`DonacionOriginal` → `List<Donacion>`), no perder la trazabilidad de qué carga original generó cada donación segmentada.
- **Estados de la donación**: persistir el estado actual **y** el historial de transiciones (tabla de auditoría `donacion_estado_historico` con timestamp, estado anterior, estado nuevo, y justificación en caso de "Entrega fallida"). No modelar el estado como un simple enum sin historial: la consigna exige trazabilidad y auditoría explícitamente.
- **Necesidad recurrente vs. extraordinaria**: mismo criterio de herencia que personas donantes; la extraordinaria necesita cantidad requerida/acumulada, la recurrente necesita período y cantidad objetivo por período.
- **Bienes perecederos**: fecha de vencimiento nullable a nivel bien, pero la subcategoría/categoría determina si es obligatoria — validar a nivel de servicio de dominio, no solo con `NOT NULL` en base.

## Rendimiento y buenas prácticas generales
- `@ManyToOne(fetch = FetchType.LAZY)` por defecto; evitar N+1 con `@EntityGraph` o `JOIN FETCH` en consultas que el diagrama de estados o el ranking mensual necesiten resolver en batch.
- Índices explícitos (vía migración) sobre columnas de búsqueda frecuente: email de persona donante (unique, usado en la importación CSV para detectar existentes), estado de donación, fecha de asignación.
- Para la importación masiva de CSV (+10.000/20.000 filas): usar inserts/updates en lote (`saveAll` con `hibernate.jdbc.batch_size`, o `JdbcTemplate` batch) — no una transacción por fila.

## Al generar el entregable "Modelo de datos: DER físico por servicio"
Incluir tipos de columna reales, claves foráneas, constraints de unicidad (email, patente de camión) e índices — es un diagrama físico, no un diagrama de clases disfrazado.
