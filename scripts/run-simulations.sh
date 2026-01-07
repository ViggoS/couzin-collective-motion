#!/usr/bin/env bash
set -euo pipefail

# Run multiple JSON configs with optional THREADS env var
# Usage:
#   scripts/run-simulations.sh config/experiment_A_b.json config/experiment_B_b.json
#   THREADS=8 scripts/run-simulations.sh config/*.json

ROOT_DIR="$(cd "$(dirname "$0")"/.. && pwd)"
OUT_DIR="$ROOT_DIR/out"
LIB_DIR="$ROOT_DIR/lib"
SRC_DIR="$ROOT_DIR/java/src"

# Ensure compiled classes exist; compile if missing
if [[ ! -d "$OUT_DIR" ]] || [[ -z "$(find "$OUT_DIR" -name '*.class' -print -quit)" ]]; then
  echo "Compiling Java sources..."
  mkdir -p "$OUT_DIR"
  # Include jars from lib if present
  if compgen -G "$LIB_DIR/*.jar" > /dev/null; then
    javac -cp "$LIB_DIR/*" -d "$OUT_DIR" $(find "$SRC_DIR" -name "*.java")
  else
    javac -d "$OUT_DIR" $(find "$SRC_DIR" -name "*.java")
  fi
fi

THREADS_DEFAULT=$(python - <<'PY'
import os, multiprocessing
print(multiprocessing.cpu_count())
PY
)

THREADS="${THREADS:-$THREADS_DEFAULT}"

if [[ $# -lt 1 ]]; then
  echo "Provide at least one JSON config path."
  exit 1
fi

# Run each config sequentially; use & to parallelize if desired
for cfg in "$@"; do
  echo "Running sim.SimulationRunner with $cfg using $THREADS threads..."
  if compgen -G "$LIB_DIR/*.jar" > /dev/null; then
    java -cp "$OUT_DIR:$LIB_DIR/*" sim.SimulationRunner "$cfg" "$THREADS"
  else
    java -cp "$OUT_DIR" sim.SimulationRunner "$cfg" "$THREADS"
  fi
done
