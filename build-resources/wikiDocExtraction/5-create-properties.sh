#!/bin/bash

# Create the i18n.properties file by read the files under 'processed/'

#abort.description = Conditionally abort the execution of a macro
#abort.summary     = <h2><span>abort() Function</span></h2>\
#<div>\u2022 <b>Introduced in version 1.3b42</b></div>\

cd processed || {
    echo >&2 "Can't change directory to 'processed'?!";
    exit 1
}

# Search each HTML file looking for 'template_version'.  We want to
# extract the following line (the description) and print the filename, a
# TAB, and the description, to stdout.  That becomes the list of things
# to process in the 'while' loop.
awk '/template_version/ { getline; print FILENAME "\t" $0 }' * |
    while read file desc
    do
        # First, strip the '.html' filename extensions
        file=${file%.html}

        # Next, write the description of the function
        # If empty, use "TBD"
        echo "$file.description = ${desc:-TBD}"

        # Next, read the contents of the file under 'processed/'
        # and generate the content for the help doc
        echo "$file.summary     = \\"
        tail -n +2 "processed/$file.html" |
            xmllint --format - |
            sed -e 's/$/\\/'

        # Next, add a blank line to indicate the previous string has
        # ended (necessary because the last line contains a backslash).
        echo

        # And last, add a blank line between entries
        echo
    done
