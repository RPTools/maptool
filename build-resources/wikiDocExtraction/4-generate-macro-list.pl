#!/usr/bin/perl -w

# Some of the characters we're going to read are apparently multibyte
# (i.e., wide) characters.  And when our `print` statements tries to
# concatenate two strings together we'll get a warning that wide
# characters are being used in conjunction with non-wide characters.
# By opening all files using the `locale` module, files will be
# interpreted using environment variables, thus becoming UTF-8 or
# whatever locale is the default.  (Note that the Windows OS doesn't
# define a default locale, so explicit variables must be created.  If
# this is done, Perl will pick those up and use them.)
# See 'What is a "wide character"?' in `perldoc perlunifaq`.
use open ':locale';
use HTML::TreeBuilder 5 -weak;

# Reads all of the HTML files under 'processed/' and makes a list that
# contains the filename (without directory prefix) and the description.

chdir "processed"
    or die "Can't change directory to 'processed'?!\n$!";

# Search each HTML file looking for 'template_description'.  We want to
# extract the first sentence within that `div`.  If the div doesn't
# exist, search for 'template_version' instead.  For this one, we want to
# extract the following line.
# Either way, we print the filename, a TAB, and the description, to stdout.

foreach my $fn (glob("*.html")) {
    my $result = &processFile($fn);
    $fn =~ s/[.]html$//;
    print "$fn\t$result\n";
}
exit(0);

sub processFile {
    my ($fn) = @_;
    my $t;
    my $tree = HTML::TreeBuilder->new;
    $tree->implicit_tags(0); # "<html>" is still added...?!
    $tree->no_expand_entities(1); # it'll be valid HTML when written
    $tree->parse_file($fn);

    $e = $tree->look_down(
        ('_tag', 'div'),
        ('class', 'template_description')
    );
    if (not($e)) {
        # If we don't find a description, use the old technique.
        # /template_version/ { getline; print FILENAME "\t" $0 }' *
        $e = $tree->look_down(
            ('_tag', 'div'),
            ('class', 'template_version')
        );
        $e = $e->right();
    }
    # Could look for the first <h2> and take the following <p>...
    die "Can't find either description or version?!" unless $e;

    my @t = ();
    foreach my $elem ($e->content_list()) {
        push(@t, ref($elem) ? $elem->as_HTML() : $elem);
    }
    $t = join(" ", @t);
    # This looks for the end of the first sentence and deletes
    # everything after that point.  This doesn't work, though.
    # What if there's a "." inside an HTML tag, such as:
    #   <img src="xxx" alt="Look. Here... Really!">
    # But doing this correctly means parsing out HTML elements, looking
    # for the end-of-sentence, then adding HTML elements back in.
    $t =~ s/[.]\s+[[:upper:]<].*$/./;
    $t =~ s/\s+/ /g;
    return $t;
}
