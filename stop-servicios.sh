#!/usr/bin/env bash
#
# Detiene los servicios Java levantados por run-servicios.sh (equivalente en
# bash/Linux de stop-servicios.ps1). Lee los PID de .pids/<servicio>.pid y
# valida, antes de matar, que el proceso todavía corresponda a ese servicio
# -- para no tocar un proceso ajeno si el sistema reusó el PID.
#
# Uso: ./stop-servicios.sh [servicio ...]
#   (sin argumentos detiene los 4 servicios)

set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_DIR="$ROOT_DIR/.pids"
SERVICE_ORDER=(servicio-donaciones servicio-logistica servicio-incentivos servicio-notificaciones)

targets=("$@")
[[ ${#targets[@]} -eq 0 ]] && targets=("${SERVICE_ORDER[@]}")

failed=0

for name in "${targets[@]}"; do
  pid_file="$PID_DIR/$name.pid"

  if [[ ! -f "$pid_file" ]]; then
    echo "$name no tiene un proceso registrado."
    continue
  fi

  pid="$(cat "$pid_file")"
  if ! [[ "$pid" =~ ^[0-9]+$ ]]; then
    echo "PID inválido para $name en $pid_file. No se detuvo ningún proceso." >&2
    failed=1
    continue
  fi

  # /proc/<pid> es la única forma confiable de distinguir "no existe" de
  # "existe pero no tengo permiso" -- kill -0 devuelve error en ambos casos
  # (ESRCH vs EPERM) y tratarlos igual borra el PID file de un proceso que
  # en realidad sigue corriendo (confirmado: pasó en la primera versión de
  # este script contra los procesos root de Matías).
  if [[ ! -d "/proc/$pid" ]]; then
    echo "$name ya estaba detenido."
    rm -f "$pid_file"
    continue
  fi

  cmdline="$(tr '\0' ' ' < "/proc/$pid/cmdline" 2>/dev/null || true)"
  if [[ -n "$cmdline" && "$cmdline" != *"$name"* ]]; then
    echo "El PID $pid no corresponde al arranque de $name (el sistema reusó el PID). No se detuvo." >&2
    failed=1
    continue
  fi

  echo "==> Deteniendo $name (PID $pid)..."
  if ! kill "$pid" 2>/dev/null; then
    owner="$(stat -c '%U' "/proc/$pid" 2>/dev/null || echo "desconocido")"
    echo "    No se pudo enviar SIGTERM a $pid -- pertenece a otro usuario ($owner). Probá: sudo kill $pid" >&2
    failed=1
    continue
  fi

  for _ in $(seq 1 10); do
    kill -0 "$pid" 2>/dev/null || break
    sleep 0.5
  done

  if kill -0 "$pid" 2>/dev/null; then
    echo "    $name no respondió a SIGTERM, forzando con SIGKILL..."
    kill -9 "$pid" 2>/dev/null || {
      echo "    No se pudo forzar la detención de $pid (¿proceso de otro usuario?)." >&2
      failed=1
      continue
    }
  fi

  rm -f "$pid_file"
  echo "    $name detenido."
done

exit $failed
