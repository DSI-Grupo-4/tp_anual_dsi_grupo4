package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.AtributoDefinicion;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.AtributoValor;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.TipoDato;
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
import java.util.Map;

@Service
public class DonacionService {

    private final GestorDonaciones gestorDonaciones;
    private final GestorDonantes gestorDonantes;
    private final PublicadorEventosPort publicadorEventos;

    public DonacionService(GestorDonaciones gestorDonaciones, GestorDonantes gestorDonantes,
                            PublicadorEventosPort publicadorEventos) {
        this.gestorDonaciones = gestorDonaciones;
        this.gestorDonantes = gestorDonantes;
        this.publicadorEventos = publicadorEventos;
    }

    /**
     * Carga única de bienes (consigna, Entrega 1): arma una SolicitudDonacion
     * con todos los ítems recibidos y deja que segmentar() la parta en una
     * Donacion por subcategoría. Reemplaza el alta directa de un único ítem
     * que había antes, que ni siquiera aceptaba categoría/subcategoría.
     */
    public List<DonacionDTO> crear(CargaDonacionDTO dto) {
        Donante donante = gestorDonantes.buscarPorId(dto.getDonanteId());

        SolicitudDonacion solicitud = new SolicitudDonacion(dto.getDescripcion());
        solicitud.setDonante(donante);

        for (ItemDonadoDTO itemDto : dto.getItems()) {
            solicitud.agregarItem(convertirItemDominio(itemDto));
        }

        return solicitud.segmentar().stream()
                .map(gestorDonaciones::registrarDonacion)
                .map(this::convertirADTO)
                .toList();
    }

    private ItemDonado convertirItemDominio(ItemDonadoDTO dto) {
        Subcategoria subcategoria = gestorDonaciones.getDeposito()
                .buscarSubcategoria(dto.getSubcategoria())
                .orElseGet(() -> new Subcategoria(dto.getSubcategoria()));

        ItemDonado item = new ItemDonado(
                null,
                dto.getDescripcion(),
                subcategoria,
                dto.getCantidad(),
                dto.getFoto()
        );
        item.setPesoKg(dto.getPesoKg());
        item.setVolumenM3(dto.getVolumenM3());
        item.setAlturaM(dto.getAlturaM());

        if (dto.getValoresAtributos() != null) {
            for (Map.Entry<String, String> valor : dto.getValoresAtributos().entrySet()) {
                AtributoDefinicion definicion = subcategoria.getAtributos().stream()
                        .filter(a -> a.getNombre().equalsIgnoreCase(valor.getKey()))
                        .findFirst()
                        .orElseGet(() -> new AtributoDefinicion(valor.getKey(), TipoDato.TEXTO, false));
                item.agregarValorAtributo(new AtributoValor(definicion, valor.getValue()));
            }
        }

        return item;
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

    /**
     * A diferencia de la versión anterior (que delegaba a crear() y perdía
     * el historial de estados), esto muta la donación existente: solo
     * cantidad/descripción/dimensiones del ítem. Estado, entidad y necesidad
     * cambian por sus propios endpoints (/estado, /asignar).
     */
    public DonacionDTO actualizar(Long id, DonacionDTO dto) {
        Donacion donacion = obtenerDominioPorId(id);
        donacion.setCantidadAsignada(dto.getCantidadAsignada());

        ItemDonado item = donacion.getItemDonado();
        item.setDescripcion(dto.getDescripcionItem());
        item.setPesoKg(dto.getPesoKg());
        item.setVolumenM3(dto.getVolumenM3());
        item.setAlturaM(dto.getAlturaM());

        return convertirADTO(donacion);
    }

    public DonacionDTO cambiarEstado(Long id, CambioEstadoDTO dto) {
        Donacion donacion = obtenerDominioPorId(id);
        donacion.cambiarEstado(dto.getNuevoEstado(), dto.getJustificacion());

        DonacionDTO resultado = convertirADTO(donacion);
        // RF-3: se publica el dominio, no el DTO, porque RabbitPublicadorEventos
        // necesita resolver la EntidadBeneficiaria/Donante para el contacto.
        // origenEvento permite que un cambio de estado disparado por un evento
        // de Logística (ver EventosLogisticaScheduler) publique el tipoEvento
        // real en vez del genérico, sin abrir un segundo camino de publicación.
        String tipoEvento = dto.getOrigenEvento() != null ? dto.getOrigenEvento() : "CAMBIO_ESTADO_DONACION";
        publicadorEventos.publicar(tipoEvento, donacion);
        return resultado;
    }

    /**
     * Confirma la asignación de una donación a una entidad beneficiaria
     * (resultado del matchmaking). Antes esto lo hacía MatchmakingService
     * mutando el dominio directo, sin pasar por acá — por eso el evento
     * hacia Notificaciones nunca se disparaba en este camino, a diferencia
     * de cambiarEstado(). Unificado en un solo lugar.
     */
    public DonacionDTO confirmarAsignacion(Long id, EntidadBeneficiaria entidad) {
        Donacion donacion = obtenerDominioPorId(id);
        donacion.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA, null);
        donacion.setEntidadBeneficiaria(entidad);

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

        ItemDonado item = donacion.getItemDonado();
        if (item != null) {
            dto.setPesoKG(item.getPesoKg());
            dto.setVolumenM3(item.getVolumenM3());
            dto.setAlturaM(item.getAlturaM());
        }
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
        dto.setDescripcionItem(donacion.getItemDonado().getDescripcion());
        dto.setCantidadAsignada(donacion.getCantidadAsignada());
        dto.setEstadoActual(donacion.getEstadoActual());

        if (donacion.getEntidadBeneficiaria() != null) {
            dto.setEntidadBeneficiariaId(donacion.getEntidadBeneficiaria().getId());
        }
        if (donacion.getNecesidadAsignada() != null) {
            dto.setNecesidadId(donacion.getNecesidadAsignada().getId());
        }

        ItemDonado item = donacion.getItemDonado();
        dto.setPesoKg(item.getPesoKg());
        dto.setVolumenM3(item.getVolumenM3());
        dto.setAlturaM(item.getAlturaM());

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
