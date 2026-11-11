package ar.edu.utn.frba.dds.web.client;

import org.springframework.http.HttpHeaders;

/**
 * Punto de extensión para que las llamadas a las APIs lleven las credenciales
 * del usuario (Persona 2). Se aplica a todos los clientes REST antes de cada request.
 *
 * Implementación esperada con Keycloak: leer el access token del usuario
 * autenticado (OAuth2AuthorizedClientManager) y agregar "Authorization: Bearer ...".
 * Para reemplazar la implementación vacía alcanza con declarar un @Component
 * que implemente esta interfaz.
 */
@FunctionalInterface
public interface PropagadorCredenciales {

    void aplicar(HttpHeaders headers);

    /** Sin autenticación: no agrega nada. Es la implementación por defecto hasta que exista la de Persona 2. */
    PropagadorCredenciales NINGUNO = headers -> { };
}
