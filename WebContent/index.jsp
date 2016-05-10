<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Race Against Time</title>
<link rel="stylesheet" type="text/css" href="mystyle.css">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js"></script>
<script type="text/javascript">
	$(document).ready(function() {

		$('form').keypress(function(e) {
			if (e.which == 13) {
				//alert("You have pressed enter!");
				var str = $("#searchString").val();
				$(".entered").empty();
				$(".results").empty();
				var news = document.getElementsByClassName("entered")[0];
				if (str.length > 1) {
					//alert(existingString);

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
								// alert(index);
								//  $.each(data.jsonArray[index], function(key, value) {
								//alert(data.SuggestionList[index]);

								//}); 

								var p = document.createElement("p");
								p.innerHTML = data.SearchedList[index];
								news.appendChild(p);

								//  $('body').append(data.SuggestionList[index]);
							});
						}
					});

					/*  $.get('SearchCall', {
						existingString : existingString
					},function(data) {
						  alert(data.SuggestionList);
					});	 */
				}
				// e.preventDefault(); 
				return false;
			}
		});

		$("#searchString").keyup(function(event) {
			if (event.keyCode == 13) {
				//alert("You have pressed enter!");
				return false;
			} else {
				var str = $("#searchString").val();
				$(".results").empty();
				$(".entered").empty();
				var news = document.getElementsByClassName("results")[0];
				if (str.length > 1) {
					//alert(existingString);

					$.ajax({

						url : "SearchCall",
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

							$.each(data.SuggestionList, function(index) {
								// alert(index);
								//  $.each(data.jsonArray[index], function(key, value) {
								// alert(data.SuggestionList[index]);

								//}); 

								var p = document.createElement("p");
								p.innerHTML = data.SuggestionList[index];
								news.appendChild(p);

								//  $('body').append(data.SuggestionList[index]);
							});
						}
					});

					/* $.get('SearchCall', {
						existingString : existingString
					},function(data) {
						  alert(data.SuggestionList);
					});	 */
				}
			}
		});

	});
</script>


</head>
<body>

	<div class="main">
		<form class="search" method="post" action="#">
			<input type="text" id="searchString" name="q" placeholder="Search..." />
		</form>
	</div>

	<div class="results" align="center"></div>

	<div class="entered" align="center"></div>

</body>
</html>