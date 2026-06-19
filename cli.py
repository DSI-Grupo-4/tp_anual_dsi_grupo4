#!/usr/bin/env python3
"""
CLI manual para desplegar y probar el sistema DonaTrack.

Requiere Python 3.9+. Usa biblioteca estandar + requests. Si requests no esta
instalado, intenta instalarlo con pip al inicio.
"""

from __future__ import annotations

import json
import os
import signal
import subprocess
import sys
import time
from pathlib import Path
from typing import Any, Callable
from urllib.parse import urljoin

def ensure_requests():
    try:
        import requests as imported_requests
        return imported_requests
    except ImportError:
        pass

    venv_dir = Path(__file__).resolve().parent / ".cli-venv"
    if os.name == "nt":
        venv_python = venv_dir / "Scripts" / "python.exe"
    else:
        venv_python = venv_dir / "bin" / "python"

    if not venv_python.exists():
        print("requests no esta instalado. Creando entorno local .cli-venv...")
        subprocess.check_call([sys.executable, "-m", "venv", str(venv_dir)])

    print("requests no esta instalado en el entorno local. Instalando...")
    subprocess.check_call([str(venv_python), "-m", "pip", "install", "requests"])
    os.execv(str(venv_python), [str(venv_python), str(Path(__file__).resolve()), *sys.argv[1:]])


requests = ensure_requests()


# ---------------------------------------------------------------------------
# Constantes faciles de cambiar
# Valores leidos del proyecto al crear este CLI:
# - servicio-donaciones: sin application.properties/yml, Spring Boot usa 8080.
# - servicio-incentivos: servicio-incentivos/src/main/resources/application.properties -> 8081.
# - servicio-notificaciones: servicio-notificaciones/src/main/resources/application.yml -> 8083.
# - n8n: servicio-incentivos/docker-compose.yml -> 5678:5678, via Docker Compose.
# ---------------------------------------------------------------------------
ROOT_DIR = Path(__file__).resolve().parent
LOG_DIR = ROOT_DIR / "logs"

HOST = "localhost"
DONACIONES_PORT = 8080
INCENTIVOS_PORT = 8081
NOTIFICACIONES_PORT = 8083
N8N_PORT = 5678

DONACIONES_BASE_URL = f"http://{HOST}:{DONACIONES_PORT}"
INCENTIVOS_BASE_URL = f"http://{HOST}:{INCENTIVOS_PORT}"
NOTIFICACIONES_BASE_URL = f"http://{HOST}:{NOTIFICACIONES_PORT}"
N8N_BASE_URL = f"http://{HOST}:{N8N_PORT}"

SERVICE_MODULES = {
    "donaciones": {
        "module": "servicio-donaciones",
        "artifact_id": "servicio-donaciones",
        "base_url": DONACIONES_BASE_URL,
        "health_paths": ["/actuator/health", "/"],
        "log": "donaciones.log",
    },
    "incentivos": {
        "module": "servicio-incentivos",
        "artifact_id": "servicio-incentivos",
        "base_url": INCENTIVOS_BASE_URL,
        "health_paths": ["/api/incentivos/ping", "/actuator/health", "/"],
        "log": "incentivos.log",
    },
    "notificaciones": {
        "module": "servicio-notificaciones",
        "artifact_id": "notificaciones-service",
        "base_url": NOTIFICACIONES_BASE_URL,
        "health_paths": ["/api/notificaciones", "/actuator/health", "/"],
        "log": "notificaciones.log",
    },
}

REQUEST_TIMEOUT = 8
STARTUP_TIMEOUT = 60


class SessionState:
    def __init__(self) -> None:
        self.donante_id: str | None = None
        self.donante_nombre: str | None = None
        self.entidad_id: str | None = None
        self.entidad_nombre: str | None = None
        self.donacion_id: str | None = None
        self.donacion_estado: str | None = None
        self.notificacion_id: str | None = None
        self.processes: dict[str, subprocess.Popen] = {}
        self.log_files: dict[str, Any] = {}


