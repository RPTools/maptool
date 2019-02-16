#!/usr/bin/env python3

from lxml.etree import ElementTree

root = ElementTree().parse('wiki-dump.xml')
print(repr(root))
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

names = []
for (elem_num, page) in enumerate(root, 1):
    if page.tag == page_with_ns:
        title = page.find("./default:title", ns).text
        # print("Title: {0}".format(title))

        text = page.find("./default:revision/default:text", ns).text
        # if not text:
        #     print("Missing revision/text field.  {0}:{1}".format(title, elem_num))
        if text and text.find("{{MacroFunction") != -1:
            names.append(title)
    else:
        print(page)

print("List of macro names")
print("\n".join(names))
