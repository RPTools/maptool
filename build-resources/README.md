MapTool
=======

Build instructions
------------------
Gradle is used to build MapTool and the build process is siginificanly
easier than 1.3. Since the gradle wrapper is being used there is no
need to download and install gradle to perform the build.

To run
./gradlew maptool:run
	The launcher can not be run in this way currently.

To build (but not create the zip distributions)
./gradle build

To Create a release zip distribution of MapTool
./gradlew clean release

	This will also create a *-osx.zip file which contains a Mac OS X
	app. At the moment we dont create a dmg disk image as OSX gate
	keepers warning message when its not signed by a registerd Apple
	developer is misleading.

	The build number is based on the git tag (as returned by
	git describe --tag) so if you want to create a new release
	you should tag it with git tag -a <tag name>.
	Remember to do a push origin <tag name> to push the tag
	to the repository so that builds can be tied to commits.


To Run unit tests
./gradlew test      or      ./gradlew check

	Also PMD and FindBugs do not currently run when you do a check
	as they take a while and no one is looking at the results yet.

To run FindBugs
./gradlew findBugsMain

To run PMD
./gradlew pmdMain

The first time that you perform a build it is likely to take quite
a while as the gradle wrapper downloads any needed components for
building. Gradle will also need to donwload any external dependancies
require by the MapTool code. Subsequent builds will be siginificanly
faster as these downloads will have been cached locally.
