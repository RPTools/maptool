#!/usr/bin/env python3

from lxml.etree import ElementTree

root = ElementTree().parse('wiki-dump.xml')
#print(repr(root))
tag = str(root.tag)

ens = "{" + tag[1:tag.find("}")] + "}"      # per-element namespace
ns = { "default": tag[1:tag.find("}")] }    # for use in find(), et al

"""
<mediawiki
    xmlns="http://www.mediawiki.org/xml/export-0.10/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.mediawiki.org/xml/export-0.10/ http://www.mediawiki.org/xml/export-0.10.xsd"
    version="0.10"
    xml:lang="en">
"""

# Cache some things that will otherwise take up quite a bit of time
page_with_ns = ens + "page"

names = {}
for (elem_num, page) in enumerate(root, 1):
    if page.tag == page_with_ns:
        title = page.find("./default:title", ns).text
        # print("Title: {0}".format(title))

        text = page.find("./default:revision/default:text", ns).text
        # Text must exist.
        # Text must contain '{{MacroFunction'.
        if title and text and text.find("{{MacroFunction") != -1:
            # Title must not contain " " (no macro names contain spaces).
            # Title must not contain ":" (these are "category" pages).
            # Title must not contain "/" (these are translated pages).
            if title.find(" ") == -1 and \
               title.find(":") == -1 and \
               title.find("/") == -1:
                names[title] = 1

# The current `wiki-dump.xml` doesn't include all macro functions,
# as some new pages were added after Craig's dump.  Those pages
# are added here.  We dump the ones we found, above

added = """\
copyTable
execMacro
exportData
exposeAllOwnedArea
exposeFogAtWaypoints
getEnvironmentVariable
getLibPropertyNames
getMaxRecursionDepth
getTableImage
getTokenRotation
getViewArea
isExternalMacroAccessAllowed
setMaxRecursionDepth
setTableImage
"""

names.update(dict.fromkeys(added.split(), 2))
all_names = list(names.keys())
all_names.sort()
print("\n".join(all_names))
