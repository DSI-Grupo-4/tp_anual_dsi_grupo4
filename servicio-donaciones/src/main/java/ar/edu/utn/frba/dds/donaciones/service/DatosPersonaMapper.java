package ar.edu.utn.frba.dds.donaciones.service;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaHumana;
import ar.edu.utn.frba.dds.donaciones.dto.*;
import java.util.*;
/** Traducción de representantes en los contratos REST. */
final class DatosPersonaMapper {
    private DatosPersonaMapper() { }
    static List<PersonaHumana> actualizarRepresentantes(List<PersonaHumanaDTO> datos, List<PersonaHumana> anteriores) {
        if(datos==null) return anteriores;
        var resultado=new ArrayList<PersonaHumana>();
        for(var dto:datos) {
            var existente=anteriores==null?Optional.<PersonaHumana>empty():anteriores.stream()
                    .filter(p->Objects.equals(p.getDocumento(),dto.getDocumento())).findFirst();
            if(existente.isEmpty()) { resultado.addAll(representantes(List.of(dto))); continue; }
            var p=existente.get(); p.setNombre(dto.getNombre()); p.setApellido(dto.getApellido());
            if(dto.getEdad()!=null) p.setEdad(dto.getEdad());
            if(dto.getGenero()!=null) p.setGenero(dto.getGenero());
            if(dto.getDireccion()!=null) p.setDireccion(EntidadBeneficiariaService.convertirDireccionDominio(dto.getDireccion()));
            // La ficha breve edita el correo principal; conserva otros medios y su preferencia.
            var correo=MedioContactoDTO.validar(dto.getMediosContacto()).stream()
                    .filter(c->c.getTipo()==ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto.EMAIL).findFirst().orElseThrow();
            var actual=p.getMediosContacto().stream().filter(c->c.getTipo()==correo.getTipo()).findFirst();
            if(actual.isPresent()) actual.get().setValor(correo.getValor());
            else p.agregarMedio(new ar.edu.utn.frba.dds.donaciones.domain.personas.MedioContacto(
                    correo.getTipo(),correo.getValor(),p.medioPreferido().isEmpty()));
            resultado.add(p);
        }
        return resultado;
    }
    static List<PersonaHumana> representantes(List<PersonaHumanaDTO> datos) {
        if (datos == null) return new ArrayList<>();
        return new ArrayList<>(datos.stream().map(d -> {
            var p = new PersonaHumana(d.getNombre(), d.getApellido(), d.getEdad(), d.getDocumento(), d.getGenero());
            p.setDireccion(EntidadBeneficiariaService.convertirDireccionDominio(d.getDireccion()));
            p.setMediosContacto(MedioContactoDTO.validar(d.getMediosContacto()));
            return p;
        }).toList());
    }
    static List<PersonaHumanaDTO> representantesDTO(List<PersonaHumana> personas) {
        if (personas == null) return List.of();
        return personas.stream().map(p -> {
            var d = new PersonaHumanaDTO();
            d.setNombre(p.getNombre()); d.setApellido(p.getApellido()); d.setDocumento(p.getDocumento());
            d.setEdad(p.getEdad()); d.setGenero(p.getGenero());
            d.setDireccion(EntidadBeneficiariaService.convertirDireccionADTO(p.getDireccion()));
            d.setMediosContacto(MedioContactoDTO.desde(p)); return d;
        }).toList();
    }
}
