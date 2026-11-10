# servicio-web: cliente web de DonaTrack (Entrega 6)

Este módulo es la interfaz gráfica de DonaTrack. Las vistas se renderizan en el servidor con Thymeleaf y consumen las APIs REST de Donaciones, Incentivos y Logística. Es un proyecto independiente: no depende de ningún otro módulo del repo y no tiene lógica de negocio. Las decisiones de diseño están en [decisiones.md](../decisiones.md): D-041 (stack y estructura) y D-042 (mapas).

## Cómo levantarlo

```bash
# desde la raíz del repo
mvn -pl servicio-web spring-boot:run -Dspring-boot.run.profiles=dev
```

La aplicación queda en http://localhost:8085. Con el perfil `dev`, el catálogo de componentes queda en http://localhost:8085/dev/componentes.

Las páginas que consultan APIs necesitan los backends levantados (`./run-servicios.sh up`). Si un backend no responde, la página muestra "Servicio no disponible" en lugar de fallar.

| Variable | Valor por defecto |
|---|---|
| `DONACIONES_API_URL` | `http://localhost:8080` |
| `INCENTIVOS_API_URL` | `http://localhost:8081` |
| `LOGISTICA_API_URL` | `http://localhost:8083` |
| `API_TIMEOUT_CONEXION` / `API_TIMEOUT_LECTURA` | `2s` / `5s` |

## Estructura

```
src/main/java/ar/edu/utn/frba/dds/web/
  client/        Clientes REST: DonacionesClient, LogisticaClient, IncentivosClient
                 + PropagadorCredenciales (punto de enganche del token, Persona 2)
  config/        ApiProperties y un RestClient por backend
  controller/    Controllers de las vistas
  error/         Excepciones de backend y WebExceptionHandler (páginas 404/409/500/503)
  mensajes/      Toasts para el patrón POST-redirect-GET
  navegacion/    Roles, menú lateral por rol y migas de pan
  vista/         Helpers para las plantillas (@fechas)
src/main/resources/
  templates/layout/      publico.html y privado.html
  templates/fragments/   comunes.html (íconos, logo, pie, toasts, "en construcción")
  static/css/            tokens, base, componentes y layout
  static/js/app.js       loaders, toasts, modales, menú móvil, ayudas y pestañas
  messages.properties    todos los textos de la interfaz
```

## Rutas

La profundidad máxima es de tres niveles (zona / sección / detalle), como pide la consigna.

| Zona | Rutas | Responsable |
|---|---|---|
| Pública | `/`, `/galeria`, `/ayuda`, `/legal/privacidad`, `/legal/terminos` (implementadas); `/mapa`, `/ingresar`, `/registro` (en construcción) | Persona 1 (ingreso y registro con la 2) |
| Donante | `/donante`, `/donante/donaciones[/{id}]`, `/donante/entidades[/{id}]`, `/donante/seguimiento`, `/donante/incentivos` | Persona 4 |
| Beneficiaria | `/beneficiaria`, `/beneficiaria/necesidades[/{id}]`, `/beneficiaria/donaciones[/{id}]`, `/beneficiaria/seguimiento` | Persona 5 |
| Admin | `/admin/donaciones[/{id}]`, `/admin/asignaciones`, `/admin/donantes[/{id}]`, `/admin/entidades[/{id}]` | Persona 3 |
| Admin | `/admin/reportes` | Persona 4 |
| Admin | `/admin/camiones`, `/admin/seguimiento` | Persona 5 |
| Admin | `/admin` (panel) | A definir |

Todas las rutas ya navegan y muestran "Sección en construcción" con el menú del rol. Para agregar una sección que no está en el menú, se suma en `NavegacionService`.

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
   El tercer parámetro recibe los scripts propios de la vista: `~{::scripts}`, o `_` si no hay.
4. **Formularios.** Seguí el patrón POST-redirect-GET.
   - Si la operación sale bien: `Toasts.exito(redirectAttributes, "...")` y redirect.
   - Si el backend la rechaza, atrapá `BackendRechazoException`, llamá a `e.volcarEn(bindingResult)` y volvé a mostrar el formulario. Los errores quedan en sus campos.
   - Los errores de conexión y los 404 los maneja `WebExceptionHandler`.
5. **Fechas.** Usá `${@fechas.fechaHora(x)}`, `${@fechas.fecha(x)}` o `${@fechas.mes(x)}`. Las APIs devuelven UTC y el helper lo pasa a la hora de Argentina.
6. **Textos.** Van en `messages.properties`, con el prefijo de tu sección (`admin.donaciones.*`, `donante.*`, etc.).
7. **Componentes.** Copiá el HTML del catálogo (`/dev/componentes`). No escribas colores, tamaños ni espaciados sueltos: si falta algo, se agrega como token o componente nuevo y se muestra en el catálogo.
8. **Tests.** Probá cada controller con MockMvc, y cada método nuevo de un cliente con `MockRestServiceServer`. Hay ejemplos en `src/test`.

