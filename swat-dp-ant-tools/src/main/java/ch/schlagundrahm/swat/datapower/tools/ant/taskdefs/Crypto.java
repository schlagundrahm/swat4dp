/**
 * created by pshah on Mar 24, 2013
 */
package ch.schlagundrahm.swat.datapower.tools.ant.taskdefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Custom ANT task to encrypt/decrypt passwords or any other text.
 * 
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 * 
 * @see <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#impl">Implementation
 *      Requirements</a>
 * 
 */
public class Crypto extends Task {

    // task attributes
    /**
     * Input string to be encoded or decoded.
     */
    private String input;

    /**
     * Name of the property to be used to store the result.
     */
    private String property;

    /**
     * Keyfile base name
     */
    private String keyfile;

    /**
     * Either "encrypt" or "decrypt"
     */
    private String mode;

    private boolean genkey = false;
    private boolean overwrite = false;
    private boolean asymkey = false;
    private boolean useprefix = false;
    private String securityprovider = "BC";
    private String iv;
    private String salt = null;

    /**
     * Algorithm for the KeyPairGenerator
     * <ul>
     * <li>RSA (2048)</li>
     * </ul>
     */
    private String asymKeyAlgorithm = "rsa";

    private int asymKeyAlgorithmStrength = 2048;

    /**
     * Algorithm for the asymmetric cipher
     * 
     * <ul>
     * <li>RSA/ECB/PKCS1Padding (1024, 2048)</li>
     * <li>RSA/ECB/OAEPWithSHA-1AndMGF1Padding (1024, 2048)</li>
     * <li>RSA/ECB/OAEPWithSHA-256AndMGF1Padding (1024, 2048)</li>
     * <li>RSA/NONE/OAEPWithSHA-256AndMGF1Padding (1024, 2048)</li>
     * </ul>
     */
    private String asymAlgorithm = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    /**
     * Algorithm for the KeyGenerator
     * 
     * <ul>
     * <li>AES (128)</li>
     * </ul>
     */
    private String symKeyAlgorithm = "aes";

    private int symKeyAlgorithmStrength = 128;

    /**
     * Algorithm for the symmetric cipher
     * 
     * <ul>
     * <li>AES/CBC/PKCS5Padding (128)</li>
     * </ul>
     */
    private String symAlgorithm = "AES/CBC/PKCS5Padding";

    // internal variables
    private SecureRandom sr = new SecureRandom();
    private String output;

    /**
     * 
     */
    public Crypto() {

        // add at runtime the Bouncy Castle Provider
        // the provider is available only for this application
        Security.addProvider(new BouncyCastleProvider());

        // BC is the ID for the Bouncy Castle provider;
        if (Security.getProvider(securityprovider) == null) {
            log("Security provider '" + securityprovider + "' is NOT available!", Project.MSG_WARN);
        } else {
            log("Security provider '" + securityprovider + "' is available", Project.MSG_DEBUG);
        }

    }

    /**
     * @return the iv property name
     */
    public String getIv() {
        return iv;
    }

    /**
     * When set the behavior is as follows: - In case of encryption a random IV is generated and assigned to this
     * property. - In case of decryption this property needs to be set to the appropriate IV.
     * 
     * @param iv the name of the property to be used for the CBC initialization vector (IV)
     */
    public void setIv(String iv) {
        this.iv = iv;
    }

    /**
     * @param salt the salt to set
     */
    public void setSalt(String salt) {
        this.salt = salt;
    }

    /**
     * @return the property
     */
    public String getProperty() {
        return property;
    }

    /**
     * The name of the property to be used to either store the cipher or clear text i.e. the result.
     * 
     * @param property the property to set
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * @return the input
     */
    public String getInput() {
        return input;
    }

    /**
     * @return the useprefix
     */
    public boolean isUseprefix() {
        return useprefix;
    }

    /**
     * @param useprefix the useprefix to set
     */
    public void setUseprefix(boolean useprefix) {
        this.useprefix = useprefix;
    }

