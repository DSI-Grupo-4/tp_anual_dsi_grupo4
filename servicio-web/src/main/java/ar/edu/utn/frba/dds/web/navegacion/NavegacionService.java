package ar.edu.utn.frba.dds.web.navegacion;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Define el menú de cada zona privada y arma las migas de pan.
 * Las rutas respetan la profundidad máxima de tres niveles que pide la consigna:
 * zona / sección / detalle (por ejemplo, /admin/donaciones/15).
 */
@Service
public class NavegacionService {

    private final Map<Rol, List<ItemMenu>> menus = new EnumMap<>(Rol.class);

    public NavegacionService() {
        menus.put(Rol.DONANTE, List.of(
                ItemMenu.de(Rol.DONANTE, "nav.donante.panel", "", "inicio"),
                ItemMenu.de(Rol.DONANTE, "nav.donante.donaciones", "donaciones", "corazon"),
                ItemMenu.de(Rol.DONANTE, "nav.donante.entidades", "entidades", "edificio"),
                ItemMenu.de(Rol.DONANTE, "nav.donante.seguimiento", "seguimiento", "ubicacion"),
                ItemMenu.de(Rol.DONANTE, "nav.donante.incentivos", "incentivos", "premio")));
        menus.put(Rol.BENEFICIARIA, List.of(
                ItemMenu.de(Rol.BENEFICIARIA, "nav.beneficiaria.panel", "", "inicio"),
                ItemMenu.de(Rol.BENEFICIARIA, "nav.beneficiaria.necesidades", "necesidades", "portapapeles"),
                ItemMenu.de(Rol.BENEFICIARIA, "nav.beneficiaria.donaciones", "donaciones", "caja"),
                ItemMenu.de(Rol.BENEFICIARIA, "nav.beneficiaria.seguimiento", "seguimiento", "ubicacion")));
        menus.put(Rol.ADMIN, List.of(
                ItemMenu.de(Rol.ADMIN, "nav.admin.panel", "", "inicio"),
                ItemMenu.de(Rol.ADMIN, "nav.admin.donaciones", "donaciones", "corazon"),
                ItemMenu.de(Rol.ADMIN, "nav.admin.asignaciones", "asignaciones", "asignar"),
                ItemMenu.de(Rol.ADMIN, "nav.admin.donantes", "donantes", "usuarios"),
                ItemMenu.de(Rol.ADMIN, "nav.admin.entidades", "entidades", "edificio"),
                ItemMenu.de(Rol.ADMIN, "nav.admin.camiones", "camiones", "camion"),
                ItemMenu.de(Rol.ADMIN, "nav.admin.seguimiento", "seguimiento", "ubicacion"),
                ItemMenu.de(Rol.ADMIN, "nav.admin.reportes", "reportes", "grafico")));
    }

    public List<ItemMenu> menu(Rol rol) {
        return menus.get(rol);
    }

    public boolean existeSeccion(Rol rol, String seccion) {
        return menus.get(rol).stream().anyMatch(i -> i.seccion().equals(seccion));
    }

    public Optional<ItemMenu> itemActivo(Rol rol, String ruta) {
        return menus.get(rol).stream().filter(i -> i.contiene(ruta)).findFirst();
    }

    /** Migas por defecto: panel de la zona › sección. Un controller puede reemplazarlas con el atributo "migas". */
    public List<Miga> migas(Rol rol, String ruta) {
        List<Miga> migas = new ArrayList<>();
        ItemMenu inicio = menus.get(rol).get(0);
        Optional<ItemMenu> activo = itemActivo(rol, ruta);
        if (activo.isEmpty() || activo.get().esInicio()) {
            migas.add(Miga.clave(inicio.clave(), null));
            return migas;
        }
        migas.add(Miga.clave(inicio.clave(), inicio.ruta()));
        boolean esDetalle = !ruta.equals(activo.get().ruta());
        migas.add(Miga.clave(activo.get().clave(), esDetalle ? activo.get().ruta() : null));
        return migas;
    }
}