STATE = SessionState()


# ---------------------------------------------------------------------------
# Utilidades generales
# ---------------------------------------------------------------------------
def clear_line() -> None:
    print()


def prompt(text: str, default: str | None = None) -> str:
    label = f"{text} [{default}]: " if default is not None else f"{text}: "
    value = input(label).strip()
    return default if value == "" and default is not None else value


def pause() -> None:
    input("\nPresioná Enter para continuar...")


def pretty_json(value: Any) -> str:
    try:
        return json.dumps(value, indent=2, ensure_ascii=False)
    except TypeError:
        return str(value)


def print_http_response(response: requests.Response) -> None:
    print(f"\nHTTP {response.status_code} {response.reason}")
    relevant_headers = ["content-type", "location", "cache-control"]
    shown = False
    for key, value in response.headers.items():
        if key.lower() in relevant_headers:
            if not shown:
                print("Headers:")
                shown = True
            print(f"  {key}: {value}")

    if response.text.strip() == "":
        print("Body: <vacio>")
        return

    print("Body:")
    try:
        print(pretty_json(response.json()))
    except ValueError:
        print(response.text)


def print_request_error(exc: Exception) -> None:
    print("\nERROR HTTP")
    print(f"  {type(exc).__name__}: {exc}")


def request_json(method: str, base_url: str, path: str, payload: Any | None = None) -> requests.Response | None:
    url = urljoin(base_url.rstrip("/") + "/", path.lstrip("/"))
    try:
        response = requests.request(method, url, json=payload, timeout=REQUEST_TIMEOUT)
        print_http_response(response)
        if response.status_code == 404:
            print("\nAviso: endpoint no implementado o recurso inexistente en el servicio actual.")
        elif response.status_code >= 400:
            print("\nAviso: el servicio devolvio un error HTTP; el CLI continua activo.")
        return response
    except requests.RequestException as exc:
        print_request_error(exc)
        return None


def parse_json_response(response: requests.Response | None) -> Any | None:
    if response is None or response.text.strip() == "":
        return None
    try:
        return response.json()
    except ValueError:
        return None


def not_implemented(section: str, detail: str = "") -> None:
    print(f"\n[{section}] Endpoint REST no implementado en el proyecto actual.")
    if detail:
        print(detail)
    print("El comando queda en el menu para guiar la prueba manual cuando el endpoint exista.")


def default_id(current: str | None, label: str) -> str:
    return prompt(label, current or "")


def update_last_notificacion_from_response(response: requests.Response | None) -> None:
    data = parse_json_response(response)
    if isinstance(data, dict) and data.get("id"):
        STATE.notificacion_id = str(data["id"])


# ---------------------------------------------------------------------------
# Despliegue
# ---------------------------------------------------------------------------
def run_command(command: list[str], cwd: Path) -> int:
    print("Ejecutando:", " ".join(command))
    process = subprocess.run(command, cwd=str(cwd))
    return process.returncode


def build_all() -> bool:
    rc = run_command(["mvn", "clean", "package", "-DskipTests"], ROOT_DIR)
    if rc != 0:
        print(f"\nBuild fallido con codigo {rc}. Revisá la salida de Maven.")
        return False
    return True


def find_service_jar(module: str) -> Path | None:
    target = ROOT_DIR / module / "target"
    if not target.exists():
        return None
    candidates = sorted(
        [p for p in target.glob("*.jar") if not p.name.endswith("-sources.jar") and not p.name.endswith("-javadoc.jar")],
        key=lambda p: p.stat().st_mtime,
        reverse=True,
    )
    return candidates[0] if candidates else None


def start_process(name: str, info: dict[str, Any]) -> None:
    jar = find_service_jar(info["module"])
    if jar is None:
        print(f"  {name}: error, no se encontro jar en {info['module']}/target")
        return

    LOG_DIR.mkdir(exist_ok=True)
    log_path = LOG_DIR / info["log"]
    log_file = open(log_path, "a", encoding="utf-8")
    command = ["java", "-jar", str(jar)]
    print(f"  {name}: lanzando {' '.join(command)} -> {log_path}")
    process = subprocess.Popen(command, cwd=str(ROOT_DIR), stdout=log_file, stderr=subprocess.STDOUT)
    STATE.processes[name] = process
    STATE.log_files[name] = log_file


