# Persona 5 — parte 4: fotos y confirmación de recepción

Se copia encima de la rama `feat/servicio-web` (después de las partes 1 a 3). Descomprimir en la raíz del repo.

## Qué incluye

**Logística**
- `POST /api/fotos` (multipart, parte `file`): guarda la foto en MongoDB con GridFS y devuelve `{id, url}`.
  Solo JPEG, PNG y WebP de hasta 5 MB; se verifica el contenido real del archivo.
- `GET /api/fotos/{id}`: sirve la imagen (con caché y cabeceras que impiden que se interprete como otra cosa).
- `POST /api/rutas/{idRuta}/paradas/{idParada}/recepcion`: confirma la recepción con 1 a 5 fotos (por URL). Exige ruta en curso y entregas en traslado.
- `Entrega` y `Parada` suman `confirmarEntregaConFotos` / `confirmarRecepcionConFotos` y `entregasEnTraslado`.
- `application.properties`: `logistica.fotos.max-bytes` y los límites de subida de Spring.
- `diagramas/der/logistica.txt`: documenta las colecciones de GridFS.

**Cliente web**
- `/beneficiaria/seguimiento/{idRuta}`: formulario para confirmar la recepción eligiendo de 1 a 5 fotos. Se ofrece desde el seguimiento
  cuando la ruta salió y hay entregas suyas en traslado.
- El detalle de una donación recibida (`/beneficiaria/donaciones/{id}`) muestra los enlaces a las fotos de su entrega.
- Las fotos que se suben aparecen en la galería pública sin cambios: la galería ya leía las URL de las entregas.

**Documentos:** `decisiones-persona5-borrador.md` (reemplaza al anterior: cinco entradas).

## Para que funcione en Render

`LOGISTICA_PUBLIC_BASE_URL` tiene que ser la URL pública de Logística (por ejemplo `https://tp-anual-dsi-grupo4-1.onrender.com`).
Con el valor por defecto las URL de las fotos apuntan a `localhost` y los navegadores de otras máquinas no las ven.

## Cómo probarlo

1. Con una ruta iniciada (ver `PERSONA5-parte3.md`), entrar a `/beneficiaria/seguimiento` y apretar «Confirmar recepción».
2. Elegir una o más imágenes y confirmar: la ruta pasa a finalizada si era la última entrega pendiente.
3. Abrir el detalle de esa donación: aparecen los enlaces a las fotos. Las mismas fotos salen en `/galeria`.
4. En Swagger de Logística: `POST /api/fotos` con un archivo, y luego `POST .../recepcion` con la URL devuelta.

## Cosas para saber

- La entidad no puede confirmar la parada de otra: el cliente web busca la parada entre los envíos de la entidad y no usa ningún dato del pedido.
- Si falla la confirmación después de subir las fotos, las fotos quedan en la base sin usarse.
- Los límites de Spring (6 MB por foto) son un tope para cortar abusos: la validación amigable (5 MB, hasta 5 fotos, solo imágenes) la hace el controlador.
- Archivos compartidos modificados: `Entrega.java` y `Parada.java` de Logística, `application.properties` de Logística y del cliente web,
  y `RutasClient.java`, `DonacionesAsignadasController.java`, `BeneficiariaWebTest.java` y los mensajes del cliente web. Si otra persona los tocó, quedarse con ambos cambios.
