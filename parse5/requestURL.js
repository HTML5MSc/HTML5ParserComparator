	var request = require("request");
	request({
	  uri: process.argv[2],
	}, function(error, response, body) {
		console.log(body);
	});