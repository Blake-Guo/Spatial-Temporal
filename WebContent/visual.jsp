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
			drawChart(jsonRes, 7, 24);
		}
	};
	xmlhttp.open("GET", "getStatic.jsp?point1="+"<%=request.getParameter("point1")%>" + "&" + "point2=" + "<%=request.getParameter("point2")%>", true);
	xmlhttp.send();
</script>
</head>

<body>
	<div id="chart_div"></div>
	<div id="json_div"></div>
</body>
</html>