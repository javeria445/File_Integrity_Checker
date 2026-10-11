package model;

import java.time.LocalDateTime;

public class FileIntegrityRecord {

    private final String fileName;
    private final String filePath;
    private final String hashAlgorithm;
    private final String hashValue;
    private final boolean malicious;
    private final String integrityStatus;
    private final LocalDateTime scanTime;

    public FileIntegrityRecord(String fileName, String filePath, String hashAlgorithm, String hashValue,
                               boolean malicious, String integrityStatus, LocalDateTime scanTime) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.hashAlgorithm = hashAlgorithm;
        this.hashValue = hashValue;
        this.malicious = malicious;
        this.integrityStatus = integrityStatus;
        this.scanTime = scanTime;
    }

    // Getters
    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public String getHashValue() {
        return hashValue;
    }

    public boolean isMalicious() {
        return malicious;
    }

    public String getIntegrityStatus() {
        return integrityStatus;
    }

    public LocalDateTime getScanTime() {
        return scanTime;
    }
}
