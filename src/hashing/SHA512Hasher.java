package hashing;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class SHA512Hasher implements HashAlgorithm {

    @Override
    public String hash(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        FileInputStream fis = new FileInputStream(file);
        byte[] dataBytes = new byte[1024];
        int bytesRead;

        while ((bytesRead = fis.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, bytesRead);
        }
        fis.close();

        byte[] mdBytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : mdBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public String getAlgorithmName() {
        return "SHA-512";
    }
}
