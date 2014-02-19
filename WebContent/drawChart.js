

/**
 * Draw the polyline check-in graph through the google chart
 * @param jsonStr, all the check-in records in json format.
 * @param d, how many days data contained in the jsonStr.
 * @param h, how many hours we will consider in each day.
 */
function drawChart(jsonStr,d,h) {
		var data = new google.visualization.DataTable();
		data.addColumn('string', 'date_hour');
		data.addColumn('number', 'number');
		data.addRows(d * h);

		var jsonObj = JSON.parse(jsonStr);

		for (var i = 0; i < d; i++)
			for (var j = 0; j < h; j++) {
				data.setValue(i * h + j, 0, i + "d:" + j + "h");
				data.setValue(i * h + j, 1, parseInt(jsonObj[i][j]));
			}

		var options = {
			title : 'Given Polygon\'s Check-in Number Temporal Change In a Week',
			titleTextStyle : {
				fontSize : 20,
				bold : true
			},
			curveType : 'function',
			hAxis : {
				title : 'time d(day):h(hour))',
				showTextEvery : 4,
				textPosition : "in",
				textStyle : {
					fontSize : 12
				}
			},
			vAxis : {
				title : 'check-in number'
			},

		};

		var chart = new google.visualization.LineChart(document.getElementById('chart_div'));
		chart.draw(data, options);
	}
