# JDK Setup Guide for SWAT4DP Java Tasks

## Problem

Maven build fails with error:
```
No compiler is provided in this environment. Perhaps you are running on a JRE rather than a JDK?
```

## Root Cause

You have the **JRE** (Java Runtime Environment) installed, but Maven needs the **JDK** (Java Development Kit) which includes the Java compiler (`javac`).

**Current Installation**:
- Location: `C:\Program Files\Semeru\jre-25.0.0.36-openj9`
- Type: JRE (Runtime only)
- Missing: Java compiler (`javac.exe`)

## Solution

### Option 1: Install IBM Semeru JDK (Recommended)

Since you already have IBM Semeru JRE, install the matching JDK:

1. **Download IBM Semeru JDK 25**:
   - Visit: https://developer.ibm.com/languages/java/semeru-runtimes/downloads/
   - Select: **JDK 25** (not JRE)
   - Platform: Windows x64
   - Download the installer

2. **Install the JDK**:
   - Run the installer
   - Install to: `C:\Program Files\Semeru\jdk-25.0.0.36-openj9`
   - Complete the installation

3. **Set JAVA_HOME Environment Variable**:
   
   **Option A: Using PowerShell (Current Session)**:
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Semeru\jdk-25.0.0.36-openj9"
   $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
   ```

   **Option B: Using System Settings (Permanent)**:
   - Open: System Properties → Advanced → Environment Variables
   - Under "System variables", click "New"
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Semeru\jdk-25.0.0.36-openj9`
   - Click OK
   - Edit the `Path` variable and add: `%JAVA_HOME%\bin`
   - Click OK and restart your terminal

4. **Verify Installation**:
   ```powershell
   # Check Java version
   java -version
   
   # Check compiler exists
   javac -version
   
   # Check JAVA_HOME
   echo $env:JAVA_HOME
   
   # Should output: C:\Program Files\Semeru\jdk-25.0.0.36-openj9
   ```

### Option 2: Install Oracle JDK or OpenJDK

If you prefer a different JDK distribution:

**Oracle JDK**:
- Download from: https://www.oracle.com/java/technologies/downloads/
- Install JDK 17 or later
- Set `JAVA_HOME` to installation directory

**Eclipse Temurin (OpenJDK)**:
- Download from: https://adoptium.net/
- Install JDK 17 or later
- Set `JAVA_HOME` to installation directory

## Quick Setup Script

Create a PowerShell script to set JAVA_HOME for your current session:

**File**: `set-java-home.ps1`
```powershell
# Set JAVA_HOME to your JDK installation
$env:JAVA_HOME = "C:\Program Files\Semeru\jdk-25.0.0.36-openj9"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Verify
Write-Host "JAVA_HOME: $env:JAVA_HOME"
Write-Host "Java version:"
java -version
Write-Host "`nJava compiler:"
javac -version
```

**Usage**:
```powershell
# Run before building
. .\set-java-home.ps1

# Then build
cd swat-dp-ant-tools
mvn clean install
```

## Building SWAT4DP Java Tasks

Once JDK is installed and `JAVA_HOME` is set:

### Step 1: Verify Setup

```powershell
# Check JAVA_HOME is set
echo $env:JAVA_HOME
# Should output: C:\Program Files\Semeru\jdk-25.0.0.36-openj9

# Check compiler exists
javac -version
# Should output: javac 25
```

### Step 2: Build the Tasks

```powershell
# Navigate to ant-tools directory
cd swat-dp-ant-tools

# Clean and build
mvn clean install

# Expected output:
# [INFO] BUILD SUCCESS
# [INFO] Total time: ~10 seconds
```

### Step 3: Update Dependencies in swat-dp-tools

```powershell
# Go to swat-dp-tools
cd ../swat-dp-tools
# Update JAR files in target/ant-libs
mvn generate-resources
# Verify
ls ./target/ant-libs/swat-dp-ant-tools-*.jar
```

### Step 4: Test the Tasks

```powershell
# Test Ant can find the tasks
cd ../swat-dp-tools
ant -f ant/tokenize-java.xml info

# Expected output:
# [echo] Java-based tokenization macros for swat-dp-tools
```

## Troubleshooting

### Issue: "JAVA_HOME is not set"

**Check**:
```powershell
echo $env:JAVA_HOME
```

**Fix**:
```powershell
$env:JAVA_HOME = "C:\Program Files\Semeru\jdk-25.0.0.36-openj9"
```

### Issue: "javac: command not found"

**Check**:
```powershell
where.exe javac
```

**Fix**: Ensure JDK (not JRE) is installed and `JAVA_HOME\bin` is in PATH:
```powershell
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

### Issue: Maven still uses JRE

**Check Maven's Java**:
```powershell
mvn -version
```

**Fix**: Ensure `JAVA_HOME` points to JDK before running Maven:
```powershell
$env:JAVA_HOME = "C:\Program Files\Semeru\jdk-25.0.0.36-openj9"
mvn -version
# Should show: Java home: C:\Program Files\Semeru\jdk-25.0.0.36-openj9
```

### Issue: Build fails with "package does not exist"

**Cause**: Missing dependencies

**Fix**: Check `pom.xml` has correct dependencies:
```xml
<dependency>
    <groupId>org.apache.ant</groupId>
    <artifactId>ant</artifactId>
    <version>1.10.14</version>
</dependency>
```

## Permanent Setup (Recommended)

To avoid setting `JAVA_HOME` every time:

### Windows System Environment Variables

1. Press `Win + X` → System
2. Click "Advanced system settings"
3. Click "Environment Variables"
4. Under "System variables":
   - Click "New"
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Semeru\jdk-25.0.0.36-openj9`
   - Click OK
5. Edit `Path` variable:
   - Click "Edit"
   - Click "New"
   - Add: `%JAVA_HOME%\bin`
   - Click OK
6. Click OK to close all dialogs
7. **Restart your terminal** for changes to take effect

### Verify Permanent Setup

Open a **new** PowerShell window:
```powershell
# Should show JDK path
echo $env:JAVA_HOME

# Should show version 25
java -version
javac -version

# Should show JDK path
mvn -version
```

## Alternative: Use Maven Wrapper

If you can't install JDK system-wide, use Maven Wrapper with explicit JDK:

```powershell
# Set JAVA_HOME for this session only
$env:JAVA_HOME = "C:\path\to\jdk"

# Use Maven Wrapper
.\mvnw clean install
```

## Summary

**Required**:
- ✅ JDK 17 or later (not JRE)
- ✅ `JAVA_HOME` environment variable set to JDK path
- ✅ `javac` compiler available in PATH

**Recommended**:
- IBM Semeru JDK 25 (matches your current JRE)
- Permanent `JAVA_HOME` setup in system environment variables

**After Setup**:
```powershell
cd swat-dp-ant-tools
mvn clean install
cp target/swat-dp-ant-tools-*.jar ../swat-dp-tools/lib/
```

---

**Need Help?**

If you encounter issues:
1. Verify JDK installation: `javac -version`
2. Check JAVA_HOME: `echo $env:JAVA_HOME`
3. Check Maven sees JDK: `mvn -version`
4. Ensure you're using a **new** terminal after setting environment variables