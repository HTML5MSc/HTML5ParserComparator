	var request = require("request");
	var fs = require('fs');
	var iconv = require('iconv-lite');
	
	request({
	  uri: process.argv[2],
	  encoding: null,
	}, function(error, response, body) {
		var bodyWithCorrectEncoding = iconv.decode(body, 'utf8');
		fs.writeFile('response', bodyWithCorrectEncoding, function(err){
			if (err) throw err;
		});
	});