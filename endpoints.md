## servicio-donaciones (puerto 8080)
Base URL: http://localhost:8080

Health check

curl http://localhost:8080/ping
Donaciones (/api/donaciones)

# Crear
curl -X POST http://localhost:8080/api/donaciones -H "Content-Type: application/json" -d '{
  "descripcionItem": "Arroz 1kg",
  "cantidadAsignada": 10,
  "entidadBeneficiariaId": 1,
  "necesidadId": 1,
  "pesoKg": 10,
  "volumenM3": 1,
  "alturaM": 1
}'

# Listar todas
curl http://localhost:8080/api/donaciones

# Listar pendientes (paginado)
curl "http://localhost:8080/api/donaciones/pendientes?page=0&size=100"

# Obtener por id
curl http://localhost:8080/api/donaciones/1

# Actualizar (mismo body que crear)
curl -X PUT http://localhost:8080/api/donaciones/1 -H "Content-Type: application/json" -d '{ "descripcionItem": "Arroz 1kg", "cantidadAsignada": 10, "entidadBeneficiariaId": 1, "necesidadId": 1 }'

# Eliminar
curl -X DELETE http://localhost:8080/api/donaciones/1

# Cambiar estado
curl -X PATCH http://localhost:8080/api/donaciones/1/estado -H "Content-Type: application/json" -d '{
  "nuevoEstado": "EN_DEPOSITO",
  "justificacion": "Recibido en depósito central"
}'
# Valores válidos de nuevoEstado: PENDIENTE_CONFIRMACION, EN_DEPOSITO, ASIGNACION_REALIZADA, LISTA_PARA_ENTREGAR, EN_TRASLADO, ENTREGADA, ENTREGA_FALLIDA, VENCIDA

# Historial de estados
curl http://localhost:8080/api/donaciones/1/historial

# Entidades candidatas (matchmaking)
curl http://localhost:8080/api/donaciones/1/candidatas

# Asignar a entidad
curl -X POST http://localhost:8080/api/donaciones/1/asignar -H "Content-Type: application/json" -d '{ "entidadId": 2 }'
Donantes (/api/donantes)

# Listar todos
curl http://localhost:8080/api/donantes

# Obtener por id
curl http://localhost:8080/api/donantes/1

# Crear donante persona humana
curl -X POST http://localhost:8080/api/donantes/humanos -H "Content-Type: application/json" -d '{
  "nombre": "Juan",
  "apellido": "Perez",
  "edad": 30,
  "documento": "30111222",
  "genero": "MASCULINO"
}'
# genero: MASCULINO, FEMENINO, X

# Crear donante persona jurídica
curl -X POST http://localhost:8080/api/donantes/juridicos -H "Content-Type: application/json" -d '{
  "razonSocial": "ACME SA",
  "tipo": "EMPRESA",
  "rubro": "Alimentos"
}'
# tipo: GUBERNAMENTAL, ONG, EMPRESA, INSTITUCION

# Actualizar (tipo determina el sub-DTO usado: HUMANA o JURIDICA)
curl -X PUT http://localhost:8080/api/donantes/1 -H "Content-Type: application/json" -d '{
  "tipo": "HUMANA",
  "nombre": "Juan",
  "apellido": "Perez",
  "documento": "30111222"
}'

# Eliminar
curl -X DELETE http://localhost:8080/api/donantes/1

# Importar CSV (multipart)
curl -X POST http://localhost:8080/api/donantes/importar -F "archivo=@donantes.csv"
Entidades beneficiarias (/api/entidades)

curl http://localhost:8080/api/entidades
curl http://localhost:8080/api/entidades/1

curl -X POST http://localhost:8080/api/entidades -H "Content-Type: application/json" -d '{
  "personaJuridica": { "razonSocial": "Comedor San José", "tipo": "ONG", "rubro": "Comedor" },
  "descripcion": "Comedor comunitario",
  "direccion": {
    "calle": "Av. Siempre Viva",
    "numero": "123",
    "ciudad": { "nombre": "CABA", "provincia": { "nombre": "Buenos Aires" } }
  }
}'

curl -X PUT http://localhost:8080/api/entidades/1 -H "Content-Type: application/json" -d '{ ... mismo body que crear ... }'

