
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class PassStore {

    private final Path file;

    public PassStore(Path file) {
        this.file = file;
    }

    public void add(String passId, String regNo, String name,
                    int route, double amount) throws IOException {

        try (BufferedWriter w = Files.newBufferedWriter(
                file,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

            w.write(String.join(",",
                passId,
                regNo,
                name,
                String.valueOf(route),
                String.valueOf(amount)
            ));

            w.newLine();
        }
    }

    public String[] find(String passId) throws IOException {

        if (!Files.exists(file)) {
            return null;
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] fields = line.split(",", -1);

                if (fields.length != 5) {
                    continue;
                }

                try {
                    Integer.parseInt(fields[3]);
                    Double.parseDouble(fields[4]);
                } catch (NumberFormatException e) {
                    continue;
                }

                if (fields[0].equals(passId)) {
                    return fields;
                }
            }
        }

        return null;
    }

    public List<String[]> listByRoute(int route)
            throws IOException {

        List<String[]> passes = new ArrayList<>();

        if (!Files.exists(file)) {
            return passes;
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] fields = line.split(",", -1);

                if (fields.length != 5) {
                    continue;
                }

                try {
                    int recordRoute = Integer.parseInt(fields[3]);
                    Double.parseDouble(fields[4]);

                    if (recordRoute == route) {
                        passes.add(fields);
                    }

                } catch (NumberFormatException e) {
                    // Skip damaged record
                }
            }
        }

        return passes;
    }

    public static void main(String[] args) throws IOException {

        Path f = Files.createTempFile("passes", ".txt");

        PassStore store = new PassStore(f);

        store.add("P1001", "192311001", "Arun", 12, 3500);
        Files.writeString(
            f,
            "P1002,DAMAGED LINE\n",
            StandardOpenOption.APPEND
        );
        store.add("P1003", "192311003", "Divya", 7, 3800);

        System.out.println(java.util.Arrays.toString(
            store.find("P1003")
        ));

        System.out.println(java.util.Arrays.toString(
            store.find("P1002")
        ));

        System.out.println("Route 12 Passes:");

        for (String[] pass : store.listByRoute(12)) {
            System.out.println(java.util.Arrays.toString(pass));
        }

        Files.deleteIfExists(f);
    }
}
