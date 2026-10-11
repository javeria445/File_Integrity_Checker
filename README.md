# Secure File Integrity Checker

A Java-based desktop application for verifying file integrity, detecting known malicious files through hash comparison, and monitoring file modifications over time. Developed as an Object-Oriented Programming (OOP) project for the **BS Cyber Security program at COMSATS University Islamabad**.

## Project Overview

The Secure File Integrity Checker helps users determine whether files have been modified, identify files matching known malicious hashes, and maintain historical records of file scans.

The application combines cryptographic hashing, integrity verification, persistent history management, and a graphical user interface to provide a practical file security solution.

## Features

- **Multiple Hash Algorithms:** Supports MD5, SHA-1, SHA-256, and SHA-512.
- **File Integrity Verification:** Detects changes by comparing current file hashes with previous scan records.
- **Malicious File Detection:** Compares computed hashes against a locally maintained malicious hash database.
- **Batch File Processing:** Allows users to scan multiple files in one session.
- **Drag-and-Drop Support:** Add files directly through the graphical interface.
- **Scan History:** Maintains historical records using CSV storage.
- **Real-Time Search:** Filter scan results across table columns.
- **Export Functionality:** Export scan history for reporting and record keeping.
- **Interactive GUI:** Provides color-coded status indicators and convenient file management controls.

## Technologies Used

- **Programming Language:** Java
- **GUI Framework:** Java Swing
- **Cryptography:** Java `MessageDigest` API
- **Data Structures:** HashSet, HashMap
- **Data Persistence:** CSV files
- **Software Design:** Object-Oriented Programming and the Strategy Design Pattern

## Supported Hash Algorithms

| Algorithm | Digest Length | Purpose |
|---|---:|---|
| MD5 | 128 bits | Legacy compatibility |
| SHA-1 | 160 bits | Legacy compatibility |
| SHA-256 | 256 bits | General-purpose integrity verification |
| SHA-512 | 512 bits | Stronger hash output for integrity verification |

**Note:** MD5 and SHA-1 are retained for compatibility but are not recommended for security-sensitive collision-resistance requirements. SHA-256 or SHA-512 is preferred for modern integrity verification.

## How It Works

1. **Select Files:** Choose files using the file browser or drag-and-drop interface.
2. **Compute Hashes:** Generate cryptographic hashes using the selected algorithm.
3. **Check Malicious Hashes:** Compare computed hashes against entries in `malicious.txt`.
4. **Verify Integrity:** Compare current hashes with historical records stored in `History.csv`.
5. **Display Results:** View file hashes, malicious status, and integrity status in the GUI.
6. **Save and Export:** Preserve scan history and export records when required.

## Integrity Statuses

| Status | Meaning |
|---|---|
| New | No previous scan record exists for the file and selected algorithm. |
| Unchanged | The current hash matches the previous recorded hash. |
| Modified | The current hash differs from the previous recorded hash. |

The application also indicates whether a file's hash matches an entry in the local malicious hash database.

**Important:** A file not found in the local database is not automatically guaranteed to be safe. Detection depends on the database's contents and freshness.

## Project Structure

```text
Secure-File-Integrity-Checker/
├── src/
│   ├── gui/
│   │   └── FileCheckerGUI.java
│   ├── hashing/
│   │   ├── HashAlgorithm.java
│   │   ├── Hasher.java
│   │   ├── MD5Hasher.java
│   │   ├── SHA1Hasher.java
│   │   ├── SHA256Hasher.java
│   │   └── SHA512Hasher.java
│   ├── db/
│   │   └── MaliciousDB.java
│   ├── main/
│   │   └── IntegrityChecker.java
│   ├── model/
│   │   └── FileIntegrityRecord.java
│   ├── report/
│   │   └── HistoryManager.java
│   ├── malicious.txt
│   └── History.csv
└── README.md
```

*The structure above follows the project report. Actual filenames and directories may differ slightly in the repository.*

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or later, depending on the project's implementation.
- A Java-compatible operating system, such as Windows, Linux, or macOS.
- The project source files and required resources, including `malicious.txt`.

### Running the Application

1. Clone or download this repository.
2. Open the project in a Java IDE, such as IntelliJ IDEA, Eclipse, or NetBeans.
3. Ensure that the source files are organized according to their Java packages.
4. Make sure `malicious.txt` is available at the path expected by the application.
5. Compile the project.
6. Run the `main()` method of `gui.FileCheckerGUI`, or use the configured main class in your IDE.

**Note:** Exact compilation and execution commands depend on the actual project structure and whether a build configuration is included.


## Limitations

- Malicious file detection depends on the available local hash database.
- Hash matching cannot identify every unknown or modified malware sample.
- MD5 and SHA-1 have known collision weaknesses.
- CSV-based persistence offers fewer querying and concurrency capabilities than a dedicated database.
- File modification tracking depends on historical records being retained and correctly associated with files and algorithms.

## Future Improvements

Potential enhancements include:

- SQLite database integration.
- Scheduled integrity scans.
- Notifications for unexpected file modifications.
- Integration with trusted threat intelligence services.
- Support for additional algorithms, including SHA-3.
- PDF report generation.
- Automated testing and improved error handling.
- Enhanced monitoring and reporting features.



##**Authors:
- Noor Saba Basit — FA24-BCT-034
- Javeria Anis — FA24-BCT-051


## Disclaimer

This project was developed for academic and educational purposes. Malicious hash matching is a supplementary detection technique and should not be treated as a complete antivirus solution or a guarantee that a file is safe.
