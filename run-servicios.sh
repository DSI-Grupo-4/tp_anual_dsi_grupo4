#!/usr/bin/env bash
#
# Compila y levanta todos los servicios del TP que ya están implementados
# (tienen una clase Spring Boot con `main`), más el workflow de n8n de
# servicio-incentivos (sin su dashboard).
#
# Uso: ./run-servicios.sh [build|up|down|status|logs <servicio>]
#   (sin argumentos equivale a "up")

set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$ROOT_DIR/logs"
PID_DIR="$ROOT_DIR/.pids"
INCENTIVOS_DIR="$ROOT_DIR/servicio-incentivos"

mkdir -p "$LOG_DIR" "$PID_DIR"

# Puerto con el que se levanta cada servicio. servicio-donaciones y
# servicio-logistica traen ambos "server.port=8080" en su
# application.properties, así que acá se lo pisa por línea de comando para
# poder correr todo al mismo tiempo sin tocar el código de cada equipo.
declare -A PORTS=(
  [servicio-donaciones]=8080
  [servicio-logistica]=8083
  [servicio-incentivos]=8081
  [servicio-notificaciones]=8082
)
SERVICE_ORDER=(servicio-donaciones servicio-logistica servicio-incentivos servicio-notificaciones)

# Sólo se compilan/levantan los módulos que ya tienen una clase con `main`;
# el resto se reporta como "no implementado" y se omite.
is_implemented() {
  find "$ROOT_DIR/$1/src/main/java" -name "*.java" 2>/dev/null \
    | xargs -r grep -l "public static void main" >/dev/null 2>&1
}

implemented_services() {
  for name in "${SERVICE_ORDER[@]}"; do
    is_implemented "$name" && echo "$name"
  done
}

usage() {
  cat <<EOF
Uso: $0 <comando>

Comandos:
  build            Compila (mvn package) todos los servicios implementados
  up               Compila si hace falta y levanta todos los servicios + n8n (default)
  down             Detiene todos los servicios y el contenedor de n8n
  status           Muestra el estado de cada servicio
  logs <servicio>  Sigue el log de un servicio (ej: servicio-incentivos, n8n)
EOF
}

build() {
  local failed=()
  for name in $(implemented_services); do
    echo "==> Compilando $name..."
    if mvn -q -pl "$name" -am -DskipTests package > "$LOG_DIR/$name-build.log" 2>&1; then
      echo "    OK"
    else
      echo "    FALLÓ (ver $LOG_DIR/$name-build.log)"
      failed+=("$name")
    fi
  done
  if [[ ${#failed[@]} -gt 0 ]]; then
    echo ""
    echo "No se pudieron compilar: ${failed[*]}"
    return 1
  fi
  return 0
}

find_jar() {
  find "$ROOT_DIR/$1/target" -maxdepth 1 -name "*.jar" \
    ! -name "*-sources.jar" ! -name "*.original" 2>/dev/null | head -n1
}

start_java_service() {
  local name="$1" port="${PORTS[$1]}"
  local pid_file="$PID_DIR/$name.pid"

  if [[ -f "$pid_file" ]] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
    echo "==> $name ya está corriendo (PID $(cat "$pid_file"), puerto $port)"
    return 0
  fi

  local jar
  jar="$(find_jar "$name")"
  if [[ -z "$jar" ]]; then
    echo "!! No se encontró el jar de $name. Corré '$0 build' primero." >&2
    return 1
  fi

  echo "==> Iniciando $name en el puerto $port..."
  nohup java -jar "$jar" --server.port="$port" > "$LOG_DIR/$name.log" 2>&1 &
  echo $! > "$pid_file"
}

stop_java_service() {
  local name="$1"
  local pid_file="$PID_DIR/$name.pid"
  [[ -f "$pid_file" ]] || return 0

  local pid
  pid="$(cat "$pid_file")"
  if kill -0 "$pid" 2>/dev/null; then
    echo "==> Deteniendo $name (PID $pid)..."
    kill "$pid"
  fi
  rm -f "$pid_file"
}

ensure_credentials_env() {
  local cred_file="$INCENTIVOS_DIR/credentials.env"
  if [[ ! -f "$cred_file" ]]; then
    echo "==> No existe $cred_file, se crea vacío (completar con las credenciales reales de Discord/Sheets si el workflow las necesita)."
    touch "$cred_file"
  fi
}

start_n8n() {
  ensure_credentials_env
  echo "==> Levantando n8n (workflow de incentivos, sin dashboard)..."
  (cd "$INCENTIVOS_DIR" && make deploy)
}

stop_n8n() {
  echo "==> Deteniendo n8n..."
  (cd "$INCENTIVOS_DIR" && make down)
}

up() {
  local missing=0
  for name in $(implemented_services); do
    [[ -n "$(find_jar "$name")" ]] || missing=1
  done
  if [[ "$missing" -eq 1 ]]; then
    build || echo "==> Sigo con lo que sí compiló."
  fi

  for name in $(implemented_services); do
    start_java_service "$name"
  done
  start_n8n

  echo ""
  status
}

down() {
  for name in $(implemented_services); do
    stop_java_service "$name"
  done
  stop_n8n
}

status() {
  echo "Servicios:"
  for name in "${SERVICE_ORDER[@]}"; do
    if ! is_implemented "$name"; then
      echo "  [N/A]  $name (sin código todavía)"
      continue
    fi
    local pid_file="$PID_DIR/$name.pid"
    if [[ -f "$pid_file" ]] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
      echo "  [UP]   $name (PID $(cat "$pid_file"), puerto ${PORTS[$name]})"
    else
      echo "  [DOWN] $name"
    fi
  done
  if docker compose -f "$INCENTIVOS_DIR/docker-compose.yml" --project-directory "$INCENTIVOS_DIR" ps --status running 2>/dev/null | grep -q n8n; then
    echo "  [UP]   n8n (workflow de incentivos, puerto 5678, dashboard deshabilitado)"
  else
    echo "  [DOWN] n8n"
  fi
}

logs() {
  local name="${1:-}"
  if [[ -z "$name" ]]; then
    echo "Uso: $0 logs <servicio>" >&2
    exit 1
  fi
  if [[ "$name" == "n8n" ]]; then
    (cd "$INCENTIVOS_DIR" && docker compose logs -f n8n)
  else
    tail -f "$LOG_DIR/$name.log"
  fi
}

cmd="${1:-up}"
[[ $# -gt 0 ]] && shift
case "$cmd" in
  build) build ;;
  up) up ;;
  down) down ;;
  status) status ;;
  logs) logs "$@" ;;
  -h|--help) usage ;;
  *) usage; exit 1 ;;
esac
