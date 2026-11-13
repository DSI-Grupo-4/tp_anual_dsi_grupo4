<!--
BORRADOR para agregar a decisiones.md, a continuación de las cinco entradas de decisiones-persona5-borrador.md.
La numeración se asigna al integrar. Todas figuran como «propuesta».
-->

## [D-???] «No recibida» e incidente solo sobre entregas en traslado y con la ruta en curso
- **Fecha:** 2026-11-11
- **Servicio(s) afectado(s):** Logística (`POST /api/rutas/{id}/paradas/{id}/no-recibida` e `/incidente`)
- **Entrega vigente al momento:** Entrega 6
- **Contexto:** Los dos endpoints marcaban **todas** las entregas de la parada sin mirar su estado, y no comprobaban el estado de la ruta. Registrar «no recibida» sobre una parada cuyas entregas ya se habían recibido las pisaba y generaba eventos erróneos hacia Donaciones; hacerlo con la ruta sin iniciar marcaba como no recibido algo que todavía no salió. Al exponerlos en la interfaz de administración, el riesgo de un clic equivocado deja de ser teórico.
- **Fuentes revisadas:** `ParadaController`, `Parada`, `Entrega`, `Ruta` y sus pruebas.
- **Opciones consideradas:**
  - A) Dejar los endpoints como están y proteger solo la interfaz web.
  - B) Corregir los endpoints existentes.
  - C) Crear endpoints nuevos y dejar los viejos.
- **Decisión tomada:** Opción B. Los dos exigen ahora que la ruta esté `INICIADA` y afectan únicamente a las entregas que seguían `EN_TRASLADO`; el resto de la parada no se toca. Los eventos (`ENTREGA_NO_RECIBIDA`, `ENTREGA_FALLIDA`) se generan solo para las entregas afectadas. Si la parada no tiene entregas en traslado, o la ruta no está en curso, responden 409. La justificación y el motivo siguen siendo obligatorios (400). Los métodos de dominio anteriores (`Parada.marcarNoRecibida` y `Parada.marcarFallida`) no se modificaron.
- **Consecuencias:** Es un cambio de comportamiento de dos endpoints existentes: una llamada que antes «funcionaba» sobre una ruta sin iniciar ahora responde 409. Se eligió corregirlos y no duplicarlos porque el comportamiento anterior era un defecto. Si la colección de Postman hacía esa llamada, hay que ajustarla.
- **Definida por:** propuesta — pendiente de confirmar por el equipo

## [D-???] Pantallas de entregas de la administración: listado, reingreso, «no recibida» e incidente
- **Fecha:** 2026-11-11
- **Servicio(s) afectado(s):** cliente web (`/admin/seguimiento/entregas` y el detalle de `/admin/seguimiento/{id}`)
- **Entrega vigente al momento:** Entrega 6
- **Contexto:** Cuando una entrega no se concreta, la administración tenía que usar Swagger para registrar el resultado y reingresarla al depósito.
- **Fuentes revisadas:** `ParadaController`, `EntregaController`, D-019 (el evento `ENTREGA_REPLANIFICADA` devuelve la donación a «En depósito»).
- **Decisión tomada:**
  - **Listado** de todas las entregas con filtro por estado, cifras y el motivo de las que fallaron.
  - **Reingresar** solo se ofrece para las `NO_RECIBIDA` y `FALLIDA`, con una confirmación; la entrega queda `REPLANIFICABLE` y el planificador la toma en su próxima corrida.
  - **Registrar «no recibida» e incidente** por parada, solo en una ruta en curso y solo si la parada tiene entregas en traslado, con un motivo obligatorio de hasta 300 caracteres.
  - Las acciones están en el detalle de la ruta y el listado cuelga de `/admin/seguimiento`, sin agregar ítems al menú.
- **Consecuencias:** Las acciones generan eventos que consume Donaciones, así que el historial de estados de una donación va a reflejar estos cambios (conviene probarlo con la persona 3). Hoy no existe el rol de chofer: las operaciones las hace la administración. El texto viaja en UTF-8 y texto plano, como espera Logística.
- **Definida por:** propuesta — pendiente de confirmar por el equipo
