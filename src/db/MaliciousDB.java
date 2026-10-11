package db;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class MaliciousDB {

    private final Set<String> maliciousHashes;

    public MaliciousDB() {
        maliciousHashes = new HashSet<>();
        loadMaliciousFile();
    }

    private void loadMaliciousFile() {
        File file = new File("malicious.txt"); // ensure it's in src folder
        if (!file.exists()) {
            System.err.println("malicious.txt not found at: " + file.getAbsolutePath());
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim(); // trim and lowercase
                if (!line.isEmpty()) {
                    maliciousHashes.add(line);
                }
            }
            System.out.println("Loaded " + maliciousHashes.size() + " malicious hashes.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isMalicious(String hash) {
        if (hash == null) return false;
        return maliciousHashes.contains(hash.trim()); // trim + lowercase
    }
}
