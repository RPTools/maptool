# Code Style and Guidelines

Please observe the following rules when working on MapTool. Failure to do so will typically result in your pull request being rejected.

1. The [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) is incorporated by reference. Submitted code shall follow those guidelines unless modified by one or more of the following rules.
2. Use parameter names different from member field names so that disambiguation using **this** is not necessary. This rule is relaxed for simple short methods (e.g. setters).
3. Avoid hard-coded strings in code when a property from an external file can be used. In MapTool's case, this means calling `I18N.getText(propertyKey)` and adding a definition for the `propertyKey` to `i18n.properties`. Also, all of the `show*()` methods in MapTool, such as `showError()` and `showWarning()`, take `propertyKeys` as well as strings -- only use `propertyKeys`!)
4. Use `static final String` when hard-coded strings _are_ appropriate. Examples include resources that are embedded inside the MapTool JAR, such as **unknown.png** -- the question mark image.
5. Report to the user all exceptions that are true errors. `InterruptedException` while waiting for a timer can be ignored, for example. But all other errors should be handled by calling `MapTool.showError(propertyKey)` or similar and passing both a `propertyKey` and the `Throwable` object representing the exception. Note that the various "show" methods already provide logging to the `.maptool/log.txt` file.
6. Use the language-defined static variables instead of hard-coded strings when possible. Examples include `File.separator` instead of **"/"** and `AppActions.menuShortcut` instead of **"ctrl"**.

There are surely others that you (the contributors) may want added and that we (the dev team) determine to be acceptable. Please speak up. :)

## Formatting Source Files

### Formatting with your IDE
Most IDEs include some Source Formatting functionality and using that functionality can make following the guidelines easier. You will need to ensure that using any such functionality does follow the guidelines.

Example for Eclipse:
* Source -> Format
* Source -> Reorganize Imports

An exported set of Eclipse Preferences can be found in the GitHub repo under `build-resources/eclipse`. Other IDEs/editors may be able to import those preference files.  If you create one for your preferred environment, you can always create a Pull Request to submit it to the MapTool repo.

### Formatting with Spotless
The gradle build file for MapTool includes the Spotless targets: spotlessCheck and spotlessApply. Make use of them by doing a `gradlew spotlessCheck` and/or `spotlessApply` prior to committing or pushing your changes. Spotless will enforce the majority of the rules but not all.
