package ar.edu.utn.donatrack.notificaciones.dto;

import ar.edu.utn.donatrack.notificaciones.enums.MedioComunicacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos mínimos que cualquier otro servicio (Donaciones, Incentivos, etc.)
 * debe enviar para solicitar el envío de una notificación.
 *
 * Ejemplo de uso típico (Servicio de Donaciones notificando a una persona
 * donante que su donación fue asignada):
 *
 * {
 *   "mensaje": "¡Tu donación fue asignada a la Escuela Rural N°10!",
 *   "medio": "EMAIL",
 *   "contacto": "ana@mail.com",
 *   "servicioOrigen": "SERVICIO_DONACIONES"
 * }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionRequestDTO {

    @NotBlank(message = "El mensaje no puede estar vacío")
    private String mensaje;

    @NotNull(message = "El medio de comunicación es obligatorio (WHATSAPP, EMAIL o SMS)")
    private MedioComunicacion medio;

    @NotBlank(message = "El contacto del destinatario es obligatorio (email, teléfono o número de WhatsApp)")
    private String contacto;

    /**
     * Identifica qué servicio origina el pedido de notificación
     * (ej: "SERVICIO_DONACIONES", "SERVICIO_INCENTIVOS").
     * Es opcional, pero recomendado para trazabilidad y logs.
     */
    private String servicioOrigen;
}
