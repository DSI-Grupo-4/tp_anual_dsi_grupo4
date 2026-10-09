#!/usr/bin/env bash
#
# Compila y levanta todos los servicios del TP que ya están implementados
# (tienen una clase Spring Boot con `main`), más el workflow de n8n de
# servicio-incentivos .
#
# Uso: ./run-servicios.sh [build|up|down|status|logs <servicio>]
#   (sin argumentos equivale a "up")

set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$ROOT_DIR/logs"
PID_DIR="$ROOT_DIR/.pids"
INCENTIVOS_DIR="$ROOT_DIR/servicio-incentivos"
COMPOSE_FILE="$ROOT_DIR/docker-compose.integration.yml"

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
  up               Compila si hace falta y levanta todos los servicios + infra (default)
  down             Detiene todos los servicios y la infra (RabbitMQ, MySQL, Adminer, n8n)
  status           Muestra el estado de cada servicio y de la infra
  logs <servicio>  Sigue el log de un servicio (ej: servicio-incentivos, n8n, mysql, rabbitmq, adminer)
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

start_infra() {
  ensure_credentials_env
  echo "==> Levantando infraestructura (RabbitMQ, MySQL, Adminer, n8n)..."
  docker compose -f "$COMPOSE_FILE" up -d
  # "docker compose up -d" vuelve en cuanto el contenedor arrancó, pero el
  # entrypoint de n8n todavía tiene que importar y publicar el workflow
  # adentro -- confirmado en vivo: tarda entre 8 y 22s según la máquina, así
  # que se sondea el webhook en vez de adivinar un tiempo fijo. Sin esto,
  # Incentivos le pega al webhook antes de que esté registrado y recibe 404.
  echo "    Esperando a que n8n registre el webhook (hasta 60s)..."
  for _ in $(seq 1 30); do
    code="$(curl -s -o /dev/null -w '%{http_code}' -X POST http://localhost:5678/webhook/events/insignia-otorgada 2>/dev/null)"
    [[ "$code" != "404" && "$code" != "000" ]] && break
    sleep 2
  done
}

stop_infra() {
  echo "==> Deteniendo infraestructura (RabbitMQ, MySQL, Adminer, n8n)..."
  docker compose -f "$COMPOSE_FILE" down
}

up() {
  local missing=0
  for name in $(implemented_services); do
    [[ -n "$(find_jar "$name")" ]] || missing=1
  done
  if [[ "$missing" -eq 1 ]]; then
    build || echo "==> Sigo con lo que sí compiló."
  fi

  start_infra

  for name in $(implemented_services); do
    start_java_service "$name"
  done

  echo ""
  status
}

down() {
  for name in $(implemented_services); do
    stop_java_service "$name"
  done
  stop_infra
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
  echo ""
  echo "Infraestructura:"
  local running
  running="$(docker compose -f "$COMPOSE_FILE" ps --status running --format '{{.Service}}' 2>/dev/null)"
  for infra in rabbitmq mysql adminer n8n; do
    if echo "$running" | grep -qx "$infra"; then
      echo "  [UP]   $infra"
    else
      echo "  [DOWN] $infra"
    fi
  done
}

logs() {
  local name="${1:-}"
  if [[ -z "$name" ]]; then
    echo "Uso: $0 logs <servicio>" >&2
    exit 1
  fi
  if [[ "$name" == "n8n" || "$name" == "mysql" || "$name" == "rabbitmq" || "$name" == "adminer" ]]; then
    docker compose -f "$COMPOSE_FILE" logs -f "$name"
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
