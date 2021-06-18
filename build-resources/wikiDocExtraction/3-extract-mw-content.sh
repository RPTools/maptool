#!/bin/bash

# Things to extract from the HTML page:
#   $('div#mw-content-text')
# but there are pieces we don't want in the documentation:
#   $('div.template_stub')
#   $('div#toc')

XSL=extract-mw-content.xsl

mkdir -p processed
find wiki.rptools.info/index.php -type f |
    sort |
    while read fname
    do
        output=processed/${fname##*/}
	# Only process the files that are newer.  This is just a simple
	# optimization so that if only a couple pages were updated, we
	# don't need to process hundreds of them.
        if [[ "$fname" -nt "$output" ]]; then
            echo "Processing $fname ..."
            rm -f "$output"
            xsltproc "$XSL" "$fname" | xmllint --format - > "$output"
        fi
    done
