package ar.edu.utn.frba.dds.donaciones.dto;
import java.util.List;
import org.springframework.data.domain.Page;
public record PaginaDTO<T>(List<T> content, long totalElements, int totalPages, int number) {
    public static <T> PaginaDTO<T> desde(Page<T> p) {
        return new PaginaDTO<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber());
    }
}
