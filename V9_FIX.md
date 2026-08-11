AINA v9 permanent logo fix

The latest GitHub run still used both ic_sahana_logo.png and ic_sahana_logo.xml,
so the build failed at mergeReleaseResources.

v9 changes the current launcher resource to the unique name
sahana_launcher_logo.png and points the manifest to it.

Additionally, app/build.gradle.kts contains a Gradle preBuild cleanup task that
removes any stale ic_sahana_logo.xml/svg/png before Android resource merging.
The GitHub workflow also performs the same cleanup as a second safety net.

This means the fix does not depend on successfully deleting an old file during
a manual GitHub upload.
