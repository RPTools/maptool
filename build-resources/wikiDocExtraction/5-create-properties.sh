#!/bin/bash

# Create the i18n.properties file by read the files under 'processed/'

#abort.description = Conditionally abort the execution of a macro
#abort.summary     = <h2><span>abort() Function</span></h2>\
#<div>\u2022 <b>Introduced in version 1.3b42</b></div>\
PROC=${1:-processed.txt}

while read file desc
do
    # First, write the description of the function
    # If empty, use "TBD"
    echo "$file.description = ${desc:-TBD}"

    # Second, read the contents of the file under 'processed/' and
    # generate the content for the help doc
    echo "$file.summary     = \\"

    # xmllint(1) will output an XML processing instruction as line one
    # and we want to remove that, then add a backslash to every line.
    xmllint --format "processed/$file.html" |
        sed -e '1d' -e 's/$/\\/'

    # This adds a blank line to indicate the previous string has
    # ended (necessary because the last line contains a backslash).
    echo

    # And last, add a blank line between entries
    echo
done < "$PROC"
