
import java.io.*;
import java.nio.file.*;

public class BufferedCounter {

    static long countUnbuffered(Path file) throws IOException {

        long lines = 0;

        try (InputStream input = new FileInputStream(file.toFile())) {

            int data;

            while ((data = input.read()) != -1) {
                if (data == '\n') {
                    lines++;
                }
            }
        }

        return lines;
    }

    static long countBuffered(Path file) throws IOException {

        long lines = 0;

        try (BufferedReader reader =
                 Files.newBufferedReader(file)) {

            while (reader.readLine() != null) {
                lines++;
            }
        }

        return lines;
    }

    static void createFile(Path file, int sizeMB)
            throws IOException {

        byte[] data = new byte[1024 * 1024];

        java.util.Arrays.fill(data, (byte) 'A');

        try (OutputStream out =
                 new BufferedOutputStream(
                     Files.newOutputStream(file))) {

            for (int i = 0; i < sizeMB; i++) {
                out.write(data);
                out.write('\n');
            }
        }
    }

    public static void main(String[] args) throws IOException {

        int[] sizes = {1, 10, 50};

        for (int size : sizes) {

            Path file = Files.createTempFile("attendance", ".txt");

            createFile(file, size);

            long start = System.nanoTime();
            long unbufferedLines = countUnbuffered(file);
            long end = System.nanoTime();

            long unbufferedTime = end - start;

            start = System.nanoTime();
            long bufferedLines = countBuffered(file);
            end = System.nanoTime();

            long bufferedTime = end - start;

            System.out.println("\nFile Size: " + size + " MB");

            System.out.println(
                "Unbuffered Lines: " + unbufferedLines
            );

            System.out.println(
                "Unbuffered Time: " +
                unbufferedTime / 1_000_000.0 + " ms"
            );

            System.out.println(
                "Buffered Lines: " + bufferedLines
            );

            System.out.println(
                "Buffered Time: " +
                bufferedTime / 1_000_000.0 + " ms"
            );

            Files.deleteIfExists(file);
        }
    }
}
