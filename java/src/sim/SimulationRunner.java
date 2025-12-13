package sim;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Random;

public class SimulationRunner {

    public static void main(String[] args) {

        try {
            // Läs JSON
            String jsonString = new String(Files.readAllBytes(Paths.get("config_experiment_1.json")));
            JSONObject config = new JSONObject(jsonString);

            String outputCsv = config.getString("output_csv");
            int numRuns = config.getInt("num_runs");
            int runTime = config.getInt("run_time");

            JSONArray N_values = config.getJSONArray("N_values");
            JSONArray n1_values = config.getJSONArray("n1_values");
            JSONArray n2_values = config.getJSONArray("n2_values");
            JSONArray p_values = config.getJSONArray("p_values");
            JSONArray angle1_values = config.getJSONArray("angle1_deg_values");
            JSONArray angle2_values = config.getJSONArray("angle2_deg_values");

            CsvWriter csv = new CsvWriter(outputCsv);
            csv.writeHeader("run", "N",  "p", "n1", "n2", "angle1_deg", "angle2_deg", "dirX", "dirY", "bbox_X", "bbox_Y");

            Random rng = new Random();

            // Loopa över alla kombinationer
            for (int iN = 0; iN < N_values.length(); iN++) {
                int N = N_values.getInt(iN);

                for (int ip = 0; ip < p_values.length(); ip++) {
                    double p = p_values.getDouble(ip);
                    int n1 = (int) (p * N); // number of informed agents of type 1
                    int n2 = 0;
                        for (int ia1 = 0; ia1 < angle1_values.length(); ia1++) {
                            double angle1 = Math.toRadians(angle1_values.getDouble(ia1));

                            for (int ia2 = 0; ia2 < angle2_values.length(); ia2++) {
                                double angle2 = Math.toRadians(angle2_values.getDouble(ia2));

                                // Kör flera repetitioner
                                for (int run = 1; run <= numRuns; run++) {
                                        CouzinModel sim = new CouzinModel(N, n1, n2, angle1, angle2, runTime, rng);
                                        CouzinModel.SimulationResult result = sim.runSimulation();
                                        Agent.Vec2 groupDir = result.groupDirection;
                                        double[] bbox = result.boundingBox;

                                    csv.writeRow(run, N, p, n1, n2, Math.toDegrees(angle1),
                                            Math.toDegrees(angle2), groupDir.x, groupDir.y, bbox[0], bbox[1]);

                                    System.out.println("Run " + run + " | N=" + N + " p = " + p + " n1=" + n1 + " n2=" + n2 +
                                            " angle1=" + Math.toDegrees(angle1) + " angle2=" + Math.toDegrees(angle2) +
                                            " dir=(" + groupDir.x + ", " + groupDir.y + ")");
                                }
                            }
                        }
                    }
                }
            

            csv.close();
            System.out.println("All simulations done. Results saved to " + outputCsv);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
