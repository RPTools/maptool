#!/usr/bin/env python3

import os
import re
import sys

from lxml import etree
from lxml.etree import ElementTree

wiki_dump = 'wiki-dump.xml'
redirect_regex = re.compile(r'#REDIRECT\s*\[\[(.*?)\]\]')

if not os.path.exists(wiki_dump):
    print(f"File '{wiki_dump}' not found.  Unpack the MediaWiki dump to that name.")
    sys.exit(1)

root = ElementTree().parse(wiki_dump)
# print(repr(root))
tag = str(root.tag)

ens = tag[1:tag.find("}")]
ns = { "default": ens }    # for use in find(), et al
ens = "{" + ens + "}"      # per-element namespace

"""
<mediawiki
    xmlns="http://www.mediawiki.org/xml/export-0.11/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.mediawiki.org/xml/export-0.11/ http://www.mediawiki.org/xml/export-0.11.xsd"
    version="0.11"
    xml:lang="en-GB">
"""


def links_to(t: str) -> str:
    if "#REDIRECT" not in t:
        return ""
    match = redirect_regex.search(t)
    return match.group(1) if match else ""


def is_validTitle(t: str) -> bool:
    return t and " " not in t and ":" not in t and "/" not in t and "(" not in t and ")" not in t


# Cache some things that will otherwise take up quite a bit of time
page_with_ns = ens + "page"

# for page in root.iter(page_with_ns):
#     if page.tag == page_with_ns:
#         revs = page.findall("default:revision", ns)
#         print(f"{page.tag}: {etree.tostring(revs[-1])}")

# Create a list of all page titles that appear to be macro functions.
names = set()
redirects = {}
for (elem_num, page) in enumerate(root.iter(page_with_ns), 1):
    title = page.find("./default:title", ns).text
    # print("Title: {0}".format(title))

    # Title must exist and not contain:
    #    " " (no macro names contain spaces).
    #    ":" (these are "category" pages).
    #    "/" (these are translated pages; maybe some day...?).
    if not is_validTitle(title):
        continue

    # text = page.find("./default:revision/default:text", ns).text
    revs = page.findall("default:revision/default:text", ns)
    text = revs[-1].text
    # Title must exist.  Text must exist.
    if text:
        if "{{MacroFunction" in text:
            names.add(title)
        elif "#REDIRECT" in text:
            # We can't determine if a redirect is a function page without
            # also looking up the other element.  Here, we keep a list of all
            # redirects and where they resolve to, so that when we're done,
            # we can check the redirects and add them to the list.
            destination = links_to(text)
            if destination and is_validTitle(destination):
                redirects[title] = destination
            else:
                print(f"# https://wiki.rptools.info/index.php/{title} redirect out of scope")
        else:
            print(f"# https://wiki.rptools.info/index.php/{title} missing template")

# These are all the redirects.  If they point to a page we've decided to
# process, add them to `names`.
list_to_add = [key for (key, value) in redirects.items() if value in names]
names.update(list_to_add)
  
# The current `wiki-dump.xml` doesn't include all macro functions,
# as some new pages were added after Craig's dump.  Those pages
# are added here.  We also cull the list we created, above, by removing
# items that are not proper pages (they should've been weeded out by
# the search for the MacroFunction template??).
added = """\
"""
removed = """\
"""
names.update(added.split())
names = names.difference(removed.split())

all_names = list(names)
all_names.sort()
print("\n".join(all_names))
