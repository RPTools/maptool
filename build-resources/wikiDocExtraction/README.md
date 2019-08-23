These scripts and auxiliary files are used to extract the documentation
from Craig's `lmwcs.com` web site and store them here under the
`processed/` directory.

The MediaWiki software adds some boilerplate that is removed in these
files -- we only keep the contents of `<div id="mw-content-text">` but
even that has pieces removed, like the table of contents and all
HTML comments.

The end result is a Java properties file that we can manually copy to
`../../src/main/resources/net/rptools/maptool/language/macro_descriptions/`
under the name `i18n.properties`.

The process is as follows:

1.  `1-wiki-getfnnames.py > wiki-has.txt`

    This Python3 script reads Craig's MediaWiki dump file and makes a
    list of all pages that appear to describe macro functions.  This
    list is stored in `wiki-has.txt` and is used in the next step.

1.  `2-get-wiki-pages.sh`

    This script reads the `wiki-has.txt` file to determine which pages
    to retrieve.  It grabs them using `wget` and puts them under the
    `lmwcs.com` directory.  The actual pages are under `rptools/wiki/`
    and auxiliary files are under `maptool/` (like JavaScripts, CSS, and
    images).

1.  `3-extract-mw-content.sh`

    This script extracts the proper `<div>` from the pages under
    `lmwcs.com/rptools/wiki/`, checking to ensure the HTML is valid,
    and then reformatting and re-indenting the document when
    generating the output.  That output is put under `processed/` for
    use in the next step.

1.  `4-generate-macro-list.sh`

    This script reads all of the files under `processed/` and creates an
    "index", per se, that lists each filename and the description of the
    macro that the file documents.  It looks for `<div
    class="template_description">` to isolate the description text.
    This becomes input for the script in the next step.

1.  `5-create-properties.sh`

    This script reads the fully processed HTML snippets stored under the
    `processed/` directory and creates the `i18n.properties` file.  This
    part isn't perfect, as it doesn't always detect the `.description`
    field properly.  Future updates to the wiki pages may correct that.
    This script should be executed and the output redirected to
    `i18n.properties` to create that file.
