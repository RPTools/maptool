#!/bin/bash

# Things to extract from the HTML page:
#   $('div#mw-content-text')
# but there are pieces we don't want in the documentation:
#   $('div.template_stub')
#   $('div#toc')

XSL=extract-mw-content.xsl

mkdir -p processed
find lmwcs.com/rptools/wiki -type f |
    sort |
    while read fname
    do
        output=processed/${fname##*/}
        if [[ "$fname" -nt "$output" ]]; then
            echo "Processing $fname ..."
            rm -f "$output"
            xsltproc "$XSL" "$fname" | xmllint --format - > "$output"
        fi
    done
