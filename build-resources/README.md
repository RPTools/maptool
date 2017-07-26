MapTool
=======

Build instructions
------------------
Gradle is used to build MapTool and the build process is siginificanly
easier than 1.3. Since the gradle wrapper is being used there is no
need to download and install gradle to perform the build.

To run:

    ./gradlew maptool:run

The MapTool Launcher can not be run in this way currently.

To build (but not create the zip distributions):

    ./gradle build

To create a release zip distribution of MapTool:

    ./gradlew clean release

This will also create a \*-osx.zip file which contains a Mac OS X
app. At the moment we don't create a dmg disk image as the OSX
Gatekeeper produces a warning message when the dmg is not signed
by a registered Apple developer.

The build number is based on the git tag (as returned by
`git describe --tag`) so if you want to create a new release
you should tag it with `git tag -a <tag_name>`.

Remember to do a `git push origin <tag_name>` to push the tag
to the repository so that builds can be tied to commits.

To run unit tests:

    ./gradlew test
or

    ./gradlew check

Also *PMD* and *FindBugs* do not currently run when you do a check
as they take a while and no one is looking at the results yet.

To run FindBugs:

    ./gradlew findBugsMain

To run PMD:

    ./gradlew pmdMain

The first time that you perform a build it is likely to take quite
a while as the gradle wrapper downloads any needed components for
building. Gradle will also need to download any external dependencies
require by the MapTool code. Subsequent builds will be significantly
faster as these downloads will have been cached locally.
