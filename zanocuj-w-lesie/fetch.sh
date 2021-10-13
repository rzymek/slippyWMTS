#!/bin/bash
set -eu
cd $(dirname "$0")

left=1559008.2685272547
top=7366360.14525283
right=2708621.173936
bottom=6238761.103990209

wgs84WebMercator=102100 # EPGS:3857

sr=$wgs84WebMercator
size=4700,4610

curl --compressed  -H 'Referer: https://www.bdl.lasy.gov.pl/' \
"https://mapserver.bdl.lasy.gov.pl/ArcGIS/rest/services/Mapa_turystyczna/MapServer/export\
?dpi=96\
&transparent=true\
&format=png8\
&layers=show:76,77\
&bbox=$left,$bottom,$right,$top\
&bboxSR=$sr\
&imageSR=$sr\
&size=$size\
&f=image" -o map.png
