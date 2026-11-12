package ar.edu.utn.frba.dds.web.client.logistica;

import java.util.List;

/** Cuerpo de POST /api/rutas/{id}/paradas/{id}/recepcion: las fotos ya subidas, por su URL. */
public record ConfirmacionRecepcionRequest(List<Foto> fotos) {

    public record Foto(String url) {
    }

    public static ConfirmacionRecepcionRequest de(List<String> urls) {
        return new ConfirmacionRecepcionRequest(urls.stream().map(Foto::new).toList());
    }
}
