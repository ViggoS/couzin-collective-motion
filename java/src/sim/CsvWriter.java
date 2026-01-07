package sim;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CsvWriter {

    private PrintWriter writer;
    private boolean headerWritten = false;
    private final Object lock = new Object();

    public CsvWriter(String filename) throws IOException {
        File file = new File(filename);
        boolean append = file.exists() && file.length() > 0;
        headerWritten = append; // skip header if file already populated
        writer = new PrintWriter(new FileWriter(file, true));
    }

    public void writeHeader(String... headers) {
        synchronized (lock) {
            if (!headerWritten) {
                writer.println(String.join(",", headers));
                headerWritten = true;
                writer.flush();
            }
        }
    }

    public void writeRow(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i].toString());
            if (i < values.length - 1) sb.append(",");
        }
        synchronized (lock) {
            writer.println(sb.toString());
            writer.flush();
        }
    }

    public void close() {
        writer.close();
    }
}
