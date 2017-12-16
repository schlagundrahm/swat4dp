package ch.schlagundrahm.swat.datapower.tools.ant.taskdefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tools.ant.BuildException;

public class FileHashTest {

    public static void main(String[] args) {
        try {
            calculateFileHash(new File(args[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void calculateFileHash(File file) throws IOException {

        InputStream is = new FileInputStream(file);

        byte[] hash = null;
        String hash2 = null;
        try {
            hash = DigestUtils.sha(is);
            System.out.println(hash);
            hash2 = DigestUtils.shaHex(is);
            System.out.println(hash2);
        } finally {
            if (is != null) {
                is.close();
            }
        }

        if (hash == null) {
            throw new BuildException("Could not calcualte SHA-1 hash for file: " + file);
        }

        System.out.println(Base64.encodeBase64String(hash).trim());
        // System.out.println( Base64.(hash2).trim());
    }

}