### Checklist de UI de la consigna (vale para todas las pantallas)
- [ ] Cada control tiene `<label>`. Las ayudas y errores se asocian con `aria-describedby`, y los campos con error llevan `aria-invalid="true"`.
- [ ] Todo lo clickeable mide al menos 48×48 px. Las clases `.boton`, `.control` y `.menu-lateral__enlace` ya lo cumplen.
- [ ] El foco es visible. No usar `outline: none` sin reemplazo.
- [ ] Cada acción muestra su resultado (toast o banner) y cada error se explica.
- [ ] Operaciones de más de 300 ms: formularios y enlaces muestran el loader solos; para un `fetch` se usa `DonaTrack.conLoader(...)`.
- [ ] La pantalla funciona en celular (menos de 600 px), tablet y escritorio, sin superposiciones.
- [ ] El estado de una donación no se indica solo con color: lleva texto siempre.
- [ ] La sección se alcanza en tres clics o menos desde el panel.

---

## Trabajo pendiente

### Persona 1 (cliente web, diseño compartido y acceso público)
Fases ya terminadas: módulo base, sistema de diseño, layouts y navegación, clientes REST, manejo de errores y páginas públicas.

- [x] **Fase 5. Páginas públicas.** Landing (`/`), galería (`/galeria`), ayuda (`/ayuda`) y borradores de privacidad y términos (`/legal/*`).
  - La fuente de datos es provisoria: `FuentePublicaApis` arma la landing y la galería a partir de los listados completos de `GET /api/entregas`, `/api/donaciones`, `/api/entidades` y `/api/donantes`, en cada visita. Las vistas nunca reciben direcciones, camiones ni datos personales. Cuando existan los endpoints públicos de las Personas 3 y 5, se reemplaza esa clase y las vistas no cambian.
  - "Último mes" son los últimos 30 días, sobre la fecha y hora de entrega de Logística.
  - Si un backend no responde, la landing sigue funcionando: muestra "—" en las cifras afectadas y un aviso en lugar de las entregas. La galería, en cambio, muestra la página 503.
  - Las fotos son URLs cargadas por las entidades: solo se aceptan `http(s)`, y si una no carga se muestra una imagen neutra.
- [ ] **Fase 6. Componente de mapa.**
  - Leaflet con tiles de OpenStreetMap y una lista accesible equivalente.
  - `/mapa` público con datos de ejemplo hasta que se valide D-042.
  - Contrato de marcadores, para las Personas 4 y 5: `{lat, lng, titulo, detalle, actualizadoEn, tipo}`.
- [ ] **Fase 7. Tema de login de Keycloak** (`keycloak/themes/donatrack`) con los tokens de diseño. Lo monta la Persona 2. `/registro` queda como página de entrada al registro.
- [ ] **Fase 8.** Sumar `servicio-web` a `run-servicios`/`stop-servicios` (`.sh` y `.ps1`), a la guía de arranque y al README raíz.
- [ ] **Fase 9.** Revisión de accesibilidad (Lighthouse/axe y teclado), suite completa en verde y D-043 en `decisiones.md` (rutas, fuente de datos provisoria, contrato del mapa y tema de Keycloak).
- [ ] Probar en el navegador el JS interactivo del catálogo: toasts, modal, ayudas, pestañas y loader.
- [ ] Diagrama de componentes en `diagramas/`. Lo pide la Entrega 7, pero conviene empezarlo ahora.

### Persona 2 (autenticación, SSO, social login y permisos)
Lo que ya existe: la rama `iam` (Keycloak, realm `donatrack`, roles `ADMIN`/`DONANTE`/`BENEFICIARIA`, D-039 y D-040), todavía sin mergear.

- [ ] Mergear `iam` en `main`. La bitácora de `feat/servicio-web` usa D-041 y D-042 y no choca con D-039 y D-040.
- [ ] Crear en el realm el cliente OIDC confidencial `servicio-web`, con redirect a `http://localhost:8085/login/oauth2/code/keycloak`, y al menos un proveedor de social login.
- [ ] En `servicio-web`:
  - Agregar `spring-boot-starter-oauth2-client`, `spring-boot-starter-security` y `thymeleaf-extras-springsecurity6`.
  - Proteger `/admin/**` (ADMIN), `/donante/**` (DONANTE) y `/beneficiaria/**` (BENEFICIARIA). El resto es público.
  - `/ingresar` debe redirigir a `/oauth2/authorization/keycloak`. El layout ya tiene el cierre de sesión por `POST /logout`, con CSRF automático.
  - Con la sesión vencida, volver al login y mostrar un toast explicando por qué.
