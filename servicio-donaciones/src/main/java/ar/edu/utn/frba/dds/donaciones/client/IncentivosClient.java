package ar.edu.utn.frba.dds.donaciones.client;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Categoria;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.MedioContacto;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Persona;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaHumana;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Optional;

/** Guarda actividad de alta/entrega en disco y reintenta REST en segundo plano. */
@Component
public class IncentivosClient {

    private static final Logger logger = LoggerFactory.getLogger(IncentivosClient.class);

    private final RestClient restClient;

    public IncentivosClient(@Value("${incentivos.base-url:http://localhost:8081}") String baseUrl) {
        var transporte = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        transporte.setConnectTimeout(3000); transporte.setReadTimeout(5000);
        this.restClient = RestClient.builder().requestFactory(transporte).baseUrl(baseUrl).build();
    }

    /** Sincroniza identidad y contacto sin contabilizar una donación ni completar misiones. */
    public void registrarDonante(Donante donante) {
        encolarPerfil(donante);
        intentarPerfil(pendientes.resolve(String.format("perfil-%020d.json", donante.getId())));
    }

    public synchronized void encolarPerfil(Donante donante) {
        var medio = donante.getPersona().medioPreferido();
        var perfil = new java.util.LinkedHashMap<String,String>();
        perfil.put("nombre", nombreDe(donante.getPersona()));
        perfil.put("medioContactoPreferido", medio.map(m -> mapearMedio(m.getTipo())).orElse(null));
        perfil.put("contactoPreferido", medio.map(MedioContacto::getValor).orElse(null));
        try {
            java.nio.file.Files.createDirectories(pendientes);
            var temporal = java.nio.file.Files.createTempFile(pendientes, "perfil-", ".tmp");
            mapper.writeValue(temporal.toFile(), java.util.Map.of("donanteId", donante.getId(), "perfil", perfil));
            java.nio.file.Files.move(temporal, pendientes.resolve(String.format("perfil-%020d.json", donante.getId())),
                java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (java.io.IOException e) { throw new IllegalStateException("No se pudo guardar el perfil pendiente de Incentivos", e); }
    }

    private synchronized void intentarPerfil(java.nio.file.Path archivo) {
        if (!java.nio.file.Files.exists(archivo)) return;
        try {
            var datos = mapper.readTree(archivo.toFile());
            restClient.put().uri("/api/donantes/{id}/perfil", datos.get("donanteId").asLong())
                .body(datos.get("perfil")).retrieve().toBodilessEntity();
            java.nio.file.Files.delete(archivo);
        } catch (Exception e) { logger.warn("Perfil pendiente de Incentivos {}: {}", archivo.getFileName(), e.getMessage()); }
    }

    public void registrarActividadDonacion(Donacion donacion) {
        Donante donante = donacion.getDonante();
        if (donante == null) {
            logger.warn("Donación {} sin donante asociado -- no se notifica actividad a Incentivos.",
                    donacion.getId());
            return;
        }
        ActividadDonacionRequest solicitud = construirRequest(donacion, donante);
        String nombre = String.format("%020d-%d.json", donacion.getId(), solicitud.donacionExitosa() ? 1 : 0);
        try {
            java.nio.file.Files.createDirectories(pendientes);
            java.nio.file.Path temporal = java.nio.file.Files.createTempFile(pendientes, "actividad-", ".tmp");
            mapper.writeValue(temporal.toFile(), java.util.Map.of("donanteId", donante.getId(), "solicitud", solicitud));
            java.nio.file.Files.move(temporal, pendientes.resolve(nombre), java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (java.io.IOException e) { throw new IllegalStateException("No se pudo guardar la actividad pendiente de Incentivos", e); }
    }

    @org.springframework.beans.factory.annotation.Value("${incentivos.pendientes-dir:.datos/donaciones/incentivos-pendientes}")
    private java.nio.file.Path pendientes = java.nio.file.Path.of(".datos/donaciones/incentivos-pendientes");
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules();

    @org.springframework.scheduling.annotation.Scheduled(fixedDelayString = "${incentivos.reintento-ms:5000}")
    public synchronized void enviarPendientes() {
        if (!java.nio.file.Files.exists(pendientes)) return;
        try (var archivos = java.nio.file.Files.list(pendientes)) {
            var lista = archivos.filter(p -> p.toString().endsWith(".json")).sorted().toList();
            for (var archivo : lista) if (archivo.getFileName().toString().startsWith("perfil-")) intentarPerfil(archivo);
            for (var archivo : lista) {
                if (archivo.getFileName().toString().startsWith("perfil-")) continue;
                try {
                    var datos = mapper.readTree(archivo.toFile());
                    restClient.post().uri("/api/donantes/{id}/actividad-donacion", datos.get("donanteId").asLong())
                        .body(datos.get("solicitud")).retrieve().toBodilessEntity();
                    java.nio.file.Files.delete(archivo);
                } catch (Exception e) { logger.warn("Actividad pendiente de Incentivos {}: {}", archivo.getFileName(), e.getMessage()); break; }
            }
        } catch (java.io.IOException e) { throw new IllegalStateException("No se pudo leer la bandeja de Incentivos", e); }
    }

    private ActividadDonacionRequest construirRequest(Donacion donacion, Donante donante) {
        Persona persona = donante.getPersona();
        Optional<MedioContacto> medio = persona != null ? persona.medioPreferido() : Optional.empty();
        EntidadBeneficiaria entidad = donacion.getEntidadBeneficiaria();

        return new ActividadDonacionRequest(
                donacion.getId(),
                donacion.getSolicitudOrigen() != null ? donacion.getSolicitudOrigen().getFechaRegistro().toLocalDate() : LocalDate.now(),
                categoriaBienDe(donacion),
                donacion.getCantidadAsignada(),
                donacion.getEstadoActual() == EstadoTrack.ENTREGADA,
                entidad != null ? entidad.getId() : null,
                entidad != null ? entidad.getEntidad().getRazonSocial() : null,
                nombreDe(persona),
                medio.map(m -> mapearMedio(m.getTipo())).orElse(null),
                medio.map(MedioContacto::getValor).orElse(null));
    }

    private String categoriaBienDe(Donacion donacion) {
        return donacion.getCategoria().name();
    }

    private String nombreDe(Persona persona) {
        if (persona instanceof PersonaHumana humana) {
            return humana.nombreCompleto();
        }
        if (persona instanceof PersonaJuridica juridica) {
            return juridica.getRazonSocial();
        }
        return null;
    }

    private String mapearMedio(TipoContacto tipo) {
        return switch (tipo) {
            case EMAIL -> "EMAIL";
            case TELEFONO -> "SMS";
            case WHATSAPP -> "WHATSAPP";
        };
    }

    private record ActividadDonacionRequest(
            Long donacionId,
            LocalDate fecha,
            String categoriaNombre,
            java.math.BigDecimal cantidadBienes,
            boolean donacionExitosa,
            Long beneficiarioId,
            String beneficiarioNombre,
            String donanteNombre,
            String donanteMedioContacto,
            String donanteContacto) {
    }
}
