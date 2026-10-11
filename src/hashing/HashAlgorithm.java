package hashing;

import java.io.File;

public interface HashAlgorithm {
    // Compute the hash of a file
    String hash(File file) throws Exception;

    // Return the algorithm name (MD5, SHA-1, etc.)
    String getAlgorithmName();
}
