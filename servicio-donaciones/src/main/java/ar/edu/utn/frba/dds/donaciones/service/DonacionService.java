package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.CambioEstado;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.GestorDonaciones;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.SolicitudDonacion;
import ar.edu.utn.frba.dds.donaciones.domain.lugares.Direccion;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.GestorDonantes;
import ar.edu.utn.frba.dds.donaciones.client.IncentivosClient;
import ar.edu.utn.frba.dds.donaciones.integracion.PublicadorEventosPort;
import ar.edu.utn.frba.dds.donaciones.dto.CambioEstadoDTO;
import ar.edu.utn.frba.dds.donaciones.dto.CargaDonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.CiudadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DireccionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionPendienteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.ItemDonadoDTO;
import ar.edu.utn.frba.dds.donaciones.dto.ProvinciaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.TimeStampDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.*;
import ar.edu.utn.frba.dds.donaciones.dto.ActualizarDonacionDTO;

@Service
public class DonacionService {

    private final GestorDonaciones gestorDonaciones;
    private final GestorDonantes gestorDonantes;
    private final PublicadorEventosPort publicadorEventos;
    private final IncentivosClient incentivosClient;

    public DonacionService(GestorDonaciones gestorDonaciones, GestorDonantes gestorDonantes,
                            PublicadorEventosPort publicadorEventos, IncentivosClient incentivosClient) {
        this.gestorDonaciones = gestorDonaciones;
        this.gestorDonantes = gestorDonantes;
        this.publicadorEventos = publicadorEventos;
        this.incentivosClient = incentivosClient;
    }

    /**
     * Carga única de bienes: arma una SolicitudDonacion con todos los ítems
     * recibidos y deja que segmentar() la parta en una Donacion por
     * subcategoría. Reemplaza el alta directa de un único ítem que había
     * antes, que ni siquiera aceptaba categoría/subcategoría.
     */
    public List<DonacionDTO> crear(CargaDonacionDTO dto) {
        Donante donante = gestorDonantes.buscarPorId(dto.getDonanteId());

        SolicitudDonacion solicitud = new SolicitudDonacion(dto.getDescripcion());
        solicitud.setDonante(donante);

        for (ItemDonadoDTO itemDto : dto.getItems()) {
            solicitud.agregarItem(convertirItemDominio(itemDto));
        }

        List<Donacion> segmentadas = solicitud.segmentar();
        gestorDonaciones.registrarSolicitud(solicitud);
        donante.registrarActividad();
        return segmentadas.stream()
                .map(gestorDonaciones::registrarDonacion)
                .map(this::convertirADTO)
                .toList();
    }

    private ItemDonado convertirItemDominio(ItemDonadoDTO dto) {
        if (dto == null) throw new IllegalArgumentException("El item es obligatorio");
        return new ItemDonado(null, dto.getDescripcion(), dto.getCategoria(), dto.getSubcategoria(),
                dto.getUnidadMedida(), dto.getCantidad(), dto.getFoto(),
                dto.getFechaVencimiento() == null ? null : new Perecedero(dto.getFechaVencimiento()),
                dto.getCondicion() == null ? null : new ConEstado(dto.getCondicion()),
                dto.getPesoKg(), dto.getVolumenM3(), dto.getAlturaM());
    }

    private ItemDonadoDTO convertirItemADTO(ItemDonado item) {
        ItemDonadoDTO dto = new ItemDonadoDTO();
        dto.setDescripcion(item.getDescripcion());
        dto.setCategoria(item.getCategoria());
        dto.setSubcategoria(item.getSubcategoria());
        dto.setUnidadMedida(item.getUnidadMedida());
        dto.setCantidad(item.getCantidad());
        dto.setFoto(item.getFoto());
        dto.setCondicion(item.condicion());
        dto.setFechaVencimiento(item.vencimiento());
        dto.setPesoKg(item.getPesoKg());
        dto.setVolumenM3(item.getVolumenM3());
        dto.setAlturaM(item.getAlturaM());
        return dto;
    }

    public List<DonacionDTO> obtenerTodas() {
        return gestorDonaciones.getDonaciones().stream().map(this::convertirADTO).toList();
    }

    public DonacionDTO obtenerPorId(Long id) {
        return convertirADTO(obtenerDominioPorId(id));
    }

    public void eliminar(Long id) {
        gestorDonaciones.eliminar(id);
    }

