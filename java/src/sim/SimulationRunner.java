package sim;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SimulationRunner {

    public static void main(String[] args) {

        try {
            // CLI args: [0] path to JSON config, [1] optional number of threads
            String configPath = args != null && args.length >= 1 ? args[0] : "config/experiment1_retry.json";
            int numThreads = args != null && args.length >= 2 ? Integer.parseInt(args[1]) : Runtime.getRuntime().availableProcessors();

            // Läs JSON
            String jsonString = new String(Files.readAllBytes(Paths.get(configPath)));
            JSONObject config = new JSONObject(jsonString);

            //String outputCsv = config.getString("output_csv");
            String outputCsv = "data/" + config.getString("output_csv");
            int numRuns = config.getInt("num_runs");
            int runTime = config.getInt("run_time");
            boolean useFeedback = config.getBoolean("use_feedback");

            JSONArray N_values = config.getJSONArray("N_values");
            JSONArray n1_values = config.getJSONArray("n1_values");
            JSONArray n2_values = config.getJSONArray("n2_values");
            JSONArray p_values = config.getJSONArray("p_values");
            JSONArray angle1_values = config.getJSONArray("angle1_deg_values");
            JSONArray angle2_values = config.getJSONArray("angle2_deg_values");

            CsvWriter csv = new CsvWriter(outputCsv);
            csv.writeHeader("run", "N",  "p", "n1", "n2", "angle1_deg", "angle2_deg", "dirX", "dirY", "bbox_X", "bbox_Y");

            Random rng = new Random();

            // Thread pool for parallel simulations
            ExecutorService pool = Executors.newFixedThreadPool(numThreads);
            List<Future<?>> futures = new ArrayList<>();

            // Loopa över alla kombinationer
            for (int iN = 0; iN < N_values.length(); iN++) {
                int N = N_values.getInt(iN);

                // Check if we should use p_values or n1/n2 arrays
                if (p_values.length() > 1) {
                    // Mode 1: Loop over p_values and calculate n1, n2
                    for (int ip = 0; ip < p_values.length(); ip++) {
                        double p = p_values.getDouble(ip);
                        int n1 = (int) (p * N); // number of informed agents of type 1
                        int n2 = 0;
                        
                        for (int ia1 = 0; ia1 < angle1_values.length(); ia1++) {
                            double angle1 = Math.toRadians(angle1_values.getDouble(ia1));

                            for (int ia2 = 0; ia2 < angle2_values.length(); ia2++) {
                                double angle2 = Math.toRadians(angle2_values.getDouble(ia2));

                                // Kör flera repetitioner (parallellt)
                                for (int run = 1; run <= numRuns; run++) {
                                    final int fRun = run;
                                    futures.add(pool.submit(() -> {
                                        CouzinModel sim = new CouzinModel(N, n1, n2, angle1, angle2, runTime, new Random(rng.nextLong()), useFeedback);
                                        CouzinModel.SimulationResult result = sim.runSimulation();
                                        Agent.Vec2 groupDir = result.groupDirection;
                                        double[] bbox = result.boundingBox;

                                        csv.writeRow(fRun, N, p, n1, n2, Math.toDegrees(angle1),
                                                Math.toDegrees(angle2), groupDir.x, groupDir.y, bbox[0], bbox[1]);

                                        System.out.println("Run " + fRun + " | N=" + N + " p = " + p + " n1=" + n1 + " n2=" + n2 +
                                                " angle1=" + Math.toDegrees(angle1) + " angle2=" + Math.toDegrees(angle2) +
                                                " dir=(" + groupDir.x + ", " + groupDir.y + ")");
                                        return null;
                                    }));
                                }
                            }
                        }
                    }
                } else {
                    // Mode 2: Loop over n1_values and n2_values arrays
                    for (int in1 = 0; in1 < n1_values.length(); in1++) {
                        int n1 = n1_values.getInt(in1);
                        
                        for (int in2 = 0; in2 < n2_values.length(); in2++) {
                            int n2 = n2_values.getInt(in2);
                            double p = (double) n1 / N; // Calculate p for logging
                            
                            for (int ia1 = 0; ia1 < angle1_values.length(); ia1++) {
                                double angle1 = Math.toRadians(angle1_values.getDouble(ia1));

                                for (int ia2 = 0; ia2 < angle2_values.length(); ia2++) {
                                    double angle2 = Math.toRadians(angle2_values.getDouble(ia2));

                                    // Kör flera repetitioner (parallellt)
                                    for (int run = 1; run <= numRuns; run++) {
                                        final int fRun = run;
                                        futures.add(pool.submit(() -> {
                                            CouzinModel sim = new CouzinModel(N, n1, n2, angle1, angle2, runTime, new Random(rng.nextLong()), useFeedback);
                                            CouzinModel.SimulationResult result = sim.runSimulation();
                                            Agent.Vec2 groupDir = result.groupDirection;
                                            double[] bbox = result.boundingBox;

                                            csv.writeRow(fRun, N, p, n1, n2, Math.toDegrees(angle1),
                                                    Math.toDegrees(angle2), groupDir.x, groupDir.y, bbox[0], bbox[1]);

                                            System.out.println("Run " + fRun + " | N=" + N + " p = " + p + " n1=" + n1 + " n2=" + n2 +
                                                    " angle1=" + Math.toDegrees(angle1) + " angle2=" + Math.toDegrees(angle2) +
                                                    " dir=(" + groupDir.x + ", " + groupDir.y + ")");
                                            return null;
                                        }));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Wait for all tasks
            for (Future<?> f : futures) {
                try { f.get(); } catch (Exception e) { e.printStackTrace(); }
            }
            pool.shutdown();

            csv.close();
            System.out.println("All simulations done. Results saved to " + outputCsv + " using " + numThreads + " threads.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
