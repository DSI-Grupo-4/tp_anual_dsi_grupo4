package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedioContactoDTO {
    public static java.util.List<ar.edu.utn.frba.dds.donaciones.domain.personas.MedioContacto> validar(
            java.util.List<MedioContactoDTO> contactos) {
        if (contactos == null || contactos.isEmpty()) throw new IllegalArgumentException("Debe incluir EMAIL y un contacto preferido");
        var resultado = contactos.stream().map(c -> {
            if (c == null || c.tipo == null || c.valor == null || c.valor.isBlank() || c.esPreferido == null)
                throw new IllegalArgumentException("Cada contacto requiere tipo, valor y esPreferido");
            String valor = c.valor.trim();
            if (c.tipo == TipoContacto.EMAIL && !valor.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
                throw new IllegalArgumentException("EMAIL inválido");
            return new ar.edu.utn.frba.dds.donaciones.domain.personas.MedioContacto(c.tipo, valor, c.esPreferido);
        }).toList();
        if (resultado.stream().noneMatch(c -> c.getTipo() == TipoContacto.EMAIL)) throw new IllegalArgumentException("EMAIL obligatorio");
        if (resultado.stream().filter(c -> Boolean.TRUE.equals(c.esPreferido())).count() != 1)
            throw new IllegalArgumentException("Debe elegir exactamente un contacto preferido");
        return resultado;
    }

    public static java.util.List<MedioContactoDTO> desde(ar.edu.utn.frba.dds.donaciones.domain.personas.Persona persona) {
        return persona.getMediosContacto().stream().map(m -> {
            var dto = new MedioContactoDTO(); dto.setTipo(m.getTipo()); dto.setValor(m.getValor()); dto.setEsPreferido(m.esPreferido()); return dto;
        }).toList();
    }

    @NotNull private TipoContacto tipo;
    @NotBlank private String valor;
    @NotNull private Boolean esPreferido;
}
