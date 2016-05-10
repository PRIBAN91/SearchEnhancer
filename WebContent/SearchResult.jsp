<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Your search results</title>
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js"></script>
<script type="text/javascript">
	$(document).ready(function() {

		$.ajax({

			url : "SearchEntered",
			data : {
				searchString : str
			},
			dataType : 'json',

			error : function() {
				alert("Error Occured");
			},
			success : function(data) {
				//	alert(data);
				var receivedData = [];

				$.each(data.SearchedList, function(index) {

					var p = document.createElement("p");
					p.innerHTML = data.SearchedList[index];
					news.appendChild(p);

					//  $('body').append(data.SuggestionList[index]);
				});
			}
		});

		return false;

	});
</script>
</head>
<body>
	<div class="results" align="center"></div>
</body>
</html>