    /** Reemplazo validado y atómico del grupo; conserva identidad, origen e historial. */
    public DonacionDTO actualizar(Long id, ActualizarDonacionDTO dto) {
        Donacion donacion = obtenerDominioPorId(id);
        List<ItemDonado> nuevos = dto.getItems().stream().map(this::convertirItemDominio).toList();
        List<ItemDonado> anteriores = donacion.getItems();
        donacion.reemplazarItems(nuevos);
        gestorDonaciones.getDeposito().reemplazarItems(anteriores, nuevos);
        return convertirADTO(donacion);
    }

    public DonacionDTO cambiarEstado(Long id, CambioEstadoDTO dto) {
        Donacion donacion = obtenerDominioPorId(id);
        donacion.cambiarEstado(dto.getNuevoEstado(), dto.getJustificacion());

        DonacionDTO resultado = convertirADTO(donacion);
        // Se publica el dominio, no el DTO, porque RabbitPublicadorEventos
        // necesita resolver la EntidadBeneficiaria/Donante para el contacto.
        // origenEvento permite que un cambio de estado disparado por un evento
        // de Logística (ver EventosLogisticaScheduler) publique el tipoEvento
        // real en vez del genérico, sin abrir un segundo camino de publicación.
        String tipoEvento = dto.getOrigenEvento() != null ? dto.getOrigenEvento() : "CAMBIO_ESTADO_DONACION";
        publicadorEventos.publicar(tipoEvento, donacion);

        // Integración con Incentivos: una donación entregada es la señal de
        // "actividad de donación" más confiable que tenemos (sabemos con
        // certeza categoría, cantidad y que fue exitosa) -- dispara el
        // cálculo de progreso de misiones del donante.
        if (dto.getNuevoEstado() == EstadoTrack.ENTREGADA) {
            incentivosClient.registrarActividadDonacion(donacion);
        }
        return resultado;
    }

    /**
     * Confirma la asignación de una donación a una entidad beneficiaria
     * (resultado del matchmaking). Antes esto lo hacía MatchmakingService
     * mutando el dominio directo, sin pasar por acá — por eso el evento
     * hacia Notificaciones nunca se disparaba en este camino, a diferencia
     * de cambiarEstado(). Unificado en un solo lugar.
     *
     * La entidad elegida tiene que salir del resultado de los algoritmos
     * (GET /{id}/candidatas), no puede ser cualquiera -- si todavía no se
     * corrió matchmaking, 409; si la entidad no apareció en ninguna de las
     * 3 listas, 400. Si además la entidad tiene una necesidad pendiente de
     * la misma subcategoría/unidad, se vincula y se descuenta la cantidad
     * contra ella -- sin esto ninguna Necesidad llegaba jamás a
     * satisfecha(), sin importar cuántas donaciones se le asignaran.
     */
    public DonacionDTO confirmarAsignacion(Long id, EntidadBeneficiaria entidad) {
        Donacion donacion = obtenerDominioPorId(id);
        if (donacion.getPesoKg() == null || donacion.getVolumenM3() == null || donacion.getAlturaM() == null)
            throw new IllegalArgumentException("Complete pesoKg, volumenM3 y alturaM de todos los items antes de asignar");
        if (donacion.getResultadoMatchmaking() == null)
            throw new IllegalStateException("Ejecute el matchmaking de esta donación (GET /api/donaciones/" + id + "/candidatas) antes de asignarla");
        if (!donacion.candidatasPropuestas().contains(entidad))
            throw new IllegalArgumentException("La entidad " + entidad.getId() + " no fue propuesta por los algoritmos de asignación para esta donación");

        donacion.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA, null);
        donacion.setEntidadBeneficiaria(entidad);

        entidad.necesidadesPendientes().stream()
                .filter(n -> n.getSubcategoria() == donacion.getSubcategoria()
                        && n.getUnidadMedida() == donacion.getUnidadMedida())
                .findFirst()
                .ifPresent(necesidad -> {
                    donacion.setNecesidadAsignada(necesidad);
                    necesidad.recibir(donacion.getCantidadAsignada());
                });

