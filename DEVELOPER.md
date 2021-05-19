# Spring MultiRabbit - Developer's guide
Spring MultiRabbit is released under the Apache 2.0 license.
If you would like to contribute some work, or propose fixes, this document can provide you instructions.

## How to Contribute
- Create an issue in the repository explaining your contribution 
- Fork the repository to your own GitHub account 
- Create a PR to push your code to Spring MultiRabbit's `main` branch. If it's a change that should be limited to a
  specific release, create the PR to the specific release instead (e.g. `release/2.5`)

## Building
Spring MultiRabbit depends on Java 8 (minimum, for compatibility), Maven 3, Docker and GPG.
Make sure to have them installed, so you can fully run tests.
For instructions on GPG, read instructions below.

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
gpg --delete-secret-keys "Wander Costa"
```
3. Delete the keys
```
gpg --delete-keys "Wander Costa"
```
4. Create a new key following the instructions above