- [ ] Implementar `PropagadorCredenciales` como un `@Component` que agregue `Authorization: Bearer <access token del usuario>`. Reemplaza al vacío sin tocar los clientes.
- [ ] En `NavegacionModelAdvice`, tomar el rol del usuario autenticado en lugar de deducirlo de la ruta, y agregar `usuarioActual` (con `nombre`) al modelo. El encabezado ya lo muestra.
- [ ] Configurar las APIs como resource server, validando los tokens de Keycloak. Cada operación tiene que quedar restringida a los recursos propios del usuario, no alcanza con ocultar botones.
- [ ] Mantener funcionando las llamadas entre servicios (Donaciones → Logística, eventos, colas) con credenciales de servicio.
- [ ] Definir el vínculo entre la cuenta de Keycloak y el Donante o la EntidadBeneficiaria. Propuesta: el usuario se registra en Keycloak, al volver completa su perfil en `/registro` y ese formulario crea el registro del dominio. Acordarlo con la Persona 1 para la vista y con la Persona 3 para las cuentas creadas por administración o importadas por CSV.
- [ ] Montar el tema `keycloak/themes/donatrack` (fase 7 de la Persona 1) en el compose y activar `loginTheme` en el realm.
- [ ] Tests de permisos: un rol que entra a la zona de otro rol, un acceso a un recurso ajeno, y una sesión vencida.

### Persona 3 (administración y gestión de Donaciones)
- [ ] Pantallas `/admin/donaciones[/{id}]`, `/admin/asignaciones`, `/admin/donantes[/{id}]` y `/admin/entidades[/{id}]`:
  - altas, modificaciones, desactivación y reactivación;
  - importación CSV con resultado, errores y estado (debe soportar más de 10.000 filas);
  - formulario de carga con varios ítems;
  - historial de estados;
  - matchmaking con las tres listas y la confirmación de la candidata;
  - marcar donaciones como vencidas.
- [ ] Agregar los métodos que falten en `DonacionesClient`.
- [ ] Agregar a la API los filtros y la paginación que necesiten las pantallas. Hoy `GET /api/donaciones` no filtra ni pagina, y `DonacionDTO` no tiene fecha.
- [ ] **Para la landing (D-041), acordarlo con la Persona 1:** un endpoint público de solo lectura con las donaciones destacadas del último mes (categoría, subcategoría, cantidad, entidad y fecha), sin datos personales de donantes.

### Persona 4 (portal del donante, Incentivos y reportes)
- [ ] Pantallas `/donante/**` y `/admin/reportes`:
  - panel del donante;
  - sus donaciones, con filtros por estado, categoría y subcategoría;
  - navegación por entidades beneficiarias;
  - métricas, misiones e insignias, con el cambio de visibilidad de insignias;
  - ranking mensual e historial de rankings.
- [ ] La sección de Incentivos es solo para donantes personas humanas (requerimiento 7): ocultarla y bloquearla para personas jurídicas.
- [ ] Agregar los métodos de `IncentivosClient`, que hoy está vacío.
- [ ] `/donante/seguimiento`: usar el componente de mapa (fase 6 de la Persona 1) con los datos de ubicación que expone la Persona 5.
- [ ] Usar las categorías de misiones del dominio (Colaborador, Sostenedor, Transformador). Los mockups dicen "Bronze" y "Donor", y eso quedó desactualizado.

### Persona 5 (portal beneficiario y operación logística)
- [ ] Pantallas `/beneficiaria/**`, `/admin/camiones` y `/admin/seguimiento`:
  - panel de la entidad;
  - necesidades recurrentes y extraordinarias;
  - donaciones asignadas y su estado;
  - confirmación de recepción con fotos;
  - ABM de camiones;
  - seguimiento de rutas y entregas.
- [ ] Carga de fotos: hoy `FotoEntrega` recibe una URL. Hay que resolver cómo un archivo que sube el navegador se convierte en esa URL.
- [ ] **Mapas (D-042, a validar con el equipo):**
  - Coordenadas de las sedes de las entidades, cargadas con un clic en el mapa, sin geocodificación.
  - Endpoint para reportar la ubicación del camión, por ejemplo `POST /api/camiones/{id}/ubicacion` desde el chofer o un simulador. Logística guarda latitud, longitud y fecha/hora y las deja disponibles para consulta, sin llamar a otros servicios.
  - Exponer esos datos con el contrato de marcadores de la fase 6.
- [ ] **Para la landing (D-041), acordarlo con la Persona 1:** un endpoint público de solo lectura con las entregas recientes que tienen foto (foto, fecha, entidad y donación asociada), sin dirección ni camión.
- [ ] Agregar los métodos que falten en `LogisticaClient`.

### Pendientes del equipo
- [ ] Validar D-042 (mapas). Afecta a las Personas 1, 4 y 5.
- [ ] Acordar los endpoints públicos de la landing (D-041). Afecta a las Personas 1, 3 y 5.
- [ ] Decidir si la campana de notificaciones de los mockups se implementa como panel dentro de la app o se saca.
- [ ] Decidir qué muestra el panel `/admin`.
- [ ] Revisar los borradores de [privacidad](src/main/resources/templates/publico/legal/privacidad.html) y [términos](src/main/resources/templates/publico/legal/terminos.html). Falta definir un medio de contacto real para ejercer los derechos de la Ley 25.326 (hoy dice "una persona administradora").
- [ ] Prueba integrada final: registro → ingreso → carga de donación → asignación → seguimiento → recepción → actualización de métricas.