        DonacionDTO resultado = convertirADTO(donacion);
        publicadorEventos.publicar("CAMBIO_ESTADO_DONACION", donacion);
        return resultado;
    }

    public List<TimeStampDTO> obtenerHistorial(Long id) {
        return obtenerDominioPorId(id).getHistorialEstados().stream()
                .map(this::convertirCambioEstadoADTO)
                .toList();
    }

    public List<Donacion> obtenerDonacionesEnDeposito() {
        return gestorDonaciones.getDonaciones().stream()
                .filter(d -> d.getEstadoActual() == EstadoTrack.EN_DEPOSITO)
                .toList();
    }

    public List<DonacionPendienteDTO> obtenerPendientes(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page debe ser >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("size debe estar entre 1 y 100");
        }

        int skip = page * size;

        return gestorDonaciones.getDonaciones().stream()
                .filter(this::estaPendienteDePlanificacion)
                .skip(skip)
                .limit(size)
                .map(this::convertirAPendienteDTO)
                .toList();
    }

    private boolean estaPendienteDePlanificacion(Donacion donacion) {
        return donacion.getEstadoActual() == EstadoTrack.ASIGNACION_REALIZADA
                && donacion.getEntidadBeneficiaria() != null;
    }

    private DonacionPendienteDTO convertirAPendienteDTO(Donacion donacion) {
        DonacionPendienteDTO dto = new DonacionPendienteDTO();
        dto.setIdDonacion(donacion.getId().intValue());
        dto.setEntidadBeneficiariaAsociadaID(
                donacion.getEntidadBeneficiaria().getId().intValue());
        dto.setDireccionDestino(
                convertirDireccionADTO(donacion.getEntidadBeneficiaria().getDireccion()));

        dto.setPesoKG(donacion.getPesoKg());
        dto.setVolumenM3(donacion.getVolumenM3());
        dto.setAlturaM(donacion.getAlturaM());
        return dto;
    }

    private DireccionDTO convertirDireccionADTO(Direccion direccion) {
        if (direccion == null) {
            return null;
        }

        DireccionDTO dto = new DireccionDTO();
        dto.setCalle(direccion.getCalle());
        dto.setNumero(direccion.getNumero());

        if (direccion.getCiudad() != null) {
            CiudadDTO ciudadDTO = new CiudadDTO();
            ciudadDTO.setNombre(direccion.getCiudad().getNombre());

            if (direccion.getCiudad().getProvincia() != null) {
                ProvinciaDTO provinciaDTO = new ProvinciaDTO();
                provinciaDTO.setNombre(direccion.getCiudad().getProvincia().getNombre());
                ciudadDTO.setProvincia(provinciaDTO);
            }

            dto.setCiudad(ciudadDTO);
        }

        return dto;
    }

    public Donacion obtenerDominioPorId(Long id) {
        return gestorDonaciones.buscarPorId(id);
    }

    private DonacionDTO convertirADTO(Donacion donacion) {
        DonacionDTO dto = new DonacionDTO();
        dto.setId(donacion.getId());
        if (donacion.getDonante() != null) {
            dto.setDonanteId(donacion.getDonante().getId());
        }
        dto.setDescripcionItem(donacion.getItems().stream().map(ItemDonado::getDescripcion).collect(java.util.stream.Collectors.joining("; ")));
        dto.setItems(donacion.getItems().stream().map(this::convertirItemADTO).toList());
        dto.setCategoria(donacion.getCategoria());
        dto.setSubcategoria(donacion.getSubcategoria());
        dto.setUnidadMedida(donacion.getUnidadMedida());
        dto.setSolicitudOrigenId(donacion.getSolicitudOrigen() == null ? null : donacion.getSolicitudOrigen().getId());
        dto.setCantidadAsignada(donacion.getCantidadAsignada());
        dto.setEstadoActual(donacion.getEstadoActual());

        if (donacion.getEntidadBeneficiaria() != null) {
            dto.setEntidadBeneficiariaId(donacion.getEntidadBeneficiaria().getId());
        }
        if (donacion.getNecesidadAsignada() != null) {
            dto.setNecesidadId(donacion.getNecesidadAsignada().getId());
        }

        dto.setPesoKg(donacion.getPesoKg());
        dto.setVolumenM3(donacion.getVolumenM3());
        dto.setAlturaM(donacion.getAlturaM());

        return dto;
    }

    private TimeStampDTO convertirCambioEstadoADTO(CambioEstado cambioEstado) {
        TimeStampDTO dto = new TimeStampDTO();
        dto.setEstado(cambioEstado.getEstadoNuevo());
        dto.setFecha(cambioEstado.getFechaCambio());
        dto.setJustificacion(cambioEstado.getJustificacion());
        return dto;
    }
}
