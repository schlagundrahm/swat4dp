# SWAT4DP Maven Central Publishing Guide

This guide explains how to publish SWAT4DP artifacts (snapshots and releases) to Maven Central using the Maven Release Plugin and Sonatype's Central Publishing Maven Plugin.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Structure](#project-structure)
3. [Configuration Overview](#configuration-overview)
4. [Publishing Snapshots](#publishing-snapshots)
5. [Publishing Releases](#publishing-releases)
6. [Troubleshooting](#troubleshooting)
7. [References](#references)

---

## Prerequisites

### 1. Sonatype Account Setup

1. **Create a Sonatype JIRA account** at https://issues.sonatype.org/
2. **Create a JIRA ticket** to claim your namespace (`ch.schlagundrahm.swat.datapower`)
3. **Generate a User Token** at https://central.sonatype.com/account
   - Navigate to your account settings
   - Generate a new token (username + password pair)
   - Save these credentials securely

### 2. GPG Key Setup

You need a GPG key to sign your artifacts:

```bash
# Generate a new GPG key (if you don't have one)
gpg --gen-key

# List your keys to find the key ID
gpg --list-keys

# Publish your public key to a key server
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### 3. Maven Settings Configuration

Configure your `~/.m2/settings.xml` with your credentials:

```xml
<settings>
  <servers>
    <!-- Sonatype Central Portal credentials -->
    <server>
      <id>central</id>
      <username>YOUR_SONATYPE_TOKEN_USERNAME</username>
      <password>YOUR_SONATYPE_TOKEN_PASSWORD</password>
    </server>
    
    <!-- Legacy OSSRH credentials (for snapshots) -->
    <server>
      <id>ossrh</id>
      <username>YOUR_SONATYPE_TOKEN_USERNAME</username>
      <password>YOUR_SONATYPE_TOKEN_PASSWORD</password>
    </server>
  </servers>

  <profiles>
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <!-- GPG configuration -->
        <gpg.executable>gpg</gpg.executable>
        <gpg.keyname>Pierce Shah</gpg.keyname>
        <!-- Optional: if your key has a passphrase -->
        <gpg.passphrase>YOUR_GPG_PASSPHRASE</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```

**Security Note**: Consider using Maven's password encryption feature:
```bash
mvn --encrypt-master-password
mvn --encrypt-password
```

---

## Project Structure

Your SWAT4DP project consists of multiple modules:

```
swat4dp/
├── swat-dp-pom/              # Parent POM (v1.1.6-SNAPSHOT)
├── swat-dp-ant-tools/        # Ant tools library (v1.2.1-SNAPSHOT)
├── swat-dp-tools/            # Main tooling package (v0.3.0-SNAPSHOT)
└── swat-dp-service-templates/ # Service templates (v1.0.0-SNAPSHOT)
```

### Module Relationships

- **swat-dp-pom**: Parent POM that defines common configuration
- **swat-dp-ant-tools**: Inherits from swat-dp-pom (v1.1.5)
- **swat-dp-tools**: Inherits from swat-dp-pom (v1.1.5), depends on swat-dp-ant-tools
- **swat-dp-service-templates**: Inherits from swat-dp-pom (v1.1.2)

---

## Configuration Overview

### Parent POM Configuration (swat-dp-pom)

#### 1. Distribution Management
Distribution management is only needed if you are publishing snapshots to a custom repository. For Central Portal publishing, this section is **not needed**.

```xml
<distributionManagement>
    <snapshotRepository>
        <id>foo</id>
        <url>https://foobar.com/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

**Important Notes:**
- The `id` must match the server `id` in your `~/.m2/settings.xml`
- No separate distributionManagement configuration is needed - releases and snapshots are handled by the central-publishing-maven-plugin

#### 2. Maven Release Plugin
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-release-plugin</artifactId>
    <version>3.1.1</version>
    <configuration>
        <autoVersionSubmodules>true</autoVersionSubmodules>
        <useReleaseProfile>false</useReleaseProfile>
        <releaseProfiles>release</releaseProfiles>
        <goals>deploy</goals>
    </configuration>
</plugin>
```

#### 3. Central Publishing Maven Plugin
```xml
<plugin>
    <groupId>org.sonatype.central</groupId>
    <artifactId>central-publishing-maven-plugin</artifactId>
    <version>0.10.0</version>
    <extensions>true</extensions>
    <configuration>
        <publishingServerId>central</publishingServerId>
        <checksums>required</checksums>
        <deploymentName>${project.artifactId}-${project.version}</deploymentName>
        <autoPublish>true</autoPublish>
        <waitUntil>validated</waitUntil>
    </configuration>
</plugin>
```

**Important Notes:**
- The `publishingServerId` must match the server `id` in `distributionManagement` and in your `~/.m2/settings.xml`
- `autoPublish=true` automatically publishes releases to Maven Central after validation
- `waitUntil=validated` waits for validation before completing the deployment
- This plugin handles both snapshots and releases to the new Central Portal

#### 4. Release Profile (GPG Signing)
```xml
<profile>
    <id>release</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-gpg-plugin</artifactId>
                <version>3.2.7</version>
                <executions>
                    <execution>
                        <id>sign-artifacts</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>sign</goal>
                        </goals>
                        <configuration>
                            <bestPractices>true</bestPractices>
                            <keyname>Pierce Shah</keyname>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</profile>
```

### Child Module Configuration

Your child modules (swat-dp-ant-tools, swat-dp-tools) include:

- **maven-source-plugin**: Generates source JARs
- **maven-javadoc-plugin**: Generates Javadoc JARs

These are required for Maven Central publication.

---

## Publishing Snapshots

Snapshots are development versions that can be published frequently without going through the release process.

### Publishing a Single Module Snapshot

```bash
# Navigate to the module directory
cd swat-dp-ant-tools

# Deploy snapshot to OSSRH
mvn clean deploy
```

### Snapshot Behavior

- **Version format**: Must end with `-SNAPSHOT` (e.g., `1.2.1-SNAPSHOT`)
- **Repository**: Deployed to `https://central.sonatype.com/repository/maven-snapshots/`
- **Availability**: Immediately available for consumption
- **Overwriting**: Each deployment overwrites the previous snapshot
- **No signing required**: Snapshots don't need GPG signatures

### Using Snapshots in Other Projects

Add the snapshot repository to your consuming project's POM:

```xml
<repositories>
    <repository>
        <name>Central Portal Snapshots</name>
        <id>central-portal-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

---

## Publishing Releases

Releases are stable versions published to Maven Central. The process involves:

1. Preparing the release (version updates, tagging)
2. Performing the release (building, signing, deploying)
3. Automatic publication to Maven Central

### Release Process Overview

The Maven Release Plugin automates:
- Removing `-SNAPSHOT` from versions
- Creating a Git tag
- Building and deploying artifacts
- Incrementing to next development version

### Step-by-Step Release Process

#### 1. Ensure Clean Working Directory

```bash
# Check Git status
git status

# Commit any pending changes
git add .
git commit -m "Prepare for release"
git push
```

#### 2. Update Parent POM Version (if needed)

If child modules reference an older parent version, update them first:

```bash
# Example: Update swat-dp-ant-tools to use latest parent
cd swat-dp-ant-tools
# Edit pom.xml to update parent version to 1.1.6-SNAPSHOT
git commit -am "Update parent POM version"
git push
```

#### 3. Prepare the Release

```bash
# Navigate to the module you want to release
cd swat-dp-pom  # or swat-dp-ant-tools, swat-dp-tools, etc.

# Prepare the release
mvn release:prepare
```

**What happens during `release:prepare`:**
1. Prompts for release version (e.g., `1.1.6`)
2. Prompts for SCM tag (e.g., `swat-dp-pom-1.1.6`)
3. Prompts for next development version (e.g., `1.1.7-SNAPSHOT`)
4. Updates POM versions
5. Commits changes
6. Creates Git tag
7. Updates to next development version
8. Commits changes

**Interactive prompts example:**
```
What is the release version for "swat-dp-pom"? (ch.schlagundrahm.swat.datapower:swat-dp-pom) 1.1.6: : 
What is SCM release tag or label for "swat-dp-pom"? (ch.schlagundrahm.swat.datapower:swat-dp-pom) swat-dp-pom-1.1.6: : 
What is the new development version for "swat-dp-pom"? (ch.schlagundrahm.swat.datapower:swat-dp-pom) 1.1.7-SNAPSHOT: :
```

#### 4. Perform the Release

```bash
# Perform the release (build, sign, deploy)
mvn release:perform
```

**What happens during `release:perform`:**
1. Checks out the tagged version
2. Builds the project
3. Runs tests
4. Generates source and Javadoc JARs
5. Signs all artifacts with GPG (via release profile)
6. Deploys to Sonatype Central Portal
7. Automatically publishes to Maven Central (due to `autoPublish=true`)

#### 5. Verify Publication

After successful deployment:

1. **Check Sonatype Central Portal**: https://central.sonatype.com/publishing/deployments
2. **Wait for Maven Central sync**: Usually 15-30 minutes
3. **Verify on Maven Central**: https://central.sonatype.com/artifact/ch.schlagundrahm.swat.datapower/swat-dp-pom

#### 6. Push Changes to GitHub

```bash
# Push commits and tags
git push
git push --tags
```

### Non-Interactive Release

For CI/CD pipelines, use batch mode:

```bash
mvn release:prepare -B \
  -DreleaseVersion=1.1.6 \
  -DdevelopmentVersion=1.1.7-SNAPSHOT \
  -Dtag=swat-dp-pom-1.1.6

mvn release:perform
```

### Releasing Multiple Modules

#### Option 1: Release Parent First, Then Children

```bash
# 1. Release parent POM
cd swat-dp-pom
mvn release:prepare
mvn release:perform

# 2. Update child modules to use new parent version
cd ../swat-dp-ant-tools
# Edit pom.xml: <parent><version>1.1.6</version></parent>
git commit -am "Update to parent version 1.1.6"

# 3. Release child module
mvn release:prepare
mvn release:perform
```

#### Option 2: Multi-Module Release (if configured)

If you create a reactor POM that includes all modules:

```bash
mvn release:prepare
mvn release:perform
```

---

## Troubleshooting

### Common Issues and Solutions

#### 1. GPG Signing Fails

**Error**: `gpg: signing failed: No secret key`

**Solution**:
```bash
# List your keys
gpg --list-secret-keys

# Ensure keyname in pom.xml matches your key
# Update settings.xml with correct keyname
```

#### 2. Authentication Failure

**Error**: `401 Unauthorized`

**Solution**:
- Verify credentials in `~/.m2/settings.xml`
- Ensure server ID matches (`central` for releases, `ossrh` for snapshots)
- Regenerate token if expired

#### 3. Missing Required Metadata

**Error**: `Missing required metadata: sources, javadoc`

**Solution**:
- Ensure `maven-source-plugin` is configured
- Ensure `maven-javadoc-plugin` is configured
- Check that plugins are in the correct POM (parent or child)

#### 4. Parent POM Version Mismatch

**Error**: `Could not resolve parent`

**Solution**:
```bash
# Install parent POM locally first
cd swat-dp-pom
mvn clean install

# Then build child modules
cd ../swat-dp-ant-tools
mvn clean install
```

#### 5. Release Plugin Fails to Push

**Error**: `Failed to execute goal org.apache.maven.plugins:maven-release-plugin:3.1.1:prepare`

**Solution**:
```bash
# Ensure you have push permissions
git remote -v

# Use SSH instead of HTTPS
git remote set-url origin git@github.com:schlagundrahm/swat4dp.git
```

### Common Error: 405 Not Allowed

**Error**: `status code: 405, reason phrase: Not Allowed (405)` when deploying

**Possible Causes and Solutions**:

1. **Server ID mismatch**: The server `id` in `distributionManagement` doesn't match `publishingServerId` in the plugin configuration or the server `id` in `~/.m2/settings.xml`
   
   **Solution**: Ensure all three use the same ID (e.g., `central`):
   ```xml
   <!-- In pom.xml -->
   <distributionManagement>
       <snapshotRepository>
           <id>central</id>
           ...
   
   <plugin>
       <configuration>
           <publishingServerId>central</publishingServerId>
   
   <!-- In ~/.m2/settings.xml -->
   <server>
       <id>central</id>
   ```

2. **Invalid credentials**: Your Sonatype token is expired or incorrect
   
   **Solution**: Regenerate your token at https://central.sonatype.com/account and update `~/.m2/settings.xml`

3. **Wrong repository URL**: Using old OSSRH URLs instead of new Central Portal URLs
   
   **Solution**: Use `https://central.sonatype.com/repository/maven-snapshots/` for snapshots

#### 7. Rollback a Failed Release

```bash
# Rollback release preparation
mvn release:rollback

# Clean up
mvn release:clean

# Delete the tag if created
git tag -d swat-dp-pom-1.1.6
git push origin :refs/tags/swat-dp-pom-1.1.6
```

### Validation Checklist

Before releasing, verify:

- [ ] All tests pass: `mvn clean test`
- [ ] No SNAPSHOT dependencies (except during snapshot deployment)
- [ ] Git working directory is clean
- [ ] GPG key is available and configured
- [ ] Maven settings.xml has correct credentials
- [ ] Parent POM version is correct in child modules
- [ ] SCM connection URLs are correct
- [ ] License information is present
- [ ] Developer information is present

---

## Best Practices

### 1. Version Numbering

Follow Semantic Versioning (SemVer):
- **MAJOR.MINOR.PATCH** (e.g., `1.2.1`)
- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes

### 2. Release Frequency

- **Snapshots**: Deploy frequently during development
- **Releases**: Only when features are stable and tested

### 3. Parent POM Management

- Release parent POM before child modules
- Keep child modules synchronized with parent version
- Document parent version requirements

### 4. Git Workflow

```bash
# Create a release branch
git checkout -b release/1.1.6

# Perform release
mvn release:prepare
mvn release:perform

# Merge back to main
git checkout main
git merge release/1.1.6
git push
```

### 5. Documentation

- Update CHANGELOG.md with release notes
- Tag releases with descriptive messages
- Document breaking changes

---

## Quick Reference Commands

### Snapshot Deployment
```bash
# Single module
mvn clean deploy

# All modules
mvn clean deploy -pl swat-dp-pom,swat-dp-ant-tools,swat-dp-tools
```

### Release Deployment
```bash
# Prepare release
mvn release:prepare

# Perform release
mvn release:perform

# Rollback if needed
mvn release:rollback
mvn release:clean
```

### Verification
```bash
# Check what will be deployed
mvn clean verify -Prelease

# Dry-run release
mvn release:prepare -DdryRun=true
mvn release:clean
```

---

## References

- **Maven Release Plugin**: https://maven.apache.org/maven-release/maven-release-plugin/
- **Central Publishing Maven Plugin**: https://central.sonatype.org/publish/publish-portal-maven/
- **Sonatype Central Portal**: https://central.sonatype.com/
- **Maven Central Repository**: https://central.sonatype.com/
- **GPG Documentation**: https://www.gnupg.org/documentation/
- **Reference Implementation**: https://github.com/teamlead/java-maven-sonatype-starter

---

## Support

For issues specific to SWAT4DP:
- **GitHub Issues**: https://github.com/schlagundrahm/swat4dp/issues
- **Email**: pshah@schlagundrahm.ch

For Maven Central publishing issues:
- **Sonatype Support**: https://central.sonatype.org/support/
- **Community Forum**: https://community.sonatype.com/