    /**
     * @param input the input to set
     */
    public void setInput(String input) {
        this.input = input;
    }

    /**
     * @return the keyfile
     */
    public String getKeyfile() {
        return keyfile;
    }

    /**
     * @param keyfile the keyfile to set
     */
    public void setKeyfile(String keyfile) {
        this.keyfile = keyfile;
    }

    /**
     * @return the mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * @return the securityprovider
     */
    public String getSecurityprovider() {
        return securityprovider;
    }

    /**
     * @param securityprovider the securityprovider to set
     */
    public void setSecurityprovider(String securityprovider) {
        this.securityprovider = securityprovider;
    }

    /**
     * @return the genkey
     */
    public boolean isGenkey() {
        return genkey;
    }

    /**
     * @param genkey the genkey to set
     */
    public void setGenkey(boolean genkey) {
        this.genkey = genkey;
    }

    /**
     * @return the overwrite
     */
    public boolean isOverwrite() {
        return overwrite;
    }

    /**
     * @param overwrite the overwrite to set
     */
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * @return the asymkey
     */
    public boolean isAsymkey() {
        return asymkey;
    }

    /**
     * @param asymkey the asymkey to set
     */
    public void setAsymkey(boolean asymkey) {
        this.asymkey = asymkey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {

        if (keyfile == null || keyfile.isEmpty()) {
            throw new BuildException("You have to provide the 'keyfile' attribute.");
        }

        if (!asymkey && iv != null && mode.equalsIgnoreCase("decrypt") && iv == null) {
            throw new BuildException(
                    "When a cipher has been encrpyted with a random IV , you need to provide this IV for decryption.");
        }

        if (genkey) {
            if (new File(keyfile).exists() && overwrite == false) {
                throw new BuildException(
                        "Keyfile '" + keyfile + "' already exists. Either use 'overwrite=true' or delete the file.");
            }
            if (asymkey) {
                genarateAsymKey();
            } else {
                genarateSymKey();
            }
        } else {

            if ((input != null) && (property == null) || (input == null) && (property != null)) {
                throw new BuildException("You have to provide both attributes 'input' and 'property'.");
            }

            if ((input != null) && input.isEmpty()) {
                throw new BuildException("Input is empty!");
            }

            if (asymkey && mode.equalsIgnoreCase("encrypt")) {

                PublicKey key = (PublicKey) readAsymKey(asymKeyAlgorithm, mode, keyfile);

                try {

                    output = encrypt(input, key, asymAlgorithm);
                } catch (GeneralSecurityException gsx) {
                    log("Error occured during encryption - " + gsx.getLocalizedMessage(), gsx, Project.MSG_ERR);
                    throw new BuildException("Could not encrypt cleartext!", gsx);
                }

                if (useprefix) {
                    output = getPrefix() + output;
                }

            } else if (asymkey && mode.equalsIgnoreCase("decrypt")) {

                PrivateKey key = (PrivateKey) readAsymKey(asymKeyAlgorithm, mode, keyfile);

                if (useprefix) {
                    input = input.substring(getPrefix().length());
                }

                try {
                    output = decrypt(input, key, asymAlgorithm).toString();
                } catch (GeneralSecurityException gsx) {
                    log("Error occured during decryption - " + gsx.getLocalizedMessage(), gsx, Project.MSG_ERR);
                    throw new BuildException("Could not decrypt input text!", gsx);
                }

            } else if (!asymkey) {

                SecretKey key = (SecretKey) readSymKey(symKeyAlgorithm, keyfile);

                if (mode.equalsIgnoreCase("encrypt")) {
                    try {
                        output = encrypt(input, key, symAlgorithm);
                    } catch (GeneralSecurityException gsx) {
                        log("Error occured during encryption - " + gsx.getLocalizedMessage(), gsx, Project.MSG_ERR);
                        throw new BuildException("Could not encrypt cleartext!", gsx);
                    }

                    if (useprefix) {
                        output = getPrefix() + output;
                    }

                } else if (mode.equalsIgnoreCase("decrypt")) {

                    log("input: " + input, Project.MSG_DEBUG);

                    try {
                        output = decrypt(input, key, symAlgorithm);
                    } catch (GeneralSecurityException gsx) {
                        log("Error occured during decryption - " + gsx.getLocalizedMessage(), gsx, Project.MSG_ERR);
                        throw new BuildException("Could not decrypt input text!", gsx);
                    }
                } else {
                    throw new BuildException("Invalid mode: " + mode + " -  use either 'encrypt' or 'decrypt'!");
                }

            } else {
                throw new BuildException("Invalid mode: " + mode + " -  use either 'encrypt' or 'decrypt'!");
            }

            if (getProject().getProperty(property) == null || overwrite == true) {
                getProject().setProperty(property, output);
            } else {
                throw new BuildException("Property '" + property
                        + "' already set. Either use a different property name or the 'overwrite=true' attribute.");
            }

        }
    }

    /**
     * Encrypt the given byte array using the provided key and algorithm.
     * 
     * @param toEncrypt the string to be encrypted
     * @param key either PublicKey or SecretKey to be used for the encryption
     * @param algorithm name of the cipher transformation e.g.
     * @return the Base64 encoded cipher text
     * @throws GeneralSecurityException
     */
    private String encrypt(String toEncrypt, Key key, String algorithm) throws GeneralSecurityException {

        log("toEncrypt: " + toEncrypt, Project.MSG_DEBUG);
        byte[] encodedtext = null;

        try {
            encodedtext = toEncrypt.getBytes("UTF8");
        } catch (UnsupportedEncodingException uex) {
            log("UTF8 encoding is not supported!", Project.MSG_ERR);
        }

        Cipher cipher = Cipher.getInstance(algorithm, securityprovider);

        if (algorithm.indexOf("/CBC/") > 0 && iv == null) {
            cipher.init(Cipher.ENCRYPT_MODE, key, generateIv(null));
        } else if (algorithm.indexOf("/CBC/") > 0 && iv != null && iv.length() > 0) {
            if (salt == null) {
                cipher.init(Cipher.ENCRYPT_MODE, key);
                writeIvToProperty(cipher, iv);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, key, generateIv(salt));

            }
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        }

        byte[] result = cipher.doFinal(encodedtext);
        String encodedcipher = new String(Base64.encodeBase64(result));
        log("cipher   : " + encodedcipher, Project.MSG_DEBUG);
        return encodedcipher;

    }