def service_responds(base_url: str, paths: list[str]) -> tuple[bool, str]:
    last_error = "sin respuesta"
    for path in paths:
        url = urljoin(base_url.rstrip("/") + "/", path.lstrip("/"))
        try:
            response = requests.get(url, timeout=2)
            # Un 404 de Spring tambien demuestra que el servidor HTTP esta arriba.
            if response.status_code < 500:
                return True, f"{url} -> HTTP {response.status_code}"
            last_error = f"{url} -> HTTP {response.status_code}"
        except requests.RequestException as exc:
            last_error = f"{url} -> {type(exc).__name__}: {exc}"
    return False, last_error


def wait_for_service(name: str, info: dict[str, Any]) -> tuple[bool, str]:
    deadline = time.time() + STARTUP_TIMEOUT
    while time.time() < deadline:
        process = STATE.processes.get(name)
        if process and process.poll() is not None:
            return False, f"proceso finalizo con codigo {process.returncode}"
        ok, detail = service_responds(info["base_url"], info["health_paths"])
        if ok:
            return True, detail
        time.sleep(2)
    return False, f"timeout de {STARTUP_TIMEOUT}s esperando {info['base_url']}"


def levantar_servicios() -> None:
    if STATE.processes:
        print("\nYa hay servicios iniciados desde esta sesion. Usá 99 para detenerlos antes de relanzar.")
        return

    if not build_all():
        return

    for name, info in SERVICE_MODULES.items():
        start_process(name, info)

    print("\nEsperando servicios...")
    for name, info in SERVICE_MODULES.items():
        ok, detail = wait_for_service(name, info)
        estado = "levantado" if ok else "error"
        print(f"  {name}: {estado} ({detail})")


def detener_servicios() -> None:
    if not STATE.processes:
        print("\nNo hay procesos iniciados por esta sesion.")
        return

    for name, process in list(STATE.processes.items()):
        if process.poll() is None:
            print(f"Deteniendo {name}...")
            try:
                if os.name == "nt":
                    process.terminate()
                else:
                    process.send_signal(signal.SIGTERM)
                process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                print(f"  {name}: no termino a tiempo, matando proceso")
                process.kill()
        print(f"  {name}: detenido")
        STATE.processes.pop(name, None)

    for log_file in STATE.log_files.values():
        try:
            log_file.close()
        except Exception:
            pass
    STATE.log_files.clear()


# ---------------------------------------------------------------------------
# Donantes, entidades y donaciones: endpoints no presentes en donaciones.
# ---------------------------------------------------------------------------
def registrar_persona_humana() -> None:
    nombre = prompt("Nombre", "Ana")
    apellido = prompt("Apellido", "Perez")
    email = prompt("Email", "ana.perez@mail.com")
    STATE.donante_nombre = f"{nombre} {apellido}"
    not_implemented("DONANTES", f"Datos capturados para sesion: {STATE.donante_nombre} <{email}>. No se pudo obtener ID real.")


def registrar_persona_juridica() -> None:
    razon = prompt("Razon social", "Empresa Solidaria SRL")
    email = prompt("Email", "contacto@empresa.local")
    STATE.donante_nombre = razon
    not_implemented("DONANTES", f"Datos capturados para sesion: {razon} <{email}>. No se pudo obtener ID real.")


def listar_donantes() -> None:
    not_implemented("DONANTES", "No hay controllers REST en servicio-donaciones para listar donantes.")


def ver_donante() -> None:
    default_id(STATE.donante_id, "ID donante")
    not_implemented("DONANTES", "No hay endpoint REST de detalle de donante.")