curl -X DELETE http://localhost:8080/api/entidades/1
Necesidades (/api/entidades/{entidadId}/necesidades)

curl http://localhost:8080/api/entidades/1/necesidades

# Recurrente
curl -X POST http://localhost:8080/api/entidades/1/necesidades/recurrentes -H "Content-Type: application/json" -d '{
  "descripcion": "Leche en polvo",
  "subcategoria": "Lácteos",
  "cantidadRequerida": 50,
  "periodicidad": "MENSUAL"
}'
# periodicidad: DIARIA, SEMANAL, MENSUAL, ANUAL

# Extraordinaria
curl -X POST http://localhost:8080/api/entidades/1/necesidades/extraordinarias -H "Content-Type: application/json" -d '{
  "descripcion": "Frazadas",
  "subcategoria": "Abrigo",
  "cantidadRequerida": 100,
  "tipoExtraordinario": "OLA_POLAR"
}'
# tipoExtraordinario: INUNDACION, SISMO, OLA_POLAR

# Actualizar (tipo: RECURRENTE o EXTRAORDINARIA determina qué campos se usan)
curl -X PUT http://localhost:8080/api/entidades/1/necesidades/5 -H "Content-Type: application/json" -d '{
  "tipo": "RECURRENTE",
  "descripcion": "Leche en polvo",
  "subcategoria": "Lácteos",
  "cantidadRequerida": 60,
  "periodicidad": "MENSUAL"
}'

curl -X DELETE http://localhost:8080/api/entidades/1/necesidades/5
Asignaciones (/api/asignaciones)

curl -X POST http://localhost:8080/api/asignaciones/candidatas -H "Content-Type: application/json" -d '{
  "descripcionItem": "Arroz 1kg",
  "subcategoria": "Alimentos secos",
  "cantidad": 10,
  "algoritmo": "PROXIMIDAD"
}'
servicio-incentivos (puerto 8081)
Base URL: http://localhost:8081

Ranking (/ranking)

curl http://localhost:8081/ranking/historial
curl "http://localhost:8081/ranking/mes?fecha=2026-06-01"   # fecha formato YYYY-MM-DD, requerida
curl http://localhost:8081/ranking/ultimo
Admin (/admin)

curl http://localhost:8081/admin/metricas
Donantes / Incentivos (/donantes)

# Métricas de actividad
curl "http://localhost:8081/donantes/1/metricas?periodo=HISTORICO"
# periodo (opcional, default HISTORICO): MENSUAL, TRIMESTRAL, SEMESTRAL, ANUAL, HISTORICO

# Misiones disponibles
curl http://localhost:8081/donantes/1/misiones

# Insignias obtenidas
curl http://localhost:8081/donantes/1/insignias

# Registrar actividad de donación
curl -X POST http://localhost:8081/donantes/1/actividad-donacion -H "Content-Type: application/json" -d '{
  "fecha": "2026-07-01",
  "categoriaNombre": "Alimentos",
  "cantidadBienes": 10,
  "donacionExitosa": true,
  "beneficiarioId": 3,
  "beneficiarioNombre": "Comedor San José",
  "donanteNombre": "Juan Perez"
}'
# nota: si el donante no existe, se crea usando "donanteNombre"

# Cambiar visibilidad de una insignia
curl -X PATCH http://localhost:8081/donantes/1/insignias/Primer%20Paso/visibilidad -H "Content-Type: application/json" -d '{ "visible": false }'
Notas:

Todos los {id} de donante/entidad/donación/necesidad son Long numéricos.
Los endpoints marcados @ResponseStatus(HttpStatus.CREATED) devuelven 201; los DELETE devuelven 204 sin body.
No vi autenticación/autorización en ningún controller — todos los endpoints están abiertos.


# obtencion de insignia

curl -X POST http://localhost:5678/webhook/events/insignia-otorgada \
  -H "Content-Type: application/json" \
  -d '{
    "donanteId": 1,
    "donanteNombre": "Juan Perez",
    "insigniaNombre": "Primer Paso",
    "imagenUrl": "https://static.vecteezy.com/system/resources/previews/011/964/284/non_2x/badge-correct-mark-icon-vector.jpg",
    "fechaObtencion": "2026-07-03"
  }'