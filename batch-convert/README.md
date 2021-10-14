
	mkdosfs /dev/sdb1 -s 4 -F 32 -n TOPO
	rsync -rW ./osm-topo/ /media/sd/osm-topo --info=progress2	


On spot instance:

    sudo amazon-linux-extras install docker
    sudo service docker start
    sudo usermod -a -G docker ec2-user
    mkdir output
    sudo mount /dev/xvdba output
    docker run -d -v $PWD/output:/app/output rzymek/topo-mbtiles
