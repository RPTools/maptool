#!/bin/bash

# Grab the list of macro functions from `wiki-has.txt` and build a wget(1)
# command line that will grab those pages from Craig's wiki.  Once we
# have all of the pages, we can strip out the boilerplate until we have
# just the content, and then it can be merged with the i18n.properties
# file used by the macro editor.  Woohoo!

# The file can contain comment lines that start with '#' and they will
# be stripped out.

# Whatever the function name is, it gets tacked onto this URL.
# Note that the MediaWiki Craig is using has case-sensitive URLs.
BASE="http://lmwcs.com/rptools/wiki/"

function my_wget {
    wget --input-file=- \
        --wait=1 \
        --force-directories \
        --adjust-extension \
        --convert-links \
        --page-requisites \
        "$@"
}

sed -e '/^#/d' -e "s!^!$BASE!" < "${1:-wiki-has.txt}" | my_wget
