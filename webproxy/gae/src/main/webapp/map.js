var puwg92 = "EPSG:2180";
proj4
        .defs(
                puwg92,
                "+proj=tmerc +lat_0=0 +lon_0=19 +k=0.9993 +x_0=500000 +y_0=-5300000 +ellps=GRS80 +units=m +no_defs");

var selectionLayer = new ol.source.Vector();
var sources = {
    'OpenStreetMap' : new ol.source.OSM(),
    'OpenCycleMap' : new ol.source.XYZ({
        url : 'http://tile.opencyclemap.org/cycle/{z}/{x}/{y}.png'
    }),
    'local' : new ol.source.OSM({
        url : 'http://localhost/slippy/{z}/{x}/{y}.png'
    }),
    'GeoPortal TOPO' : new ol.source.OSM({
        url : '/tile/topo/{z}/{x}/{y}.png'
    }),
    'GeoPortal ORTO' : new ol.source.OSM({
        url : '/tile/orto/{z}/{x}/{y}.png'
    }),
    'UMP' : new ol.source.XYZ({
        url : 'http://tiles.ump.waw.pl/ump_tiles/{z}/{x}/{y}.png'
    })
};

[ 'Road', 'Aerial', 'AerialWithLabels' ]
        .forEach(function (style) {
            sources['Bing: ' + style] = new ol.source.BingMaps(
                    {
                        key : 'Apszeg5_v01g6cZjl_9VTYEcC_qchUYPEfVR64qdbgV5aRKfbYTbMeitv3bLEPkq',
                        imagerySet : style
                    });
        });

[ 'osm', 'sat' ].forEach(function (style) {
    sources['MapQuest: ' + style] = new ol.source.MapQuest({
        layer : style
    });
});

var layers = [];
Object.keys(sources).forEach(function (name) {
    layers.push(new ol.layer.Tile({
        visible : false,
        preload : Infinity,
        source : sources[name]
    }));
});

layers.push(new ol.layer.Vector({
    source : selectionLayer,
    style : new ol.style.Style({
        fill : new ol.style.Fill({
            color : 'rgba(255, 0, 0, 0.2)'
        }),
        stroke : new ol.style.Stroke({
            color : 'red',
            width : 2
        })
    })  
}));
//layers.push(new ol.layer.Tile({
//    source : new ol.source.TileDebug({
//        tileGrid : new ol.tilegrid.XYZ({
//            maxZoom : 22
//        })
//    })
//}));
 
function formatMGRS (c) {
    return exports.forward(c, 5).replace(
            /([0-9]*)([A-Z]*)([0-9]{5})([0-9]{5})/, "$1 $2 $3 $4");
}

var map = new ol.Map({
    target : 'map',
    controls : ol.control.defaults().extend([ new ol.control.MousePosition({
        coordinateFormat : function (coordinate) {
            return formatMGRS(coordinate);
        },
        className : '',
        projection : 'EPSG:4326',
        target : document.getElementById('mgrs')
    }), new ol.control.MousePosition({
        coordinateFormat : function (coordinate) {
            return ol.coordinate.toStringXY(coordinate, 4);
        },
        className : '',
        projection : 'EPSG:4326',
        target : document.getElementById('latlon')
    }), new ol.control.MousePosition({
        coordinateFormat : function (coordinate) {
            return ol.coordinate.toStringXY(coordinate, 0); // 0 decimal places
        },
        className : '',
        projection : puwg92,
        target : document.getElementById('puwg')
    }), new ol.control.MousePosition({
        coordinateFormat : function (coordinate) {
            return ol.coordinate.toStringXY(coordinate, 0);
        },
        className : '',
        target : document.getElementById('mercator')
    }), new ol.control.ScaleLine(), new ol.control.ZoomSlider() ]),
    layers : layers,
    view : new ol.View({
        center : ol.proj.transform([ 21.03, 52.22 ], 'EPSG:4326', 'EPSG:3857'),
        zoom : 10
    })
});

var dragBox = new ol.interaction.DragBox({
    condition : ol.events.condition.shiftKeyOnly,
    style : new ol.style.Style({
        stroke : new ol.style.Stroke({
            color : [ 0, 0, 255, 1 ]
        })
    })
});
map.addInteraction(dragBox);
var selectedBox = null;

function updateSelectionInfo () {
    var extent = dragBox.getGeometry().getExtent();
    var x1 = extent[0];
    var y1 = extent[1];
    var x2 = extent[2];
    var y2 = extent[3];
    var box = getFetchParams();
    box.width = box.x2 - box.x1 + 1;
    box.height = box.y2 - box.y1 + 1;
    document.getElementById('selectionInfo').innerHTML = 'Zaznacznie: '
            + ((x2 - x1) / 1000).toFixed(1) + ' x '
            + ((y2 - y1) / 1000).toFixed(1) + ' km<br>' + box.width + " x "
            + box.height + " = " + (box.width * box.height) + ' kafli<br>'
            + (box.width * 512) + ' x ' + (box.height * 512) + ' px'
    document.getElementById('fetcher').style.display = 'block';
}

dragBox.on('boxend', function (e) {
    var extent = dragBox.getGeometry().getExtent();
    var x1 = extent[0];
    var y1 = extent[1];
    var x2 = extent[2];
    var y2 = extent[3];
    selectionLayer.clear();
    selectedBox = new ol.Feature(new ol.geom.Polygon([ [ [ x1, y1 ],
            [ x1, y2 ], [ x2, y2 ], [ x2, y1 ] ] ]));
    selectionLayer.addFeature(selectedBox);
    updateSelectionInfo();
});

function getFetchParams () {
    var bbox = selectedBox.getGeometry().getExtent();
    var puwg = ol.proj.transform(bbox, 'EPSG:3857', 'EPSG:2180');
    var mgrsTopLeft = exports.forward(ol.proj.transform([ bbox[0], bbox[1] ],
            'EPSG:3857', 'EPSG:4326'), 1);
    var params = {
        z : document.getElementById('getZ').value,
        source : 'topo',
        box : {
            x1 : puwg[0],
            y1 : puwg[1],
            x2 : puwg[2],
            y2 : puwg[3]
        }
    };
    var box = getTileBox(params);
    box.z = params.z;
    return box;
}

function fetch (path) {
    var box = getFetchParams();
    window.open('/' + path + '?z=' + box.z + '&x1=' + box.x1 + '&y1=' + box.y1
            + '&x2=' + box.x2 + '&y2=' + box.y2);
}

map.on('click', function (evt) {
    var feature = map.forEachFeatureAtPixel(evt.pixel,
            function (feature, layer) {
                return feature;
            });
    if (!feature) {
        return;
    }
    fetch('kml');
});

var layerSelection = document.getElementById('layerSelection');
Object.keys(sources).forEach(function (name) {
    var option = document.createElement('option');
    option.text = name;
    layerSelection.add(option);
});
function selectLayer (combo) {
    var idx = combo.selectedIndex;
    var selectableSources = Object.keys(sources).length;
    for (var i = 0; i < selectableSources; i++) {
        layers[i].setVisible(i === idx);
    }
}
selectLayer({
    selectedIndex : 0
});
