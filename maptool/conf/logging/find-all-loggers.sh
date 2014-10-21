#!/bin/bash

# Give this script a top-level source directory and it will find all
# logging classes and generate a list.

DEFAULT=~/Workspace/maptool/src
DIR=${1:-$DEFAULT}

# If it looks like we were given the project directory instead of the
# source directory, go ahead and descend into the source...
if [[ -d "$DIR/src" ]]; then
    DIR="$DIR/src"
fi

find "$DIR" -name '*.java' -print |
    sort |
    xargs grep -lH 'Logger.getLogger' |
    sed -e "s:^$DIR/::" -e 's:\.java$::' -e 's:/:.:g'
