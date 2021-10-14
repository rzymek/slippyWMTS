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
&format=svg\
&layers=show:76,77\
&bbox=$left,$bottom,$right,$top\
&bboxSR=$sr\
&imageSR=$sr\
&size=$size\
&f=image" -o map.svg


https://mapserver.bdl.lasy.gov.pl/ArcGIS/rest/services/Mapa_turystyczna/MapServer/export?dpi=96
  &transparent=true
  &format=png8
  &bbox=1532421.4519384366,6195576.5291813975,2755413.9045009264,7418568.981743888
  &bboxSR=102100
  &imageSR=102100
  &size=1000,1000
  &layers=show%3A76,77,81,82,83,84,90,91,92,94,95,96,97
  &f=image
https://mapserver.bdl.lasy.gov.pl/ArcGIS/rest/services/Mapa_turystyczna/MapServer/export?dpi=96
  &transparent=true
  &format=png8
  &bbox=1571557.2104204353,6246942.2121890215,2690595.3045151136,7365980.3062837
  &bboxSR=102100
  &imageSR=102100
  &size=915,915
  &layers=show%3A76,77,81,82,83,84,90,91,92,94,95,96,97
  &f=image
