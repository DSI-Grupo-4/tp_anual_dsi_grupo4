package ar.edu.utn.frba.dds.logistica.domain.rutas;

public enum EstadoEntrega {
    PENDIENTE,
    ASIGNADA_A_RUTA,
    EN_TRASLADO,
    ENTREGADA,
    // La entidad informó que no la recibió -- motivo "imposibilidad de
    // recepción" del enunciado.
    NO_RECIBIDA,
    // No se pudo concretar por un motivo distinto a que la entidad no la
    // haya recibido (vencimiento de los bienes previo a la entrega,
    // incidente logístico en el camino, etc.) -- reportado por el chofer o
    // una persona administradora, no por la entidad.
    FALLIDA,
    // La persona administradora revisó una NO_RECIBIDA/FALLIDA y determinó
    // que se puede reintentar -- queda constancia de esa decisión antes de
    // volver a estar disponible para la próxima planificación.
    REPLANIFICABLE
}
