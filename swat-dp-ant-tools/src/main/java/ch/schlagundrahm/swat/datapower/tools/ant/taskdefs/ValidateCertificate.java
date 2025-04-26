// Assisted by watsonx Code Assistant 
/*
 * (C) Copyright IBM Corp. 2023.
 * (C) Copyright 2009-2013 schlag&rahm AG, Switzerland. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Created by Pierce Shah
 * 
 * Ant Tools for Swat4DP
 * Schlag&rahm WebSphere Administration Toolkit for DataPower
 * 
 *
 *      https://github.com/schlagundrahm/swat4dp/tree/master/swat-dp-ant-tools
 *
 */
package ch.schlagundrahm.swat.datapower.tools.ant.taskdefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * The ValidateCertificate class is a basic ANT task that validates x509 certificates.
 * 
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 * 
 */
public class ValidateCertificate extends Task {

    private File input;
    private boolean checkValidity;
    private boolean verbose;
    private Vector<FileSet> filesets;
    private boolean failonerror;
    private String property;

    private boolean result = true;

    /**
     * ValidateCertificate constructor.
     */
    public ValidateCertificate() {
        input = null;
        checkValidity = true;
        verbose = false;
        filesets = new Vector<FileSet>();
        failonerror = true;
        property = null;
    }

    /**
     * Set the input file.
     * 
     * @param input
     */
    public void setInput(File input) {
        this.input = input;
    }

    /**
     * Set whether to check the certificate's validity.
     * 
     * @param checkValidity
     */
    public void setCheckValidity(boolean checkValidity) {
        this.checkValidity = checkValidity;
    }

    /**
     * Set whether to log verbose output.
     * 
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Set whether the build should fail if an error occurs.
     * 
     * @param failonerror
     */
    public void setFailonerror(boolean failonerror) {
        this.failonerror = failonerror;
    }

    /**
     * Set the name of the property to set with the result.
     * 
     * @param property The property name
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * Add a set of files to validate.
     * 
     * @param fileset The input file set
     */
    public void addFileSet(FileSet fileset) {
        if (!filesets.contains(fileset)) {
            filesets.add(fileset);
        }
    }

    @Override
    public void execute() throws BuildException {

        if ((input != null) && (filesets.size() > 0)) {
            throw new BuildException("You cannot supply the 'file' attribute and filesets at the same time.");
        }

        if ((input != null) && input.exists()) {

            X509Certificate cert = readCertFile(input);

            if (checkValidity) {
                result = checkValidity(cert, failonerror, 1);
            }

        } else if (filesets.size() > 0) {

            int filesProcessed = 0;
            DirectoryScanner ds;
            for (FileSet fileset : filesets) {
                ds = fileset.getDirectoryScanner(getProject());
                File dir = ds.getBasedir();
                String[] filesInSet = ds.getIncludedFiles();
                for (String filename : filesInSet) {
                    File file = new File(dir, filename);
                    X509Certificate cert = readCertFile(file);
                    filesProcessed++;
                    if (checkValidity) {
                        result = result && checkValidity(cert, failonerror, filesProcessed);
                    }
                }
            }
        }

        if (property != null && property.length() > 0) {
            getProject().setNewProperty(property, Boolean.toString(result));
        }

    }

    /**
     * Read an X.509 certificate from a file.
     * 
     * @param file The file containing the X.509 certificate
     * @return the certificate
     */
    private X509Certificate readCertFile(File file) {
        InputStream inStream = null;
        X509Certificate cert = null;
        try {
            inStream = new FileInputStream(file);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(inStream);
            if (verbose) {
                log("validating certificate file '" + file.getName() + "' ...", Project.MSG_INFO);
            }
        } catch (FileNotFoundException fnfx) {
            throw new BuildException("Can not read certificate file!", fnfx);
        } catch (CertificateException cx) {
            throw new BuildException("Failed to parse certificate file!", cx);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException iox) {
                    log("Failed to close input stream!", iox, Project.MSG_ERR);
                }
            }
        }

        if (verbose) {
            log(cert.toString(), Project.MSG_DEBUG);
        }

        return cert;

    }

    /**
     * Check the validity of an X.509 certificate.
     * 
     * @param cert
     * @param fail
     * @param index
     * @return
     */
    private boolean checkValidity(X509Certificate cert, boolean fail, int index) {
        boolean valid = true;
        try {
            cert.checkValidity();
            if (verbose) {
                log("[" + index + "] " + cert.getSubjectDN().getName() + " is VALID", Project.MSG_INFO);
            }
        } catch (CertificateExpiredException cex) {
            log("Certificate " + cert.getSubjectDN().getName() + " has expired on " + cert.getNotAfter(),
                    Project.MSG_ERR);
            valid = false;
            if (fail) {
                throw new BuildException("Certificate has expired!", cex);
            }
        } catch (CertificateNotYetValidException cnyvx) {
            log("Certificate " + cert.getSubjectDN().getName() + " is not valid before " + cert.getNotBefore(),
                    Project.MSG_ERR);
            valid = false;
            if (fail) {
                throw new BuildException("Certificate not yet valid!", cnyvx);
            }
        }

        return valid;
    }

}