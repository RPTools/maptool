# Contributing to MapTool

Thank you for your interest in contributing to MapTool! This document provides guidelines and
information about contributing to the project.

The following is a set of guidelines for contributing to MapTool. These are mostly guidelines, not
rules. Use your best judgement,
and feel free to propose changes to this document via a pull request.

This project and everyone participating in it is governed by
the [RPTools Code Of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected
to uphold this code. Also please remember that everyone is contributing in their spare time so some
times it may take a day to answer any questions you have.

## Ways to Contribute

MapTool welcomes contributions from anyone who is interested in helping out. You don't need to be a
programmer to contribute. Here are several ways you can help:

* **[Reporting Bugs](#reporting-bugs)** - Help identify bugs in MapTool
* **[Suggesting Enhancements](#suggesting-enhancements)** - Help improve MapTool by suggesting new
  features
* **[Testing](#testing)** - Help test new features and report bugs
* **[Translations](#translations)** - Help translate MapTool into different languages
* **[Documentation](#documentation)** - Improve existing documentation or create new guides
* **[Asset Creation](#asset-creation)** - Create tokens, states, maps, and other assets
* **[Code Contributions](#code-contributions)** - Contribute bug fixes or new features


* [Your First Code Contribution](#your-first-code-contribution)
* [Pull Requests](#pull-requests)

[Styleguides](#styleguids)

* [Coding Styleguides](#coding-styleguides)

[Asking Questions](#asking-questions)

### Reporting Bugs

#### Before submitting a Bug Report

* **Perform a [cursory search][bug-issue]** to see if the problem has already been reported.
  if it has and the issue is still open add a comment to the existing issue rather than opening a
  new one.

### How do I Submit a Bug Report?

Bugs are tracked as [GitHub Issues](https://guides.github.com/features/issues/). To create a Bug
Report use
the [Bug Report Issue Template](https://github.com/RPTools/maptool/issues/new?assignees=&labels=bug&template=bug_report.md&title=).
Explain the problem and include additional details to help maintainers reproduce the problem.

* **Use a clear descriptive title** for the issue to identify the problem.
* **Describe the exact steps which reproduce the problem** in as much detail as possible. This will
  help us find and fix the problem faster.
* **Explain which behavior you expected to see and why**
* **Include screenshots and animated GIFs** (where relevant) which show you following the steps and
  the error that occurred.
* **If a problem wasn't triggered by a specific action**, describe what you were doing before the
  problem happened.
* **Provide the details of the Operating System and MapTool version** you are running.

Provide more context by answering these questions:

* **Did the problem start happening recently**, for example after upgrading to a new version, or was
  this always a problem?
* **Can you reliably reproduce the issue?** If not, provide details about how often the problem
  happens and under which conditions it normally happens.
* **If the problem is related to assets (for example images)** does the problem happen with all
  assets of this type or only certain ones?

### Suggesting Enhancements

This section guides you through submitting an enhancement suggestion/feature request for MapTool,
including completely new features and minor improvements
to existing functionality.

#### Before Suggesting an Enhancement.

* **Perform a [cursory search][feature-issue]** to see if the problem has already been reported.

### How Do I Submit an Enhancement Suggestion?

Feature requests are tracked as [GitHub Issues](https://guides.github.com/features/issues/). To
create a Feature Request use
the [Feature Request Issue Template](https://github.com/RPTools/maptool/issues/new?assignees=&labels=feature&template=feature_request.md&title=).

Explain your request

* **Use a clear and descriptive title** for the issue to identify the suggestion.
* **Provide a step-by-step description of the enhancement** in as much detail as possible.
* **Provide specific examples to demonstrate**
* **Describe the current behavior** and **explain which behavior you expected to see instead** and
  why.
* **Include screenshots and animated GIFs** which help to demonstrate the steps or point out how
  things could work.
* **Explain why this enhancement would be useful** to other MapTool users.
* **Specify which version of MapTool you are using.**

### Testing

We can never have enough testing. If you would like to help test new features or bug fixes,
please download the latest development build and report any issues you find. You can also join our
[discord](discord.gg/dZy7HeYYVY) server to discuss testing and assign yourself the Testing role.

### Translation

MapTool uses [crowdin](https://crowdin.com/project/maptool) for translations. If you would like
to help translate MapTool into your language, please visit the crowdin project page and create an
account. You can then request to join the MapTool translation team. There is also an area on our
[discord](discord.gg/dZy7HeYYVY) to discuss translations.

### Asset Creation

New Tokens, States, and Maps, and Tile packs are always welcome. If you would like to contribute
assets to MapTool, please log into discord and ask about how to contribute assets.

### Documentation

Improving documentation is always welcome. If you would like to help improve the documentation
you can create an account on the [RPTools Wiki](https://wiki.rptools.info/index.php/Main_Page)
and start editing, or you can create a pull request with the changes you would like to see in the
documentation on the [MapTool GitHub Repository](https://github.com/RPTools/maptool), or the
[MapTool GitHub Wiki](https://github.com/RPTools/maptool/wiki). There is also an area on
our [discord](discord.gg/dZy7HeYYVY) server to discuss documentation.

### Asset Creation

New Tokens, States, and Maps, and Tile packs are always welcome. If you would like to contribute
assets to MapTool, please log into discord and ask about how to contribute assets.

### Code Contributions

* Java Development Kit (JDK) from Eclipse Temurin (formerly AdoptOpenJDK)
* An IDE (IntelliJ IDEA recommended)
* [How To Setup User Interface (UI) Tools for MapTool](docs/How_To_Setup_User_Interface_Tools_for_MapTool.md) (
  This is optional as it is only
  required if you want to make modifications to the user interface.)

For detailed setup instructions, please visit
our [Contributor Setup Guide](https://github.com/RPTools/maptool/wiki/Contributor-Setup-Instructions-For-MapTool).

#### Code Style and Guidelines

Please read the [Code Style and Guidelines][coding-style-guides] for MapTool before contributing.

We follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) with

#### Code Formatting

* Use Spotless for code formatting: Run `gradlew spotlessCheck` and `spotlessApply` before
  committing

#### Pull Request Process

1. Fork the repository and create a new branch for your feature or fix
2. Ensure your code follows our style guidelines
3. Write clear commit messages
4. Include tests if applicable
5. Update documentation as needed
6. Submit a pull request with a clear description of the changes

#### Before Submitting

- [ ] Run Spotless to ensure code formatting
- [ ] Test your changes thoroughly
- [ ] Update relevant documentation
- [ ] Ensure all tests pass
- [ ] Review your changes for potential issues

#### Your First Code Contribution

Unsure where to begin contributing code to MapTool? You can start by looking through these
`a good first issue` and `up for grabs` issues.

* [Good First Issues][good-first-issue] - issues which should be good for people who are not yet
  familiar with MapTool code base.
* [Up For Grabs Issues][up-for-grabs] - issues which require a little but not much familiarity with
  the MapTool code base and don't require extensive modifications.

#### Local Development

MapTool can be developed locally, for instructions on how to do this
see [Contributor Setup Instructions For MapTool][contributor-setup-for-maptool]

## Asking Questions

If you have questions not answered in these guidelines, need further help getting started, or need
help working on an issue then you can

* [Ask on the RPTools Discord Sever](discord.gg/dZy7HeYYVY)
* [Submit a GitHib Issue as a question](https://github.com/RPTools/maptool/issues/new?assignees=&labels=question&template=submit-a-question.md&title=)

[bug-issue]:https://github.com/RPTools/maptool/labels/bug

[feature-issue]:https://github.com/RPTools/maptool/labels/feature

[good-first-issue]:https://github.com/RPTools/maptool/labels/good%20first%20issue

[up-for-grabs]:https://github.com/RPTools/maptool/labels/up%20for%20grabs

[contributor-setup-for-maptool]:https://github.com/RPTools/maptool/wiki/Contributor-Setup-Instructions-For-MapTool

[coding-style-guides]:https://github.com/RPTools/maptool/blob/develop/doc/Code_Style_and_Guidelines.md


Thank you for contributing to MapTool! Your efforts help make the project better for everyone.
