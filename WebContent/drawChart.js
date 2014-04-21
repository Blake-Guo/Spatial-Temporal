/**
 * Draw the  check-in temporal change graph by the google chart
 * @param {Object} jsonStr , the check-in records stored in json format
 * @param {Object} w , the number of weeks to show
 */
function drawChart(jsonStr, w) {

	var dataTable = new Array();
	for (var k = 0; k < w; k++) {
		dataTable[k] = new google.visualization.DataTable();
		dataTable[k].addColumn('string', 'date_hour');
		dataTable[k].addColumn('number', 'number');
		dataTable[k].addRows(7 * 24);
	}

	var jsonObj = JSON.parse(jsonStr);

	for (var k = 0; k < w; k++)
		for (var i = 0; i < 7; i++)//day, from monday to sunday
			for (var j = 0; j < 24; j++) {
				dataTable[k].setValue(i * 24 + j, 0, (i + 1) + "d:" + j + "h");

				var ki = k * 7 + i + 1;//the reason that adding 1 is because the isodow start from 1.

				if (jsonObj[ki][j] == null)//not pretty sure about this!!!!!!
				{
					//dataTable[k].setValue(i * 24 + j, 1, 0);
				} else {
					dataTable[k].setValue(i * 24 + j, 1, parseInt(jsonObj[ki][j]));
				}
			}

	var options = {
		title : 'Check-in Number Weekly Change',
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

	var chartArr = new Array();
	for (var i = 0; i < w; i++) {

		var divName = 'chart' + (i+1) + '_div';
		chartArr[i] = new google.visualization.LineChart(document.getElementById(divName));
		chartArr[i].draw(dataTable[i], options);

	}

	//var chart1 = new google.visualization.LineChart(document.getElementById('chart1_div'));
	//chart1.draw(data, options);

	//Output the json, just for test.
	//document.getElementById('json_div').innerHTML = jsonStr;
}