def registrar_entidad() -> None:
    nombre = prompt("Nombre", "Comedor Escobar")
    direccion = prompt("Direccion", "Av. Siempre Viva 123")
    email = prompt("Email", "comedor@mail.com")
    STATE.entidad_nombre = nombre
    not_implemented("ENTIDADES", f"Datos capturados: {nombre}, {direccion}, {email}. No se pudo obtener ID real.")


def listar_entidades() -> None:
    not_implemented("ENTIDADES", "No hay controllers REST en servicio-donaciones para entidades beneficiarias.")


def registrar_necesidad_extraordinaria() -> None:
    default_id(STATE.entidad_id, "ID entidad")
    prompt("Descripcion", "Alimentos no perecederos")
    not_implemented("ENTIDADES", "No hay endpoint REST para registrar necesidades extraordinarias.")


def registrar_necesidad_recurrente() -> None:
    default_id(STATE.entidad_id, "ID entidad")
    prompt("Descripcion", "Viandas semanales")
    not_implemented("ENTIDADES", "No hay endpoint REST para registrar necesidades recurrentes.")


def listar_necesidades_entidad() -> None:
    default_id(STATE.entidad_id, "ID entidad")
    not_implemented("ENTIDADES", "No hay endpoint REST para listar necesidades por entidad.")


def registrar_donacion() -> None:
    default_id(STATE.donante_id, "ID donante")
    prompt("Descripcion", "Caja de alimentos")
    not_implemented("DONACIONES", "No hay endpoint REST para registrar donaciones en servicio-donaciones.")


def listar_donaciones() -> None:
    not_implemented("DONACIONES", "No hay endpoint REST para listar donaciones.")


def historial_donacion() -> None:
    default_id(STATE.donacion_id, "ID donacion")
    not_implemented("DONACIONES", "No hay endpoint REST para historial de estados.")


def cambiar_estado_donacion() -> None:
    default_id(STATE.donacion_id, "ID donacion")
    print("Estados validos no disponibles: no existe endpoint/modelo REST de estados en el servicio actual.")
    not_implemented("DONACIONES")


def marcar_entrega_fallida() -> None:
    default_id(STATE.donacion_id, "ID donacion")
    prompt("Justificacion", "No se pudo contactar al destinatario")
    not_implemented("DONACIONES", "No hay endpoint REST para marcar ENTREGA_FALLIDA.")


def ejecutar_matchmaking() -> None:
    default_id(STATE.donacion_id, "ID donacion")
    not_implemented("DONACIONES", "No hay endpoint REST de matchmaking/ranking de entidades candidatas.")


def confirmar_asignacion() -> None:
    default_id(STATE.donacion_id, "ID donacion")
    default_id(STATE.entidad_id, "ID entidad")
    not_implemented("DONACIONES", "No hay endpoint REST para confirmar asignacion.")


# ---------------------------------------------------------------------------
# Incentivos
# ---------------------------------------------------------------------------
def current_donor_id_or_prompt() -> str:
    donor_id = default_id(STATE.donante_id, "ID donante")
    if donor_id:
        STATE.donante_id = donor_id
    return donor_id


def ver_metricas() -> None:
    donor_id = current_donor_id_or_prompt()
    if donor_id:
        request_json("GET", INCENTIVOS_BASE_URL, f"/api/incentivos/donantes/{donor_id}/metricas")


def ver_misiones() -> None:
    donor_id = current_donor_id_or_prompt()
    if donor_id:
        request_json("GET", INCENTIVOS_BASE_URL, f"/api/incentivos/donantes/{donor_id}/misiones")


def ver_insignias() -> None:
    donor_id = current_donor_id_or_prompt()
    if donor_id:
        request_json("GET", INCENTIVOS_BASE_URL, f"/api/incentivos/donantes/{donor_id}/insignias")


def ver_ranking_actual() -> None:
    request_json("GET", INCENTIVOS_BASE_URL, "/api/incentivos/ranking/actual")


def ver_historial_rankings() -> None:
    request_json("GET", INCENTIVOS_BASE_URL, "/api/incentivos/ranking/historial")


