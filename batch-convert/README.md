
	mkdosfs /dev/sdb1 -s 4 -F 32 -n TOPO
	rsync -rW ./osm-topo/ /media/sd/osm-topo --info=progress2	
