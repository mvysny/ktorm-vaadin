# Developing

Please feel free to open bug reports to discuss new features; PRs are welcome as well :)

# Running tests

To run all tests on all Vaadin versions, simply run `./gradlew test`.

# Releasing

To release the library to Maven Central:

1. Edit `build.gradle.kts` and remove `-SNAPSHOT` in the `version=` stanza, e.g. "0.2"
2. Edit `README.md`: bump the version in the Gradle install snippet under "Using In Your Apps" to match the release
3. Run `./gradlew clean build publish closeAndReleaseStagingRepositories`
4. (Optional) watch [Maven Central Publishing Deployments](https://central.sonatype.com/publishing/deployments) as the deployment is published.
5. Commit with the commit message of simply being the version being released, e.g. "0.2"
6. git tag the commit with the same tag name as the commit message above, e.g. `0.2`
7. `git push`, `git push --tags`
8. Add the `-SNAPSHOT` back to the `version=` while increasing the version to something which will be released in the future,
   e.g. 0.3-SNAPSHOT, then commit with the commit message "0.3-SNAPSHOT" and push.
