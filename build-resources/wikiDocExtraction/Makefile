# Just boilerplate so far -- not ready for prime time.

all: make-properties-file

make-page-list: wiki-has.txt
	./1-wiki-getfnnames.py > _tmp && mv _tmp wiki-has.txt

get-pages: make-page-list wiki.rptools.info/
	./2-get-wiki-pages.sh

extract-content: get-pages processed/
	./3-extract-mw-content.sh

make-summary-file: extract-content processed.txt
	./4-generate-macro-list.pl > _tmp && mv _tmp processed.txt

make-properties-file: make-summary-file i18n.properties
	./5-create-properties.sh > _tmp && mv _tmp i18n.properties
