Raphael("map", 1000, 400, function () {
  var r = this;
  r.rect(0, 0, 1000, 400, 10).attr({
    stroke: "none",
    fill: "#09baa2"
  });
  r.setStart();
  for (var country in worldmap.shapes) {
    r.path(worldmap.shapes[country]).attr({stroke: "#ccc6ae", fill: "#f0efeb", "stroke-opacity": 0.25});
  }
  var world = r.setFinish();
  world.getXY = function (lat, lon) {
     return {
       cx: lon * 2.6938 + 465.4,
       cy: lat * -2.6938 + 227.066
     };
  };
  world.getLatLon = function (x, y) {
    return {
      lat: (y - 227.066) / -2.6938,
      lon: (x - 465.4) / 2.6938
    };
  };
  var latlonrg = /(\d+(?:\.\d+)?)[\xb0\s]?\s*(?:(\d+(?:\.\d+)?)['\u2019\u2032\s])?\s*(?:(\d+(?:\.\d+)?)["\u201d\u2033\s])?\s*([SNEW])?/i;
  world.parseLatLon = function (latlon) {
    var l = latlon.split(",");
    return this.getXY(l[0], l[1]);
  };
  var dot = r.circle();

  function go(attr,color,offset){
    if(attr.cx && attr.cy){

       dot.paper.add([{
           type: "circle",
           cx: attr.cx + offset[0],
           cy: attr.cy + offset[1],
           r: 4
       }]).attr({"stroke": "#000", "stroke-width": "0.5px","fill":color});

    }
  }

  for (var i=0;i<points.length;i++){ 
    var txt = points[i][0];
    var attr = world.parseLatLon(txt);
    go(attr,points[i][1],points[i][2]);
  }
});
