#!/usr/bin/env python3

import os
import sys

from lxml import etree
from lxml.etree import ElementTree

wiki_dump = 'wiki-dump.xml'

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
    xmlns="http://www.mediawiki.org/xml/export-0.10/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.mediawiki.org/xml/export-0.10/ http://www.mediawiki.org/xml/export-0.10.xsd"
    version="0.10"
    xml:lang="en">
"""
"""
<mediawiki
    xmlns="http://www.mediawiki.org/xml/export-0.11/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.mediawiki.org/xml/export-0.11/ http://www.mediawiki.org/xml/export-0.11.xsd"
    version="0.11"
    xml:lang="en-GB">
"""

# Cache some things that will otherwise take up quite a bit of time
page_with_ns = ens + "page"

# for page in root.iter(page_with_ns):
#     if page.tag == page_with_ns:
#         revs = page.findall("default:revision", ns)
#         print(f"{page.tag}: {etree.tostring(revs[-1])}")

# Create a list of all page titles that appear to be macro functions.
names = set()
for (elem_num, page) in enumerate(root.iter(page_with_ns), 1):
    title = page.find("./default:title", ns).text
    # print("Title: {0}".format(title))

    # Title must exist and not contain:
    #    " " (no macro names contain spaces).
    #    ":" (these are "category" pages).
    #    "/" (these are translated pages; maybe some day...?).
    if not title or " " in title or ":" in title or "/" in title:
        continue

    # text = page.find("./default:revision/default:text", ns).text
    revs = page.findall("default:revision/default:text", ns)
    text = revs[-1].text
    # Title must exist.  Text must exist.
    if text:
        if "{{MacroFunction" in text:
            # print(f"https://wiki.rptools.info/{title}: full compliance")
            names.add(title)
        elif "#REDIRECT" in text:
            # The redirect will happen when we fetch the page at runtime.
            names.add(title)
        else:
            print(f"https://wiki.rptools.info/index.php/{title} missing template")

# The current `wiki-dump.xml` doesn't include all macro functions,
# as some new pages were added after Craig's dump.  Those pages
# are added here.  We dump the ones we found, above
added = """\
"""
removed = """\
!!unknown-macro!!
AI
Aura
Bars
Editor
Frameworks
Glossary
Halo
MBL
MTBasics
MapTool
Notepad++
Number
Phergus
Preferences
Size
Stamp
State
String
Tables
Token
TokenTool
Uninstalling
VBL
Wiki
aura
d
f
getOwnerOnlyVisible()
h
u
"""
names.update(added.split())
names = names.difference(removed.split())

all_names = list(names)
all_names.sort()
print("\n".join(all_names))