def completar_mision_incentivos() -> None:
    donor_id = current_donor_id_or_prompt()
    mision_id = prompt("ID mision", "1")
    medio = prompt("Medio", "EMAIL")
    contacto = prompt("Contacto", f"donante{donor_id}@mail.com")
    payload = {"donanteId": int(donor_id), "misionId": int(mision_id), "medio": medio, "contacto": contacto}
    request_json("POST", INCENTIVOS_BASE_URL, "/api/incentivos/misiones/completar", payload)


def subir_categoria_incentivos() -> None:
    donor_id = current_donor_id_or_prompt()
    categoria = prompt("Nueva categoria", "COLABORADOR")
    medio = prompt("Medio", "EMAIL")
    contacto = prompt("Contacto", f"donante{donor_id}@mail.com")
    payload = {"donanteId": int(donor_id), "nuevaCategoria": categoria, "medio": medio, "contacto": contacto}
    request_json("POST", INCENTIVOS_BASE_URL, "/api/incentivos/categoria/subir", payload)


# ---------------------------------------------------------------------------
# Notificaciones
# ---------------------------------------------------------------------------
def enviar_notificacion_manual() -> None:
    mensaje = prompt("Mensaje", "Tu donacion fue asignada a una entidad beneficiaria.")
    medio = prompt("Medio EMAIL/SMS/WHATSAPP", "EMAIL").upper()
    contacto = prompt("Contacto", "donante@mail.com")
    payload = {
        "mensaje": mensaje,
        "medio": medio,
        "contacto": contacto,
        "servicioOrigen": "cli",
    }
    response = request_json("POST", NOTIFICACIONES_BASE_URL, "/api/notificaciones/", payload)
    update_last_notificacion_from_response(response)


def ver_notificaciones() -> None:
    request_json("GET", NOTIFICACIONES_BASE_URL, "/api/notificaciones/")


def ver_notificacion() -> None:
    notification_id = default_id(STATE.notificacion_id, "ID notificacion")
    if notification_id:
        response = request_json("GET", NOTIFICACIONES_BASE_URL, f"/api/notificaciones/{notification_id}")
        update_last_notificacion_from_response(response)


# ---------------------------------------------------------------------------
# Flujos combinados
# ---------------------------------------------------------------------------
def step(title: str, action: Callable[[], None]) -> None:
    print("\n" + "-" * 60)
    print(title)
    print("-" * 60)
    action()
    pause()


def flujo_completo_donacion() -> None:
    print("\nFlujo completo guiado. Algunos pasos dependen de endpoints aun no implementados en servicio-donaciones.")
    step("1. Crear donante", registrar_persona_humana)
    step("2. Crear entidad beneficiaria", registrar_entidad)
    step("3. Registrar necesidad extraordinaria", registrar_necesidad_extraordinaria)
    step("4. Registrar donacion", registrar_donacion)
    step("5. Ejecutar matchmaking", ejecutar_matchmaking)
    step("6. Confirmar asignacion", confirmar_asignacion)
    step("7. Avanzar estado hasta ENTREGADA", cambiar_estado_donacion)
    step("8. Verificar notificaciones registradas", ver_notificaciones)
    step("9. Verificar metricas de incentivos", ver_metricas)


def simular_inactividad() -> None:
    donor_id = current_donor_id_or_prompt()
    print("\nNo hay endpoint REST implementado para modificar fechaUltimaActividad/fechaUltimaInteraccion del donante.")
    print("Para probarlo hoy, cargá un Donante en el repositorio en memoria de servicio-donaciones con")
    print("fechaUltimaInteraccion anterior a 20 dias y dejá correr el scheduler @Scheduled(cron = '0 0 8 * * *').")
    print(f"Donante seleccionado para la prueba manual: {donor_id or '<sin id>'}")


