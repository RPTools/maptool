#!/bin/bash

# Grab the list of macro functions from stdin and build a wget(1)
# command line that will grab those pages from Craig's wiki.  Once we
# have all of the pages, we can strip out the boilerplate until we have
# just the content, and then it can be merged with the i18n.properties
# file used by the macro editor.  Woohoo!

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

sed -e "s!^!$BASE!" < "${1:-wiki-has.txt}" | my_wget
