# The following line not used so that this script works properly on
# non-Bash systems.
#!/bin/bash

# This script was last modified for 1.3.b90.
# This script is provided under the Apache License v2.0 or later.

# All configurable options go here (no spaces inside the quotes)
MAXMEMSZ="768m"	# The 'm' suffix means megabytes.
MINMEMSZ="32m"	# If your Java crashes, try making this the same value.
STACKSZ="2m"	# Larger and more complicated macros require larger stack size.

# Any other options you might want to pass on the command line.  Various
# examples are shown here.  You can put multiple options in this
# variable if you separate them with spaces.
#APPOTHER="-Djava.security.debug=access"
#APPOTHER="-DMAPTOOL_DATADIR=~/.maptool"
#APPOTHER="-Dfile.encoding=UTF-8"
#APPOTHER="-DMAPTOOL_DATADIR=~/.maptool -Dfile.encoding=UTF-8"

# If you have installed SoyLatte (open source Java for OSX) and you
# want to try it, create a shell script that sets the proper PATH
# and JAVA_HOME variables and put it in ~/bin/java_home.  Then uncomment
# the following line.  This is NOT a statement that it will work.
# In particular, I've had weird problems with the way SoyLatte selects
# a network interface and MapTool currently has no way to force a
# particular interface so if it doesn't work, don't use it. :)
#. ~/bin/java_home

#########################################
# DON'T TOUCH ANYTHING BELOW THIS LINE! #
#########################################

# There are some constructs in here that you might consider weird.
# They're likely because OSX 10.4 uses Bash 2.05 while 10.5 and most
# Linux systems use Bash 3.x -- I'm trying to be portable between
# both versions of the shell.

if [ "X$RANDOM" = "X$RANDOM" ]; then
    echo 1>&2 "Error: this script requires a Korn or Bash shell."
    exit 1
fi

# From here down we use features specific to Korn/Bash shells, such as
# the ${var##pattern} modifier, double-brackets for IF statements, and
# the "let" command.

# Check first to see if the user specified a JAVA_HOME.  Then fallback
# to whatever is in the user's PATH.  If anything... :-/
java=$JAVA_HOME/bin/java
if [[ ! -x "$java" ]]; then
    java=$(type -p java)
    if [[ ! -x "$java" ]]; then
	echo 1>&2 "${0##*/}: Error: Can't find Java executable."
	echo 1>&2 "${0##*/}: Set JAVA_HOME such as \$JAVA_HOME/bin/java works."
	exit 2
    fi
fi

dir=$(dirname "$0")
cd "$dir" || {
    echo 1>&2 "${0##*/}: Error: Can't find script directory."
    exit 3
}

VERS=maptool*.jar
APPDOCKNAME=""		# These only need values on OSX
APPDOCKICON=""

case "$JAVA_HOME" in
    *[Ss]oy[Ll]atte*)	# No additional options if using SoyLatte...
	;;
    *)	case $(uname -s) in
	Darwin)
	    # These variables are only for OSX
	    APPDOCKNAME="-Xdock:name=MapTool"
	    # I'm using the URL because I don't want to package yet
	    # another file inside the download archive.  Sigh.
	    APPDOCKICON="-Xdock:icon=http://www.rptools.net/images/logo/RPTools_Map_Logo.png"
	    ;;
	esac
	;;
esac

ALL_OPTS="-Xmx$MAXMEMSZ -Xms$MINMEMSZ -Xss$STACKSZ $APPDOCKNAME $APPDOCKICON"

# Figure out how many JARs are in the current directory that match $VERS.
#
# If there's more than one,
#	give the user a menu and let them choose one.
# Otherwise,
#	skip the prompt and just run the program.
#
count=0
for jar in $VERS
do
    let count=count+1
    MAPTOOL="$jar"
done

# If there was only one, MAPTOOL is the expanded name.
# If there were more than one, we'll let the user decide which to use.
if ((count > 1)); then
    rvid=$(tput smso)	# Reverse video
    normal=$(tput sgr0)	# Turn off all attributes
    MAPTOOL=""
    PS3="
Type the number of your choice and press <Enter>.
Or use Ctrl-C to terminate: "
    IFS=""	# Turn off word breaks on whitespace
    echo 1>&2 ""	# Blank line
    select jar in $VERS
    do
	if [[ "$jar" == "" ]]; then jar="$REPLY"; fi
	# The user's selection doesn't match any of the choices!
	# Maybe they entered a pathname?
	if [[ -e "$jar" ]]; then
	    # Yep, they gave us a filename.  We'll use it.
	    MAPTOOL="$jar"
	    break
	fi
	echo 1>&2 ""	# Blank line
	echo 1>&2 "${rvid}Error: Invalid input -- try again.${normal}"
	echo 1>&2 ""
	REPLY=""	# Force menu to appear again.
    done
    if [[ "$MAPTOOL" == "" ]]; then exit 4; fi	# Ctrl-D at the select prompt
    echo 1>&2 "" # Blank line
fi

echo 1>&2 "Executing $MAPTOOL ..."
eval $java $ALL_OPTS $APPOTHER -jar "$MAPTOOL" run
