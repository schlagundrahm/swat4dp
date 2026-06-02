package ch.schlagundrahm.swat.datapower.tools.ant.taskdefs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test class for ValidateCertificate Ant task.
 * 
 * @author Pierce Shah
 */
class ValidateCertificateTest {

    @TempDir
    File tempDir;

    private Project project;

    @BeforeEach
    void setUp() {
        project = new Project();
    }

    /**
     * Create a self-signed certificate for testing.
     */
    private File createTestCertificate(String filename, long validityDays) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        X500Name issuer = new X500Name("CN=Test Certificate");
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000)); // Yesterday
        Date notAfter = new Date(System.currentTimeMillis() + (validityDays * 24 * 60 * 60 * 1000));

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serial,
                notBefore,
                notAfter,
                issuer,
                keyPair.getPublic());

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certBuilder.build(signer));

        File certFile = new File(tempDir, filename);
        try (FileOutputStream fos = new FileOutputStream(certFile)) {
            fos.write(cert.getEncoded());
        }

        return certFile;
    }

    /**
     * Create an expired certificate for testing.
     */
    private File createExpiredCertificate(String filename) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        X500Name issuer = new X500Name("CN=Expired Certificate");
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date(System.currentTimeMillis() - (365 * 24 * 60 * 60 * 1000L)); // 1 year ago
        Date notAfter = new Date(System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)); // 30 days ago

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serial,
                notBefore,
                notAfter,
                issuer,
                keyPair.getPublic());

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certBuilder.build(signer));

        File certFile = new File(tempDir, filename);
        try (FileOutputStream fos = new FileOutputStream(certFile)) {
            fos.write(cert.getEncoded());
        }

        return certFile;
    }

    @Test
    @DisplayName("Validate a valid certificate")
    void testValidCertificate(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File certFile = createTestCertificate("valid.crt", 365);

        ValidateCertificate task = new ValidateCertificate();
        task.setProject(project);
        task.setInput(certFile);
        task.setCheckValidity(true);
        task.setVerbose(true);
        task.execute();

        System.out.println("Valid certificate test completed successfully");
    }

    @Test
    @DisplayName("Fail on expired certificate with failonerror=true")
    void testExpiredCertificateFailOnError(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File certFile = createExpiredCertificate("expired.crt");

        ValidateCertificate task = new ValidateCertificate();
        task.setProject(project);
        task.setInput(certFile);
        task.setCheckValidity(true);
        task.setFailonerror(true);

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    @DisplayName("Continue on expired certificate with failonerror=false")
    void testExpiredCertificateNoFailOnError(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File certFile = createExpiredCertificate("expired.crt");

        ValidateCertificate task = new ValidateCertificate();
        task.setProject(project);
        task.setInput(certFile);
        task.setCheckValidity(true);
        task.setFailonerror(false);
        task.setProperty("cert.valid");
        task.execute();

        String result = project.getProperty("cert.valid");
        System.out.println("Certificate validity result: " + result);
        assertEquals("false", result);
    }

    @Test
    @DisplayName("Validate certificate without checking validity")
    void testValidateWithoutCheckingValidity(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File certFile = createExpiredCertificate("expired.crt");

        ValidateCertificate task = new ValidateCertificate();
        task.setProject(project);
        task.setInput(certFile);
        task.setCheckValidity(false);
        task.execute();

        // Should complete without error even though certificate is expired
        System.out.println("Validation without checking validity completed successfully");
    }

    @Test
    @DisplayName("Validate multiple certificates using fileset")
    void testValidateFileset(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createTestCertificate("cert1.crt", 365);
        createTestCertificate("cert2.crt", 180);
        createTestCertificate("cert3.crt", 90);

        FileSet fileset = new FileSet();
        fileset.setDir(tempDir);
        fileset.setIncludes("*.crt");
        fileset.setProject(project);

        ValidateCertificate task = new ValidateCertificate();
        task.setProject(project);
        task.addFileSet(fileset);
        task.setCheckValidity(true);
        task.setVerbose(true);
        task.execute();

        System.out.println("Fileset validation completed successfully");
    }

    @Test
    @DisplayName("Set property with validation result")
    void testSetPropertyWithResult(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File certFile = createTestCertificate("valid.crt", 365);

        ValidateCertificate task = new ValidateCertificate();
        task.setProject(project);
        task.setInput(certFile);
        task.setCheckValidity(true);
        task.setProperty("cert.valid");
        task.execute();

        String result = project.getProperty("cert.valid");
        System.out.println("Certificate validity result: " + result);
        assertEquals("true", result);
    }

    @Test
    @DisplayName("Fail when both file and fileset provided")
    void testFileAndFilesetConflict(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File certFile = createTestCertificate("valid.crt", 365);

        FileSet fileset = new FileSet();
        fileset.setDir(tempDir);
        fileset.setProject(project);

        ValidateCertificate task = new ValidateCertificate();
        task.setProject(project);
        task.setInput(certFile);
        task.addFileSet(fileset);

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("cannot supply"));
    }

    @Test
    @DisplayName("Handle missing certificate file")
    void testMissingFile(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File nonExistent = new File(tempDir, "nonexistent.crt");

        ValidateCertificate task = new ValidateCertificate();
        task.setProject(project);
        task.setInput(nonExistent);
        task.execute();

        // Should log message but not throw exception
        System.out.println("Missing file handled gracefully");
    }

    @Test
    @DisplayName("Validate with verbose output enabled")
    void testVerboseOutput(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File certFile = createTestCertificate("valid.crt", 365);

        ValidateCertificate task = new ValidateCertificate();
        task.setProject(project);
        task.setInput(certFile);
        task.setCheckValidity(true);
        task.setVerbose(true);
        task.execute();

        System.out.println("Verbose validation completed successfully");
    }

    @Test
    @DisplayName("Validate multiple certificates with mixed validity")
    void testMixedValidityFileset(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createTestCertificate("valid1.crt", 365);
        createExpiredCertificate("expired1.crt");
        createTestCertificate("valid2.crt", 180);

        FileSet fileset = new FileSet();
        fileset.setDir(tempDir);
        fileset.setIncludes("*.crt");
        fileset.setProject(project);

        ValidateCertificate task = new ValidateCertificate();
        task.setProject(project);
        task.addFileSet(fileset);
        task.setCheckValidity(true);
        task.setFailonerror(false);
        task.setProperty("certs.valid");
        task.execute();

        String result = project.getProperty("certs.valid");
        System.out.println("Certificates validity result: " + result);
        assertEquals("false", result); // Should be false because one cert is expired
    }

    @Test
    @DisplayName("Validate empty fileset")
    void testEmptyFileset(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        FileSet fileset = new FileSet();
        fileset.setDir(tempDir);
        fileset.setIncludes("*.crt");
        fileset.setProject(project);

        ValidateCertificate task = new ValidateCertificate();
        task.setProject(project);
        task.addFileSet(fileset);
        task.setCheckValidity(true);
        task.execute();

        // Should complete without error
        System.out.println("Empty fileset validation completed successfully");
    }
}

// Made with Bob
