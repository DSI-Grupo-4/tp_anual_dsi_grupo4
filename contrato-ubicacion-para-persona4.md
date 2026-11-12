# Ubicación del camión: lo que necesita la Persona 4 para `/donante/seguimiento`

Ya están en la rama (parte 3 de la Persona 5): los endpoints de Logística, los campos nuevos y un helper para armar los marcadores.

## Qué expone Logística

`GET /api/rutas` ya trae la ubicación dentro del camión de cada ruta:

```json
{ "idRuta": 7, "estadoRuta": "INICIADA",
  "camionAsociado": { "idCamion": 1, "patente": "AA123BB", "estadoCamion": "EN_RUTA",
                      "latitud": -34.61, "longitud": -58.39,
                      "ubicacionActualizadaEn": "2026-11-12T17:30:00.123", "ubicacionSimulada": true },
  "paradas": [ { "numeroParada": 1, "idEntidadBeneficiariaAsociada": 3,
                 "entregas": [ { "idEntrega": 9, "idDonacionAsociada": 15, "estadoEntrega": "EN_TRASLADO" } ] } ] }
```

- `ubicacionActualizadaEn` está en UTC; el helper `Marcador.de(...)` y el componente de mapa la convierten a hora de Argentina.
- Un camión que nunca informó su ubicación tiene esos campos en `null`.
- También existen `GET /api/camiones/ubicaciones` (todos los camiones con ubicación) y `GET /api/camiones/{id}/ubicacion`.
- Para ubicar una donación: `idDonacionAsociada` de la entrega es el id de la donación de Donaciones.

## Cómo armar el mapa del donante

`RutasClient` y `MarcadoresSeguimiento` ya existen en `servicio-web`:

```java
// controller/donante/SeguimientoDonanteController.java
@GetMapping("/donante/seguimiento")
public String seguimiento(Model model) {
    Set<Long> misDonaciones = ...;                       // ids de las donaciones del donante (DonacionesClient)
    model.addAttribute("marcadores", marcadores.paraDonaciones(misDonaciones, rutasClient.rutas()));
    return "donante/seguimiento";
}

@GetMapping(value = "/donante/seguimiento/ubicaciones.json", produces = "application/json")
@ResponseBody
public List<Marcador> ubicaciones() {
    return marcadores.paraDonaciones(misDonaciones(), rutasClient.rutas());
}
```

En la vista, el tercer parámetro del layout es `~{fragments/mapa :: recursos}` y el mapa se incluye con
`~{fragments/mapa :: mapa('mapa-donante', #{...}, ${marcadores}, @{/donante/seguimiento/ubicaciones.json}, 15)}`.
Hay un ejemplo completo en `templates/beneficiaria/seguimiento.html`.

`paraDonaciones` devuelve solo los camiones de rutas **en curso** que llevan alguna de las donaciones pedidas. Del resto no
muestra nada. Los textos del marcador salen de `messages-logistica.properties`.

## Para probarlo sin chofer

En el detalle de una ruta en curso (`/admin/seguimiento/{id}`), el botón «Simular avance del camión» mueve el camión un paso
hacia la próxima parada. O por la API: `curl -X PUT localhost:8083/api/camiones/1/ubicacion -H "Content-Type: application/json" -d '{"latitud":-34.61,"longitud":-58.39}'`.