# ---------------------------------------------------------------------------
# Menu
# ---------------------------------------------------------------------------
def session_summary() -> str:
    return "\n".join(
        [
            f"    Donante:  ID={STATE.donante_id or '-'} ({STATE.donante_nombre or '-'})",
            f"    Entidad:  ID={STATE.entidad_id or '-'} ({STATE.entidad_nombre or '-'})",
            f"    Donacion: ID={STATE.donacion_id or '-'} (estado: {STATE.donacion_estado or '-'})",
            f"    Notif.:   ID={STATE.notificacion_id or '-'}",
        ]
    )


def print_menu() -> None:
    print(
        f"""
=============================================================
  SISTEMA DE GESTION DE DONACIONES - CLI DE PRUEBAS
=============================================================
  Sesion actual:
{session_summary()}

  [ SERVICIOS ]
  0  Levantar servicios
  99 Detener servicios

  [ DONANTES ]
  1  Registrar persona humana
  2  Registrar persona juridica
  3  Listar donantes
  4  Ver donante

  [ ENTIDADES BENEFICIARIAS ]
  5  Registrar entidad
  6  Listar entidades
  7  Registrar necesidad extraordinaria
  8  Registrar necesidad recurrente
  9  Listar necesidades de una entidad

  [ DONACIONES ]
  10 Registrar donacion
  11 Listar donaciones
  12 Ver historial de estados de una donacion
  13 Cambiar estado de una donacion
  14 Marcar donacion como ENTREGA_FALLIDA
  15 Ejecutar matchmaking para una donacion
  16 Confirmar asignacion de una donacion a una entidad

  [ INCENTIVOS ]
  20 Ver metricas de actividad de un donante
  21 Ver misiones y progreso de un donante
  22 Ver insignias de un donante
  23 Ver ranking mensual actual
  24 Ver historial de rankings
  25 Simular mision completada
  26 Simular subida de categoria

  [ NOTIFICACIONES ]
  30 Enviar notificacion manual
  31 Ver todas las notificaciones registradas
  32 Ver detalle de una notificacion

  [ FLUJOS COMBINADOS ]
  90 Flujo completo de donacion
  91 Simular inactividad de donante

  q  Salir
============================================================="""
    )


ACTIONS: dict[str, Callable[[], None]] = {
    "0": levantar_servicios,
    "99": detener_servicios,
    "1": registrar_persona_humana,
    "2": registrar_persona_juridica,
    "3": listar_donantes,
    "4": ver_donante,
    "5": registrar_entidad,
    "6": listar_entidades,
    "7": registrar_necesidad_extraordinaria,
    "8": registrar_necesidad_recurrente,
    "9": listar_necesidades_entidad,
    "10": registrar_donacion,
    "11": listar_donaciones,
    "12": historial_donacion,
    "13": cambiar_estado_donacion,
    "14": marcar_entrega_fallida,
    "15": ejecutar_matchmaking,
    "16": confirmar_asignacion,
    "20": ver_metricas,
    "21": ver_misiones,
    "22": ver_insignias,
    "23": ver_ranking_actual,
    "24": ver_historial_rankings,
    "25": completar_mision_incentivos,
    "26": subir_categoria_incentivos,
    "30": enviar_notificacion_manual,
    "31": ver_notificaciones,
    "32": ver_notificacion,
    "90": flujo_completo_donacion,
    "91": simular_inactividad,
}


def main() -> None:
    print("DonaTrack CLI")
    print("n8n detectado via Docker Compose en servicio-incentivos/docker-compose.yml, puerto 5678.")
    try:
        while True:
            print_menu()
            option = input("Elegí una opción: ").strip().lower()
            if option == "q":
                break
            action = ACTIONS.get(option)
            if action is None:
                print("\nOpcion invalida.")
                pause()
                continue
            action()
            if option not in {"0", "99", "90"}:
                pause()
    finally:
        if STATE.processes:
            answer = input("\nHay servicios iniciados por esta sesion. ¿Detenerlos? [s]: ").strip().lower() or "s"
            if answer.startswith("s"):
                detener_servicios()


if __name__ == "__main__":
    main()
