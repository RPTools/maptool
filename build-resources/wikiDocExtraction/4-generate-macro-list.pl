#!/usr/bin/perl -w

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
    $t = join("", @t);
    $t =~ s/[.]\s\s*[A-Z].*$/./;
    return $t;
}
