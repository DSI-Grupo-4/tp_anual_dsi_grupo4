package ar.edu.utn.frba.dds.logistica.domain.eventos;

public enum TipoEvento {
    RUTA_PLANIFICADA,
    RUTA_INICIADA,
    ENTREGA_EN_TRASLADO,
    ENTREGA_CONFIRMADA,
    ENTREGA_NO_RECIBIDA,
    ENTREGA_FALLIDA,
    // Una persona administradora revisó una NO_RECIBIDA/FALLIDA y la
    // reingresó al depósito (ver Entrega.reingresarADeposito) -- sin este
    // evento, Donaciones nunca se enteraba y la Donacion asociada quedaba
    // congelada en ENTREGA_FALLIDA para siempre, mientras Logística seguía
    // su curso y la replanificaba igual.
    ENTREGA_REPLANIFICADA
}
