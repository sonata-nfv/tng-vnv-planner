#!/bin/sh

echo "null = None" > documents_catalogue.py
echo "tests=`cat static/tests.json`" >>  documents_catalogue.py
mv VNVGTK.txt mock-vnv-gtk/app/
mv documents_catalogue.py mock-vnv-gtk/app/

echo "null = None" > documents_catalogue.py
echo "services=`cat static/services.json`" >>  documents_catalogue.py
echo " " >> documents_catalogue.py
echo "packages=`cat static/packages.json`" >>  documents_catalogue.py
mv GTK.txt mock-gtk/app/
mv documents_catalogue.py mock-gtk/app/

echo "null = None" > documents_catalogue.py
mv documents_catalogue.py mock-vnv-curator/app/
