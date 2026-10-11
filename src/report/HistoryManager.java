package report;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import model.FileIntegrityRecord;

public class HistoryManager {

    private static final String DEFAULT_HISTORY_PATH = "History.csv";

    // Key = filePath + "|" + algorithm to distinguish same file under different algorithms
    private final Map<String, String> previousHashes = new HashMap<>();
    private final List<FileIntegrityRecord> records = new ArrayList<>();
    private final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public HistoryManager() {
        // Load history from default CSV if it exists
        loadHistory(DEFAULT_HISTORY_PATH);
    }

    // Load previous scan history
    public void loadHistory(String path) {
        File file = new File(path);
        if (!file.exists()) return;

        // Reset current in-memory history and reload from disk
        previousHashes.clear();
        records.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { 
                    firstLine = false; // skip header
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length < 7) continue;

                String fileName = parts[0];
                String filePath = parts[1];
                String algorithm = parts[2];
                String hash = parts[3];
                boolean malicious = Boolean.parseBoolean(parts[4]);
                String integrityStatus = parts[5];
                LocalDateTime scanTime;
                try {
                    scanTime = LocalDateTime.parse(parts[6], dtFormatter);
                } catch (Exception e) {
                    scanTime = LocalDateTime.now();
                }

                // Store previous hash for integrity checking (by path + algorithm)
                String key = buildKey(filePath, algorithm);
                previousHashes.put(key, hash);

                // Add to records list (optional, GUI can show history if needed)
                records.add(new FileIntegrityRecord(fileName, filePath, algorithm, hash, malicious, integrityStatus, scanTime));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Determine file integrity status based on previous hash and algorithm
    public String checkStatus(String filePath, String algorithm, String currentHash) {
        String key = buildKey(filePath, algorithm);
        // Ensure we are up-to-date with the on-disk history before deciding
        if (!previousHashes.containsKey(key)) {
            loadHistory(DEFAULT_HISTORY_PATH);
        }
        if (!previousHashes.containsKey(key)) {
            return "New";
        } else if (previousHashes.get(key).equals(currentHash)) {
            return "Unchanged";
        } else {
            return "Modified";
        }
    }

    // Add new record and update previousHashes map
    public void addRecord(FileIntegrityRecord record) {
        String key = buildKey(record.getFilePath(), record.getHashAlgorithm());
        previousHashes.put(key, record.getHashValue());
        records.add(record);
    }

    // Export current scan history to a CSV
    public void exportHistory(String path) throws Exception {
        try (FileWriter fw = new FileWriter(path)) {
            fw.write("FileName,FilePath,Algorithm,Hash,Malicious,IntegrityStatus,ScanTime\n");
            for (FileIntegrityRecord r : records) {
                fw.write(
                        r.getFileName() + "," +
                        r.getFilePath() + "," +
                        r.getHashAlgorithm() + "," +
                        r.getHashValue() + "," +
                        r.isMalicious() + "," +
                        r.getIntegrityStatus() + "," +
                        r.getScanTime().format(dtFormatter) + "\n"
                );
            }
        }
    }

    // Append a single record to the default History.csv so future runs
    // read modification information from disk, not only in-memory cache.
    public void appendRecordToDefaultHistory(FileIntegrityRecord r) {
        try {
            File file = new File(DEFAULT_HISTORY_PATH);
            boolean writeHeader = !file.exists();
            try (FileWriter fw = new FileWriter(file, true)) {
                if (writeHeader) {
                    fw.write("FileName,FilePath,Algorithm,Hash,Malicious,IntegrityStatus,ScanTime\n");
                }
                fw.write(
                        r.getFileName() + "," +
                        r.getFilePath() + "," +
                        r.getHashAlgorithm() + "," +
                        r.getHashValue() + "," +
                        r.isMalicious() + "," +
                        r.getIntegrityStatus() + "," +
                        r.getScanTime().format(dtFormatter) + "\n"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Optional: get all records
    public List<FileIntegrityRecord> getRecords() {
        return records;
    }

    private String buildKey(String filePath, String algorithm) {
        return filePath + "|" + algorithm;
    }
}