    private String decrypt(String toDecrypt, Key key, String algorithm) throws GeneralSecurityException {

        log("toDecrypt: " + toDecrypt, Project.MSG_DEBUG);

        toDecrypt = stripPrefix(toDecrypt);

        log("toDecrypt (without prefix): " + toDecrypt, Project.MSG_DEBUG);

        byte[] raw = Base64.decodeBase64(toDecrypt);

        Cipher deCipher = Cipher.getInstance(algorithm, securityprovider);

        if (algorithm.indexOf("/CBC/") > 0 && iv == null) {
            deCipher.init(Cipher.DECRYPT_MODE, key, generateIv(null));
        } else if (algorithm.indexOf("/CBC/") > 0 && iv != null && iv.length() > 0) {
            deCipher.init(Cipher.DECRYPT_MODE, key, readIvFromProperty(iv));
        } else if (algorithm.indexOf("/CBC/") > 0 && salt != null && salt.length() > 0) {
            deCipher.init(Cipher.DECRYPT_MODE, key, generateIv(salt));
        } else {
            deCipher.init(Cipher.DECRYPT_MODE, key);
        }

        byte[] result = deCipher.doFinal(raw);

        String cleartext = null;
        try {
            cleartext = new String(result, "UTF8");
        } catch (UnsupportedEncodingException e) {
            log("UTF8 encoding is not supported!", Project.MSG_ERR);
        }

        log("cleartext: " + cleartext, Project.MSG_DEBUG);
        return cleartext;
    }

