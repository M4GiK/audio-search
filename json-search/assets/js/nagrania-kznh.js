var movies = [];

jQuery.getJSON("assets/library/lib.json", function(data, textStatus) {
	if (textStatus == "success") {
		jQuery.each(data, function(key, value) {
			if (key != "_comment") {
				value.webdirectory = value["web-directory"];
				delete value["web-directory"];			
				movies.push(value);			
			}
		});
	} else {
		alert("JSON non-success status: " + textStatus);
	}
});
