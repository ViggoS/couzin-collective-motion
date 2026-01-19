# Couzin Collective Motion — Simulation Runner

This repository includes a Java simulation (`sim.SimulationRunner`) that reads a JSON config and runs batches of simulations. It now supports running repetitions in parallel and taking the config path from the command line.

## Usage

From the project root (same folder as `java/src` and `config`):

```bash
# Compile (if not already compiled via VS Code/Gradle/your setup)
# Example using javac with a flat classpath:
javac -cp lib/* -d out $(find java/src -name "*.java")

# Run with default threads (number of CPU cores)
java -cp out:lib/* sim.SimulationRunner config/experiment_A_b.json

# Run with explicit thread count (e.g., 8)
java -cp out:lib/* sim.SimulationRunner config/experiment_A_b.json 8
```

- `arg[0]` — path to JSON config (defaults to `config/experiment1_retry.json` if omitted)
- `arg[1]` — optional number of threads (defaults to available processors)
- Output CSV file is taken from the JSON key `output_csv` and written under `data/`.

## JSON Config

Two modes are supported:

- Mode 1 (by proportion): Use `p_values` with `N_values`; `n1 = p*N`, `n2 = 0`.
- Mode 2 (by counts): Use `n1_values` and `n2_values` arrays with `N_values`.

Common keys:
- `output_csv` — filename (no path); saved to `data/`
- `num_runs` — repetitions per parameter combo
- `run_time` — simulation time steps
- `use_feedback` — boolean
- `angle1_deg_values`, `angle2_deg_values` — arrays of degrees

## Parallel Execution

`SimulationRunner` uses a fixed thread pool (`ExecutorService`) and a thread-safe CSV writer. Each repetition is submitted as a task, and the program waits for completion before closing the CSV.

## Helper Script

See `scripts/run-simulations.sh` to run multiple configs easily.

```bash
# Run a batch sequentially
scripts/run-simulations.sh config/experiment_A_b.json config/experiment_B_b.json

# Run with 8 threads per job
THREADS=8 scripts/run-simulations.sh config/*.json
```

## org.json Dependency

The code uses `org.json` (`JSONObject`, `JSONArray`). Ensure it is on the runtime classpath. If you need to add it manually:

1. Download `json` jar (e.g., `org.json:json` from Maven Central) and place it in `lib/`.
2. Include `lib/*` on your compile and run classpath (as shown above).

If VS Code highlights missing imports but the runtime works, that’s usually an editor classpath configuration issue.

## Results

CSV files are written to `data/` with headers:
`run,N,p,n1,n2,angle1_deg,angle2_deg,dirX,dirY,bbox_X,bbox_Y`

You can analyze results using the Python scripts in `analysis/python/`.
