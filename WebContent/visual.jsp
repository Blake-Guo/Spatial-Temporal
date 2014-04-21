<html>
<head>
<title>Visual The Check-In</title>

<script src="https://www.google.com/jsapi"></script>
<script src="drawChart.js"></script>
<script>
	google.load("visualization", "1", {
		packages : [ "corechart" ]
	});
	
	
	xmlhttp = new XMLHttpRequest();

	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
			var jsonRes = xmlhttp.responseText;
			drawChart(jsonRes, 2);
		}
	};
	
	var url = "";
	
	var url = "getStatic.jsp?point1=";
	url += "<%=request.getParameter("point1")%>";
	url += "&point2=";
	url += "<%=request.getParameter("point2")%>";
	url += "&city=";
	url += "<%=request.getParameter("selectedCity")%>";
			
	xmlhttp.open("GET", url , true);
	xmlhttp.send();
</script>
</head>

<body>
	<div id="chart1_div"></div>
	<div id="chart2_div"></div>
	<div id="chart3_div"></div>
</body>
</html>