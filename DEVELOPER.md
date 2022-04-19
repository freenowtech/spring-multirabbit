# Developer's Guide
Spring MultiRabbit is released under the Apache 2.0 license.

## Building
Spring MultiRabbit depends on Java 8 (minimum, for compatibility), Maven 3, Docker and GPG. Make sure to have them
installed, so you can fully run tests. For instructions on GPG, read instructions below.

To build Spring MultiRabbit, run:
```shell
mvn clean install
```

To build Spring MultiRabbit skipping the signing of the package, run:
```shell
mvn clean install -Dgpg.skip=true
```

### GPG Configuration
To run and build locally, the developer needs to have GPG installed and configured to the package can be signed
1. Install GPG
```shell
brew install gpg // MacOS
```
2. List the current keys
```shell
gpg --list-keys 
```
3. Create a key if none is found
```shell
gpg --generate-key
```
4. Add the following to the ~/.m2/settings.xml under the `servers` section
```xml
<server>
    <id>gpg.passphrase</id>
    <passphrase>someNonProductionPassPhrase</passphrase>
</server>
```

If you already have a key, but doesn't remember the password, you can remove the key and recreate it.
1. List the keys and use the name for the ID on the next steps
```shell
gpg --list-keys
```
2. Delete the secret keys
```
gpg --delete-secret-keys "Some Name"
```
3. Delete the keys
```
gpg --delete-keys "Some Name"
```
4. Create a new key following the instructions above
