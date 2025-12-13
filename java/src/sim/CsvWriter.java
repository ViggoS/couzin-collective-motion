package sim;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CsvWriter {

    private PrintWriter writer;
    private boolean headerWritten = false;

    public CsvWriter(String filename) throws IOException {
        writer = new PrintWriter(new FileWriter(filename, true)); // append mode
    }

    public void writeHeader(String... headers) {
        if (!headerWritten) {
            writer.println(String.join(",", headers));
            headerWritten = true;
        }
    }

    public void writeRow(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i].toString());
            if (i < values.length - 1) sb.append(",");
        }
        writer.println(sb.toString());
    }

    public void close() {
        writer.close();
    }
}
