# servicio-web: cliente web de DonaTrack (Entrega 6)

Este módulo es la interfaz gráfica de DonaTrack. Las vistas se renderizan en el servidor con Thymeleaf y consumen las APIs REST de Donaciones, Incentivos y Logística. Es un proyecto independiente: no depende de ningún otro módulo del repo y no tiene lógica de negocio.

Las decisiones de diseño están en [decisiones.md](../decisiones.md): D-041 (stack y estructura), D-042 (mapas, a validar por el equipo) y D-043 (rutas, datos públicos, mapa, tema de Keycloak y accesibilidad). El diagrama de componentes está en [diagramas/arquitectura](../diagramas/arquitectura/componentes.puml) ([imagen](../diagramas/arquitectura/img/componentes.png)).

**Contenido:** [Estado](#estado) · [Cómo levantarlo](#cómo-levantarlo) · [Estructura](#estructura) · [Rutas](#rutas) · [Cómo implementar una pantalla](#cómo-implementar-una-pantalla) · [Componente de mapa](#componente-de-mapa) · [Tema de login de Keycloak](#tema-de-login-de-keycloak) · [Accesibilidad](#accesibilidad) · [Trabajo pendiente](#trabajo-pendiente)

## Estado

Ya está hecha la base que usan las cinco personas:

- **Sistema de diseño y catálogo.** Tokens, componentes, JS común y un catálogo de componentes en `/dev/componentes`.
- **Layouts.** Público y privado, con menú por rol y migas de pan. Todas las rutas del sistema ya navegan.
- **Integración con las APIs.** Clientes REST, manejo de errores (páginas 404/409/500/503) y helper de fechas.
- **Páginas públicas.** Landing, galería de entregas, ayuda, privacidad, términos y entrada al registro.
- **Mapa.** Componente reutilizable y `/mapa` público.
- **Login.** Tema de login y registro para Keycloak.
- **Arranque y verificación.** Integración con `run-servicios`/`stop-servicios`, revisión de accesibilidad automatizada y 39 tests.

**Lo que no se probó todavía:**
- El cliente web no se probó contra el stack completo levantado. Las pruebas de punta a punta usaron un backend falso que devuelve el mismo JSON que Donaciones y Logística.
- Los scripts `.ps1` no se ejecutaron, porque no había PowerShell disponible. Solo se revisaron a mano.
- El tema de Keycloak se probó en un Keycloak 26.8.2 aparte, no en el de la rama `iam`.

## Cómo levantarlo

**Con todo el sistema:** `./run-servicios.sh up` (Linux/macOS) o `.\run-servicios.ps1` (Windows). Levanta la infraestructura, los cuatro servicios y, al final, el cliente web en http://localhost:8085. Para bajarlo solo a él: `./stop-servicios.sh servicio-web` o `.\stop-servicios.ps1 -Service web`.

**Solo el cliente web, para desarrollar:**

```bash
# desde la raíz del repo
mvn -pl servicio-web spring-boot:run -Dspring-boot.run.profiles=dev
```

El perfil `dev` agrega dos cosas:
- el catálogo de componentes en http://localhost:8085/dev/componentes;
- datos de ejemplo en `/mapa`, avisando en la página que lo son.

Sin backends, la landing igual carga: muestra "—" en las cifras y un aviso en lugar de las entregas.

| Variable | Valor por defecto | Para qué |
|---|---|---|
| `DONACIONES_API_URL` | `http://localhost:8080` | API de Donaciones |
| `INCENTIVOS_API_URL` | `http://localhost:8081` | API de Incentivos |
| `LOGISTICA_API_URL` | `http://localhost:8083` | API de Logística |
| `API_TIMEOUT_CONEXION` / `API_TIMEOUT_LECTURA` | `2s` / `5s` | Después de ese tiempo se muestra "Servicio no disponible" |
| `MAPA_TILES_URL` / `MAPA_ATRIBUCION` | Servidor público de OpenStreetMap | Proveedor de las imágenes del mapa |
| `AUTH_REGISTRO_URL` | `/oauth2/authorization/keycloak?registro` | Destino del botón "Crear cuenta" de `/registro`: abre el formulario de registro de Keycloak |
| `KEYCLOAK_CLIENT_SECRET` | vacío | Secret del cliente `servicio-web` en Keycloak, en `servicio-web/.env` |
| `SEGURIDAD_HABILITADA` | `false` | `true`: manda el token a las APIs (el del usuario, o el de servicio para la landing). Va igual que en los cuatro servicios |

## Estructura

```
src/main/java/ar/edu/utn/frba/dds/web/
  client/        Clientes REST: DonacionesClient, LogisticaClient, IncentivosClient
                 + PropagadorCredenciales (punto de enganche del token, Persona 2)
  config/        ApiProperties y un RestClient por backend
  controller/    Controllers de las vistas (públicas, "en construcción", catálogo)
  error/         Excepciones de backend, WebExceptionHandler y textos de la página de error
  mapa/          Marcador (contrato del componente de mapa) y su configuración
  mensajes/      Toasts para el patrón POST-redirect-GET
  navegacion/    Roles, menú lateral por rol y migas de pan
  publico/       Datos de la landing, la galería y el mapa público
  vista/         Helpers para las plantillas: @fechas y @json
src/main/resources/
  templates/layout/      publico.html y privado.html
  templates/fragments/   comunes (íconos, logo, pie, toasts), publico (tarjeta de donación), mapa
  templates/publico/     landing, galería, mapa, ayuda, registro y legales
  static/css/            tokens, base, componentes, layout, publico y mapa
  static/js/             app.js (comportamiento común) y mapa.js
  messages.properties    todos los textos de la interfaz
herramientas/accesibilidad/   revisión automática con axe-core (ver "Accesibilidad")
../keycloak/themes/donatrack/ tema de login de Keycloak
```

## Rutas

La profundidad máxima es de tres niveles (zona / sección / detalle), como pide la consigna.

| Zona | Rutas | Responsable |
|---|---|---|
| Pública | `/`, `/galeria`, `/mapa`, `/ayuda`, `/registro`, `/legal/privacidad`, `/legal/terminos` (implementadas); `/ingresar` (en construcción) | Persona 1 (ingreso y registro con la 2) |
| Donante | `/donante`, `/donante/donaciones[/{id}]`, `/donante/entidades[/{id}]`, `/donante/seguimiento`, `/donante/incentivos` | Persona 4 |
| Beneficiaria | `/beneficiaria`, `/beneficiaria/necesidades[/{id}]`, `/beneficiaria/donaciones[/{id}]`, `/beneficiaria/seguimiento` | Persona 5 |
| Admin | `/admin/donaciones[/{id}]`, `/admin/asignaciones`, `/admin/donantes[/{id}]`, `/admin/entidades[/{id}]` | Persona 3 |
| Admin | `/admin/reportes` | Persona 4 |
| Admin | `/admin/camiones`, `/admin/seguimiento` | Persona 5 |
| Admin | `/admin` (panel) | Persona 3 |

Las rutas privadas pendientes muestran "Sección en construcción"; las rutas de administración de Persona 3 ya tienen vistas funcionales. Para agregar una sección que no está en el menú, se suma en `NavegacionService`.

## Cómo implementar una pantalla

1. **Controller.** Creá un controller con la ruta literal, por ejemplo `@GetMapping("/admin/donaciones")`. Spring lo prioriza sobre la ruta genérica de "en construcción" (`SeccionesPrivadasController`), así que no hace falta tocar esa clase. Ubicalo en `controller/admin`, `controller/donante` o `controller/beneficiaria`, según la zona.
2. **Datos.** Pedilos siempre por el cliente del servicio. Si falta un endpoint, agregá un método en `DonacionesClient`, `LogisticaClient` o `IncentivosClient`. Mapeá la respuesta a un record propio con `@JsonIgnoreProperties(ignoreUnknown = true)`, que tenga solo los campos que usa la vista. No importes clases de otros módulos ni hagas HTTP directo desde un controller.
3. **Vista.** Usá el layout de tu zona y definí el contenido como fragmento:
   ```html
   <html lang="es" xmlns:th="http://www.thymeleaf.org"
         th:replace="~{layout/privado :: layout(#{nav.admin.donaciones}, ~{::contenido}, _)}">
   <body>
   <th:block th:fragment="contenido">
     <div class="cabecera-pagina"><h1 th:text="#{nav.admin.donaciones}">Donaciones</h1></div>
     ...
   </th:block>
   </body>
   </html>
   ```
   El tercer parámetro recibe los scripts y estilos propios de la vista: `~{::scripts}`, `~{fragments/mapa :: recursos}`, o `_` si no hay.
4. **Formularios.** Seguí el patrón POST-redirect-GET.
   - Si la operación sale bien: `Toasts.exito(redirectAttributes, "...")` y redirect.
   - Si el backend la rechaza, atrapá `BackendRechazoException`, llamá a `e.volcarEn(bindingResult)` y volvé a mostrar el formulario. Los errores quedan en sus campos.
   - Los errores de conexión y los 404 los maneja `WebExceptionHandler`.
5. **Fechas.** Usá `${@fechas.fechaHora(x)}`, `${@fechas.fecha(x)}` o `${@fechas.mes(x)}`. Las APIs devuelven UTC y el helper lo pasa a la hora de Argentina.
6. **Datos para JavaScript.** Pasalos con `th:attr="data-algo=${@json.escribir(objeto)}"` y leelos con `JSON.parse(el.dataset.algo)`. No los escribas dentro de un `<script>`: un texto cargado por un usuario podría romper la página (D-043).
7. **Textos.** Van en `messages.properties`, con el prefijo de tu sección (`admin.donaciones.*`, `donante.*`, etc.).
8. **Componentes.** Copiá el HTML del catálogo (`/dev/componentes`). No escribas colores, tamaños ni espaciados sueltos: si falta algo, se agrega como token o componente nuevo y se muestra en el catálogo.
9. **Ocultar elementos.** Usá el atributo `hidden` (o `el.hidden = true` en JS). `base.css` garantiza que oculte aunque el componente tenga su propio `display`.
10. **Tests.** Probá cada controller con MockMvc, y cada método nuevo de un cliente con `MockRestServiceServer`. Hay ejemplos en `src/test`. Antes de dar por terminada una pantalla, corré la [revisión de accesibilidad](#accesibilidad) sobre ella.

## Componente de mapa

Es el mismo componente para el mapa público (`/mapa`), el seguimiento del donante (Persona 4) y el de la entidad y la administración (Persona 5). Dibuja los marcadores con Leaflet y los muestra también como lista, que es la alternativa para lectores de pantalla. Puede refrescarse solo cada N segundos.

**Contrato de un marcador** ([Marcador.java](src/main/java/ar/edu/utn/frba/dds/web/mapa/Marcador.java)):

```json
{ "lat": -34.3497, "lng": -58.793, "titulo": "Comedor Escobar Sonrisas",
  "detalle": "Arroz 1 kg × 20", "actualizadoEn": "2026-11-03T17:30Z", "tipo": "entrega" }
```

- `tipo` es `entrega`, `camion` o `sede`, y define el color y el ícono del pin.
- `actualizadoEn` es una fecha ISO con zona, o `null`. Se muestra en hora de Argentina como "Actualizado: dd/MM/aaaa hh:mm".
- Para crear uno desde una fecha UTC de las APIs: `Marcador.de(lat, lng, titulo, detalle, fechaUtc, Marcador.CAMION)`.

**Uso en una vista:**

```html
<html lang="es" xmlns:th="http://www.thymeleaf.org"
      th:replace="~{layout/privado :: layout(#{nav.donante.seguimiento}, ~{::contenido}, ~{fragments/mapa :: recursos})}">
...
<section th:replace="~{fragments/mapa :: mapa('mapa-seguimiento', 'Mapa de tus entregas en camino',
         ${marcadores}, @{/donante/seguimiento/ubicaciones.json}, 30)}"></section>
```

| Parámetro | Para qué |
|---|---|
| `id` | Único en la página |
| título | Nombre accesible del mapa |
| `marcadores` | Lista inicial de `Marcador` |
| URL | Endpoint propio que devuelve `List<Marcador>` en JSON, o `null` |
| intervalo | Segundos entre refrescos; `0` para no refrescar |

El endpoint se escribe como `@GetMapping(value = ".../ubicaciones.json", produces = "application/json") @ResponseBody`. Hay un ejemplo en `PublicoController.mapaDatos()`. El refresco se pausa cuando la pestaña no está visible, y si falla avisa en la página.

**Mapa público:** hoy usa `MapaPublicoSinCoordenadas`, que muestra el mapa vacío con un aviso, porque las APIs no tienen coordenadas (D-042). Cuando Logística las exponga, hay que escribir otra implementación de `MapaPublico` con `LogisticaClient` y borrar la provisoria. Las vistas no cambian.

## Tema de login de Keycloak

El login y el registro los muestra Keycloak, no `servicio-web`. Para que se vean como el resto de la aplicación está el tema [keycloak/themes/donatrack](../keycloak/themes/donatrack):
- hereda de `keycloak.v2`;
- agrega fondo lavanda, tarjeta blanca, botón oscuro, enlaces violeta, logo de DonaTrack, Fira Sans y controles de 48 px;
- traduce al español los textos que el tema base deja en inglés.

En la rama `iam` el volumen del tema, `loginTheme`, el idioma `es` y los flags `--spi-theme--cache-themes=false --spi-theme--cache-templates=false` ya están en el compose y en el JSON del realm. Si el realm ya existe en MySQL, el import no lo pisa: hay que elegir el tema en la consola. Los pasos están en [GUIA-LEVANTAR-SERVICIOS.md](../GUIA-LEVANTAR-SERVICIOS.md), sección "Login del cliente web".

**Regla:** `resources/css/tokens.css` del tema es una copia de [static/css/tokens.css](src/main/resources/static/css/tokens.css), porque Keycloak no puede pedírselo a `servicio-web`. Si cambiás los tokens, copiá el archivo al tema. Si te olvidás, `TemaKeycloakTest` falla.

## Accesibilidad

La consigna pide WCAG AA, foco visible, áreas táctiles de 48 × 48 px, diseño responsive y loaders para operaciones de más de 300 ms. La herramienta [herramientas/accesibilidad](herramientas/accesibilidad/revision.js) revisa todo eso de forma automática:

- **Cobertura:** cada página en escritorio y en celular.
- **Reglas:** WCAG 2.0, 2.1 y 2.2 niveles A y AA, con axe-core.
- **Teclado y táctil:** recorre la página con Tab verificando que cada elemento muestre el foco, y mide el área clickeable de cada control.
- **Interacción:**
  - enlace para saltar al contenido;
  - toasts, modal (cierra con Esc y devuelve el foco) y pestañas con flechas;
  - tooltip con mouse y con foco;
  - loader: oculto antes de 300 ms, visible después y oculto al terminar;
  - marcadores y lista del mapa, con mouse y teclado;
  - menú en celular y desborde horizontal.

```bash
# con servicio-web levantado con el perfil dev
cd servicio-web/herramientas/accesibilidad
npm install
npm run revisar
# para sumar tus pantallas:
PAGINAS_EXTRA=/admin/donantes,/donante/incentivos npm run revisar
# para revisar también el login de Keycloak:
KC_AUTH_URL=http://localhost:8180/realms/donatrack/protocol/openid-connect \
KC_QUERY="client_id=<cliente>&redirect_uri=<uri registrada>&response_type=code&scope=openid" npm run revisar
```

Usa el Chromium del sistema (`CHROMIUM=/ruta` para otro). Termina con código 1 si hay violaciones, elementos sin foco visible o fallas de interacción. El detalle queda en `revision.json`.

**Resultado actual** (10/11/2026, D-043): **0 violaciones** en 26 combinaciones de página y dispositivo, incluidos el login y el registro de Keycloak. Foco visible en todos los elementos, sin desborde horizontal, y todas las pruebas de interacción en verde. Quedan dos excepciones aceptadas:
- Los enlaces de atribución de Leaflet y OpenStreetMap miden menos de 48 px de alto. Son texto en línea exigido por la licencia de OSM, y WCAG 2.5.8 exceptúa los enlaces en línea.
- El checkbox del catálogo mide 20 px, pero su `<label>` de 48 px también lo activa.

### Checklist de UI de la consigna (vale para todas las pantallas)
- [ ] Cada control tiene `<label>`. Las ayudas y errores se asocian con `aria-describedby`, y los campos con error llevan `aria-invalid="true"`.
- [ ] Todo lo clickeable mide al menos 48×48 px. Las clases `.boton`, `.control` y `.menu-lateral__enlace` ya lo cumplen. Un enlace suelto fuera de un párrafo, usá `class="boton boton--fantasma"`.
- [ ] El foco es visible. No usar `outline: none` sin reemplazo.
- [ ] Cada acción muestra su resultado (toast o banner) y cada error se explica.
- [ ] Operaciones de más de 300 ms: formularios y enlaces muestran el loader solos; para un `fetch` se usa `DonaTrack.conLoader(...)`.
- [ ] La pantalla funciona en celular (menos de 600 px), tablet y escritorio, sin superposiciones.
- [ ] Una tabla que puede desbordar va en `<div class="tabla-contenedor" tabindex="0" role="region" aria-label="...">`.
- [ ] El estado de una donación no se indica solo con color: lleva texto siempre.
- [ ] La sección se alcanza en tres clics o menos desde el panel.
- [ ] `npm run revisar` con tus pantallas en `PAGINAS_EXTRA` termina sin violaciones.

---

## Trabajo pendiente

### Persona 1 (cliente web, diseño compartido y acceso público)
Terminado: las fases 1 a 9 del plan (ver [Estado](#estado)). Queda:

- [ ] Probar el cliente web con el stack real levantado (`run-servicios`), en lugar del backend falso que se usó hasta ahora. En particular, verificar que el JSON de `GET /api/entregas`, `/api/donaciones`, `/api/entidades` y `/api/donantes` mapee como se espera.
- [ ] Ejecutar `run-servicios.ps1` y `stop-servicios.ps1` en Windows (no se pudieron ejecutar acá).
- [ ] Cuando las Personas 3 y 5 publiquen los endpoints públicos (D-041), reemplazar `FuentePublicaApis` por una implementación que los use. Hoy pide los listados completos en cada visita a la landing: sirve para el TP, pero no escala.
- [ ] Cuando Logística exponga coordenadas (D-042), reemplazar `MapaPublicoSinCoordenadas` por la implementación real.
- [ ] Con la Persona 2, implementar el formulario para completar el perfil después del registro en Keycloak. Crea el Donante o la Entidad, con la ubicación de la sede marcada en el mapa.
- [ ] Revisar con el equipo los borradores legales (ver "Pendientes del equipo").
- [ ] Subir la rama: `git push -u origin feat/servicio-web` y abrir el PR.

### Persona 2 (autenticación, SSO, social login y permisos)
Implementado en la rama `iam` (D-044 a D-047; pasos y pruebas en [GUIA-LEVANTAR-SERVICIOS.md](../GUIA-LEVANTAR-SERVICIOS.md)). En esta máquina no se compiló ni se probó en Docker:

- Login con Keycloak (authorization code), registro con `prompt=create`, ingreso con Google configurable, logout con `end_session` y borrado de `DONATRACK_SESSION`.
- `/admin/**`, `/donante/**` y `/beneficiaria/**` por rol; `/cuenta` para usuarios sin rol, que pueden pedir la vinculación con su email verificado.
- `CredencialesKeycloak` (implementa `PropagadorCredenciales`): manda el access token del usuario, renovándolo con el refresh token; la landing y las fotos usan el token de servicio de `servicio-web`.
- 401 de las APIs: se cierra la sesión y vuelve al login con el aviso "Tu sesión venció". 403: página de acceso denegado.
- Las cuatro APIs como resource servers, con reglas por endpoint y pertenencia (`donante_id`, `entidad_ids`), detrás de `SEGURIDAD_HABILITADA`. Llamadas entre servicios con client_credentials.
- Cuentas creadas y vinculadas desde Donaciones al dar de alta o importar donantes y entidades (varios representantes por entidad).
- Autorregistro en `/cuenta` (D-047): donante humano, organización donante con un único representante, y entidad nueva con el primer representante. No reclama un registro ya cargado.

Pendiente:

- [ ] Compilar, correr los tests y probar en Docker con la guía.
- [ ] Con la Persona 1: la sede se carga como dirección de texto. El dominio no tiene un punto en el mapa (D-042).
- [ ] Endpoint "mis donaciones" para la vista del donante: hoy solo existe el acceso a una donación por id.

### Persona 3 (administración y gestión de Donaciones)
Implementado:

- Panel `/admin` con cantidades del depósito y accesos a las tareas principales.
- Donantes y entidades: búsqueda paginada, alta, ficha, edición, baja lógica y reactivación. Correo obligatorio, contacto preferido, dirección humana y representantes de organizaciones.
- Importación CSV de hasta 20 MB, ejemplo descargable, indicador de espera y resultado. Cliente de importación con timeout independiente de 10 minutos (`api.timeout-importacion`).
- Carga de varios bienes con categoría, subcategoría, unidad, condición o vencimiento y medidas logísticas. El backend realiza la segmentación.
- Donaciones: búsqueda paginada por descripción, estado, categoría, subcategoría, fechas y relaciones con donante o entidad; detalle, edición, eliminación en depósito, historial y vencimiento justificado.
- Asignaciones: ejecución manual, lectura del último resultado persistido, tres listas separadas y confirmación de candidata. Consultar o refrescar la página no ejecuta los algoritmos.
- API `/api/administracion/{donantes,entidades,donaciones,resumen}`. Los listados aceptan `page` desde 0 y `size` entre 1 y 100; las vistas usan 20 registros.
- Endpoint público `GET /api/publico/donaciones/destacadas?limite=6`: últimas entregas de los últimos 30 días, máximo 20, sin datos personales de donantes. Queda disponible para que Persona 1 conecte la landing; la galería y sus fotos siguen siendo parte de Logística.

Para probar, levantá el sistema con `run-servicios` y abrí **http://localhost:8085/admin**. No simula un inicio de sesión: hasta integrar Persona 2, el acceso sigue siendo directo. La protección ADMIN y la vinculación de cuentas/representantes quedan pendientes de esa integración. `AdminDonacionesClient` reutiliza la propagación de credenciales de los clientes existentes.

La dirección humana agrega cuatro columnas anulables a `persona_humana`. El arranque aplica esa ampliación de forma aditiva e idempotente antes de la validación JPA; no borra tablas ni datos. El usuario de base necesita permiso para agregar esas columnas en la primera ejecución. El esquema SQL de instalaciones nuevas también está actualizado.

Verificación: `mvn -pl servicio-web,servicio-donaciones -am test`. Hay pruebas MVC, contratos HTTP, persistencia H2, importación de 10.001 filas y consulta pública sin datos personales. Las comprobaciones no envían correos reales. La revisión en navegador usa un backend aislado; todavía corresponde realizar la prueba final con todos los servicios y la autenticación integrados.

### Persona 4 (portal del donante, Incentivos y reportes)
- [ ] Pantallas `/donante/**` y `/admin/reportes`:
  - panel del donante;
  - sus donaciones, con filtros por estado, categoría y subcategoría;
  - navegación por entidades beneficiarias;
  - métricas, misiones e insignias, con el cambio de visibilidad de insignias;
  - ranking mensual e historial de rankings.
- [ ] La sección de Incentivos es solo para donantes personas humanas (requerimiento 7): ocultarla y bloquearla para personas jurídicas.
- [ ] Agregar los métodos de `IncentivosClient`, que hoy está vacío.
- [ ] `/donante/seguimiento`: usar el [componente de mapa](#componente-de-mapa) con marcadores de tipo `camion` y un endpoint propio con refresco, con los datos de ubicación que expone la Persona 5.
- [ ] Usar las categorías de misiones del dominio (Colaborador, Sostenedor, Transformador). Los mockups dicen "Bronze" y "Donor", y eso quedó desactualizado.

### Persona 5 (portal beneficiario y operación logística)
- [ ] Pantallas `/beneficiaria/**`, `/admin/camiones` y `/admin/seguimiento`:
  - panel de la entidad;
  - necesidades recurrentes y extraordinarias;
  - donaciones asignadas y su estado;
  - confirmación de recepción con fotos;
  - ABM de camiones;
  - seguimiento de rutas y entregas.
- [ ] Carga de fotos: hoy `FotoEntrega` recibe una URL. Hay que resolver cómo un archivo que sube el navegador se convierte en esa URL. La galería pública solo acepta URLs `http(s)`.
- [ ] **Mapas (D-042, a validar con el equipo):**
  - Coordenadas de las sedes de las entidades, cargadas con un clic en el mapa, sin geocodificación.
  - Endpoint para reportar la ubicación del camión, por ejemplo `POST /api/camiones/{id}/ubicacion` desde el chofer o un simulador. Logística guarda latitud, longitud y fecha/hora y las deja disponibles para consulta, sin llamar a otros servicios.
  - Exponer las coordenadas de las entregas realizadas para el mapa público, sin dirección.
  - En `servicio-web`, convertir esos datos al [contrato de marcadores](#componente-de-mapa).
- [ ] **Para la landing (D-041), acordarlo con la Persona 1:** un endpoint público de solo lectura con las entregas recientes que tienen foto (foto, fecha, entidad y donación asociada), sin dirección ni camión.
- [ ] Agregar los métodos que falten en `LogisticaClient`. Ya existe `entregas()`.

### Pendientes del equipo
- [ ] Validar D-042 (mapas). Afecta a las Personas 1, 4 y 5.
- [ ] Acordar los endpoints públicos de la landing (D-041). Afecta a las Personas 1, 3 y 5.
- [ ] Decidir si la campana de notificaciones de los mockups se implementa como panel dentro de la app o se saca.
- [ ] Decidir qué muestra el panel `/admin`.
- [ ] Revisar los borradores de [privacidad](src/main/resources/templates/publico/legal/privacidad.html) y [términos](src/main/resources/templates/publico/legal/terminos.html). Falta definir un medio de contacto real para ejercer los derechos de la Ley 25.326 (hoy dice "una persona administradora").
- [ ] Mantener actualizado el [diagrama de componentes](../diagramas/arquitectura/componentes.puml) a medida que se implementen la autenticación y los endpoints nuevos. Lo pide la Entrega 7. Para regenerar la imagen: `docker run --rm -u $(id -u):$(id -g) -v $PWD/diagramas/arquitectura:/data plantuml/plantuml:1.2024.7 -tpng -o /data/img /data/componentes.puml`.
- [ ] Prueba integrada final: registro → ingreso → carga de donación → asignación → seguimiento → recepción → actualización de métricas.