    private Key readAsymKey(String algorithm, String mode, String file) {

        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(algorithm, securityprovider);
        } catch (GeneralSecurityException gsx) {
            log("Could not create key factory - " + gsx.getLocalizedMessage(), gsx, Project.MSG_ERR);
            throw new BuildException("Can not read key - " + gsx.getLocalizedMessage(), gsx);
        }

        log("KeyFactory Object Info: ", Project.MSG_DEBUG);
        log("  Algorithm = " + keyFactory.getAlgorithm(), Project.MSG_DEBUG);
        log("  Provider  = " + keyFactory.getProvider(), Project.MSG_DEBUG);

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException fnfx) {
            log("Can not read key file!", fnfx, Project.MSG_ERR);
        }
        int kl;
        byte[] kb;
        try {
            kl = fis.available();
            kb = new byte[kl];
            fis.read(kb);
        } catch (IOException iox) {
            log("Key file IO exception!", iox, Project.MSG_ERR);
            throw new BuildException("Key file IO exception!", iox);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log("Could not close key file!");
                }
            }
        }

        Key ky = null;
        KeySpec ks;
        try {
            if (mode.equalsIgnoreCase("encrypt")) {
                ks = new X509EncodedKeySpec(kb);
                ky = keyFactory.generatePublic(ks);
            } else if (mode.equalsIgnoreCase("decrypt")) {
                ks = new PKCS8EncodedKeySpec(kb);
                ky = keyFactory.generatePrivate(ks);
            } else {
                throw new BuildException("Invalid mode: " + mode);
            }
        } catch (InvalidKeySpecException iksx) {
            log("Invalid key spec - " + iksx.getLocalizedMessage(), iksx, Project.MSG_ERR);
            throw new BuildException("Invalid key spec!", iksx);
        }

        log("Key Object Info: ", Project.MSG_DEBUG);
        log("  Algorithm  = " + ky.getAlgorithm(), Project.MSG_DEBUG);
        log("  Saved File = " + file, Project.MSG_DEBUG);
        log("  Length     = " + kl, Project.MSG_DEBUG);
        log("  Format     = " + ky.getFormat(), Project.MSG_DEBUG);

        return ky;
    }

    private Key readSymKey(String algorithm, String file) {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException fnfx) {
            log("Can not read key file!", fnfx, Project.MSG_ERR);
            throw new BuildException("Can not read key file " + file + " - " + fnfx.getLocalizedMessage(), fnfx);
        }
        int kl;
        byte[] kb;
        try {
            kl = fis.available();
            kb = new byte[kl];
            fis.read(kb);
        } catch (IOException iox) {
            log("Key file IO exception!", iox, Project.MSG_ERR);
            throw new BuildException("Key file IO exception!", iox);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log("Could not close key file!");
                }
            }
        }

        Key ky = null;
        KeySpec ks = null;
        SecretKeyFactory kf = null;

        if (algorithm.equalsIgnoreCase("DES")) {
            try {
                ks = new DESKeySpec(kb);
                kf = SecretKeyFactory.getInstance("DES");
                ky = kf.generateSecret(ks);
            } catch (GeneralSecurityException gsx) {
                log("Could not create key factory - " + gsx.getLocalizedMessage(), gsx, Project.MSG_ERR);
                throw new BuildException("Can not read key - " + gsx.getLocalizedMessage(), gsx);
            }

        } else if (algorithm.equalsIgnoreCase("DESede")) {
            try {
                ks = new DESedeKeySpec(kb);
                kf = SecretKeyFactory.getInstance("DESede");
                ky = kf.generateSecret(ks);
            } catch (GeneralSecurityException gsx) {
                log("Could not create key factory - " + gsx.getLocalizedMessage(), gsx, Project.MSG_ERR);
                throw new BuildException("Can not read key - " + gsx.getLocalizedMessage(), gsx);
            }
        } else {
            ks = new SecretKeySpec(kb, algorithm);
            ky = new SecretKeySpec(kb, algorithm);
        }

        log("KeyFactory Object Info: ", Project.MSG_DEBUG);
        log("  Algorithm = " + (kf == null ? "n/a" : kf.getAlgorithm()), Project.MSG_DEBUG);
        log("  Provider  = " + (kf == null ? "n/a" : kf.getProvider()), Project.MSG_DEBUG);

        log("KeySpec Object Info: ", Project.MSG_DEBUG);
        log("  Saved File = " + file, Project.MSG_DEBUG);
        log("  Length     = " + kb.length, Project.MSG_DEBUG);

        log("SecretKey Object Info: ", Project.MSG_DEBUG);
        log("  Algorithm = " + ky.getAlgorithm(), Project.MSG_DEBUG);
        log("  Format    = " + ky.getFormat(), Project.MSG_DEBUG);

        return ky;
    }

    /**
     * Generate an asymmetric key pair.
     * 
     * The public key (<keyfile>.pub) will be used for encryption and the private key (<keyfile>.pri) will be used for
     * decryption.
     * 
     */
    private void genarateAsymKey() {

        KeyPairGenerator gen = null;
        try {
            gen = KeyPairGenerator.getInstance(asymKeyAlgorithm, securityprovider);
        } catch (NoSuchAlgorithmException nsax) {
            log("Key algorithm '" + asymKeyAlgorithm + "' is not supported!", Project.MSG_ERR);
            throw new BuildException("Key algorithm '" + asymKeyAlgorithm + "' is not supported!", nsax);
        } catch (NoSuchProviderException nspx) {
            log("Security provider '" + securityprovider + "' is not supported!", Project.MSG_ERR);
            throw new BuildException("Security provider '" + securityprovider + "' is not supported!", nspx);
        }
        gen.initialize(asymKeyAlgorithmStrength, sr);

        log("Generating asymmetric key pair ...", Project.MSG_INFO);
        KeyPair keyPair = gen.generateKeyPair();

        PrivateKey priKey = keyPair.getPrivate();
        PublicKey pubKey = keyPair.getPublic();

        String fl = keyfile + ".pri";
        FileOutputStream out = null;
        byte[] ky = { 0 };
        try {
            out = new FileOutputStream(fl);
            ky = priKey.getEncoded();
            out.write(ky);

        } catch (FileNotFoundException fnfx) {
            log("Can not write private key to file '" + keyfile + ".pri'!", Project.MSG_ERR);
            throw new BuildException("", fnfx);
        } catch (IOException iox) {
            log("Can not write private key to file '" + keyfile + ".pri'!", Project.MSG_ERR);
            throw new BuildException("", iox);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log("Could not close file stream!", e, Project.MSG_ERR);
                }
            }
        }

        log("Private Key Info: ", Project.MSG_DEBUG);
        log("  Algorithm  = " + priKey.getAlgorithm(), Project.MSG_DEBUG);
        log("  Saved File = " + fl, Project.MSG_DEBUG);
        log("  Size       = " + ky.length, Project.MSG_DEBUG);
        log("  Format     = " + priKey.getFormat(), Project.MSG_DEBUG);

        fl = keyfile + ".pub";
        try {
            out = new FileOutputStream(fl);
            ky = pubKey.getEncoded();
            out.write(ky);
        } catch (FileNotFoundException fnfx) {
            log("Can not write public key to file '" + keyfile + ".pub'!", Project.MSG_ERR);
            throw new BuildException("", fnfx);
        } catch (IOException iox) {
            log("Can not write public key to file '" + keyfile + ".pub'!", Project.MSG_ERR);
            throw new BuildException("", iox);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log("Could not close file stream!", e, Project.MSG_ERR);
                }
            }

        }

        log("Public Key Info: ", Project.MSG_DEBUG);
        log("  Algorithm  = " + pubKey.getAlgorithm(), Project.MSG_DEBUG);
        log("  Saved File = " + fl, Project.MSG_DEBUG);
        log("  Size       = " + ky.length, Project.MSG_DEBUG);
        log("  Format     = " + pubKey.getFormat(), Project.MSG_DEBUG);

    }

    private void genarateSymKey() {

        KeyGenerator gen = null;
        try {
            gen = KeyGenerator.getInstance(symKeyAlgorithm, securityprovider);
        } catch (NoSuchAlgorithmException nsax) {
            log("Key algorithm '" + symKeyAlgorithm + "' is not supported!", Project.MSG_ERR);
            throw new BuildException("Key algorithm '" + symKeyAlgorithm + "' is not supported!", nsax);
        } catch (NoSuchProviderException nspx) {
            log("Security provider '" + securityprovider + "' is not supported!", Project.MSG_ERR);
            throw new BuildException("Security provider '" + securityprovider + "' is not supported!", nspx);
        }
        gen.init(symKeyAlgorithmStrength, sr);

        log("Generating symmetric key ...", Project.MSG_INFO);
        SecretKey key = gen.generateKey();

        String fl = keyfile + ".key";
        FileOutputStream out = null;
        byte[] ky = { 0 };
        try {
            out = new FileOutputStream(fl);
            ky = key.getEncoded();
            out.write(ky);

        } catch (FileNotFoundException fnfx) {
            log("Can not write secret key to file '" + keyfile + ".key'!", Project.MSG_ERR);
            throw new BuildException("", fnfx);
        } catch (IOException iox) {
            log("Can not write secret key to file '" + keyfile + ".key'!", Project.MSG_ERR);
            throw new BuildException("", iox);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log("Could not close file stream!", e, Project.MSG_ERR);
                }
            }
        }

        log("Secret Key Info: ", Project.MSG_DEBUG);
        log("  Algorithm  = " + key.getAlgorithm(), Project.MSG_DEBUG);
        log("  Saved File = " + fl, Project.MSG_DEBUG);
        log("  Size       = " + ky.length, Project.MSG_DEBUG);
        log("  Format     = " + key.getFormat(), Project.MSG_DEBUG);

    }

    private String getPrefix() {
        String prefix = null;
        if (useprefix) {
            if (isAsymkey()) {
                prefix = "{" + asymKeyAlgorithm + "}";
            } else {
                prefix = "{" + symKeyAlgorithm + "}";
            }
        }
        return prefix;
    }

    private String stripPrefix(String text) {
        if (useprefix) {
            return text.substring(getPrefix().length());
        } else if (text.indexOf("{") == 0 && text.indexOf("}") > 0) {
            return text.substring(text.indexOf("}") + 1);
        } else {
            return text;
        }
    }

    private AlgorithmParameterSpec generateIv(String salt) {
        // block size = 16
        if (salt == null || salt.isEmpty()) {
            salt = "VivaSchlag&Rahm!";
        }
        byte[] ivBytes = null;
        try {
            ivBytes = salt.getBytes("UTF8");
        } catch (UnsupportedEncodingException uex) {
            throw new BuildException("Can not encode IV - " + uex.getLocalizedMessage());
        }
        return new IvParameterSpec(ivBytes);
    }

    private void writeIvToProperty(Cipher c, String propertyName) {
        getProject().setProperty(propertyName, new String(Base64.encodeBase64(c.getIV())));
    }

    private AlgorithmParameterSpec readIvFromProperty(String propertyName) {
        return new IvParameterSpec(Base64.decodeBase64(getProject().getProperty(propertyName)));
    }

    public void getProviderInfo() {
        // try {
        // Provider p[] = Security.getProviders();
        // for (int i = 0; i < p.length; i++) {
        // System.out.println(p[i]);
        // for (Enumeration<Object> e = p[i].keys(); e.hasMoreElements();)
        // System.out.println("\t" + e.nextElement());
        // }
        // } catch (Exception e) {
        // System.out.println(e);
        // }

        for (Provider provider : Security.getProviders()) {
            System.out.println(provider.getName());
            for (String key : provider.stringPropertyNames())
                System.out.println("\t" + key + "\t" + provider.getProperty(key));
        }
    }

}
