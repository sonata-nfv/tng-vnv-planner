#!/bin/sh

echo `cat ./app/static/tests.json`> VNVGTK.txt
echo "null = None" > ./app/documents_catalogue.py
echo "tests=`cat ./app/static/tests.json`" >>  ./app/documents_catalogue.py

