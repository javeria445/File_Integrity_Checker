package main;

import db.MaliciousDB;
import hashing.HashAlgorithm;
import java.io.File;
import java.time.LocalDateTime;
import model.FileIntegrityRecord;
import report.HistoryManager;

public class IntegrityChecker {

    private final HashAlgorithm hasher;
    private final HistoryManager history;
    private final MaliciousDB db;

    public IntegrityChecker(HashAlgorithm hasher, HistoryManager history, MaliciousDB db) {
        this.hasher = hasher;
        this.history = history;
        this.db = db;
    }

    public FileIntegrityRecord checkFile(String path) throws Exception {
        File f = new File(path);

        String hash = hasher.hash(f).trim();
        boolean malicious = db.isMalicious(hash);

        // Check previous integrity status
        String status = history.checkStatus(path, hasher.getAlgorithmName(), hash);

        // Create record with status
        FileIntegrityRecord record = new FileIntegrityRecord(
                f.getName(),
                f.getAbsolutePath(),
                hasher.getAlgorithmName(),
                hash,
                malicious,
                status,
                LocalDateTime.now()
        );

        history.addRecord(record); // store in memory
        history.appendRecordToDefaultHistory(record); // also persist to History.csv
        return record;
    }
}
