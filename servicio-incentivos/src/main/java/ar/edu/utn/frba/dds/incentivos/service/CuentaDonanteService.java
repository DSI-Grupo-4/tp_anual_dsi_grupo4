package ar.edu.utn.frba.dds.incentivos.service;

import ar.edu.utn.frba.dds.incentivos.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.incentivos.domain.misiones.Mision;
import ar.edu.utn.frba.dds.incentivos.domain.personas.CategoriasDonante;
import ar.edu.utn.frba.dds.incentivos.domain.personas.CuentaDonante;
import ar.edu.utn.frba.dds.incentivos.domain.personas.Donante;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CuentaDonanteService {

    private final Map<Integer, Donacion> donacionesPorDonante = new ConcurrentHashMap<>();

    public Donacion obtenerOCrear(int donanteId) {
        return donacionesPorDonante.computeIfAbsent(donanteId, id -> {
            Donacion d = new Donacion();
            d.setId(id);
            d.setCategoriaDonada(CategoriasDonante.COLABORADOR);
            d.setIdDonante(new CuentaDonante(new ArrayList<>()));
            return d;
        });
    }

    public Donacion registrarDonacion(int donanteId, int cantItems, int cantTypes, String fecha) {
        Donacion cuenta = obtenerOCrear(donanteId);
        cuenta.setTotalDonaciones(cuenta.getTotalDonaciones() + cantItems);
        cuenta.setRachaActual(cuenta.getRachaActual() + 1);
        cuenta.setFechaUltimaDonaion(LocalDateTime.now());
        cuenta.completarMision();
        return cuenta;
    }

    public Donacion registrarEntrega(int donanteId, String beneficiario) {
        Donacion cuenta = obtenerOCrear(donanteId);
        cuenta.setTotalOrganizaciones(cuenta.getTotalOrganizaciones() + 1);
        cuenta.completarMision();
        return cuenta;
    }

    public void completarMision(int donanteId, int misionId) {
        Donacion cuenta = obtenerOCrear(donanteId);
        Mision mision = cuenta.getMisionActual();
        if (mision != null && misionId == mision.getIdMision()) {
            mision.setEstaCompleta(true);
            cuenta.completarMision();
        }
    }

    public void subirCategoria(int donanteId, CategoriasDonante nuevaCategoria) {
        Donacion cuenta = obtenerOCrear(donanteId);
        cuenta.setCategoriaDonada(nuevaCategoria);
    }

    public List<Donacion> obtenerTodas() {
        return new ArrayList<>(donacionesPorDonante.values());
    }

    public Optional<Donacion> buscarPorId(int id) {
        return Optional.ofNullable(donacionesPorDonante.get(id));
    }
}
