
var Parser = require('parse5').Parser;
var fs = require('fs');

var input;

switch(process.argv[2]){
	case '-s':
		parseInput(process.argv[3]);
		break;
	case '-u':
		parseURL(process.argv[3]);
		break;
	case '-f': 
		//console.log('file');	
		if(typeof(process.argv[3]) == 'undefined' || process.argv[3] === null){
			throw new Error('Empty file path argument');
		}
		fs.readFile(process.argv[3], 'utf8', function (err,data){
			
			if (err) throw err;
			parseInput(data.replace(/\n$/, ''));
		});
		break;

	default:
		throw new Error('Invalid input argument');
		break;
}

function parseURL(input){
	var parse5 = require('parse5');
	var parser = new parse5.Parser();

	var request = require("request");
	request({
	  uri: input,
	}, function(error, response, body) {
		//console.log(body);
		var document = parser.parse(body);
		var html5libFormat = serializeToTestDataFormat(document, parse5.TreeAdapters.default);
		console.log(html5libFormat);
	});
}

function parseInput(input){

//console.log(input);
if(typeof(input) == 'undefined' || input === null){
	throw new Error('Empty input argument');
}

//Instantiate parser
var parser = new Parser();

//Then feed it with an HTML document
var document = parser.parse(input);

var parse5 =  require('parse5');

//Instantiate new serializer with default tree adapter
var serializer1 = new parse5.Serializer();

/*
//Serialize document
var output = serializer1.serialize(document);

//Show the serialized DOM
console.log(output);
*/

var html5libFormat = serializeToTestDataFormat(document, parse5.TreeAdapters.default);

if(html5libFormat.charAt(html5libFormat.length - 1) == '\n') 
    html5libFormat = html5libFormat.substring(0, html5libFormat.length - 1);

//Show the serialized DOM with HTML5Lib format
console.log(html5libFormat);

/*
//Write the output file
fs.writeFile('output.txt', html5libFormat, function(err){
	if (err) throw err;
	console.log('It\'s saved!');
});
*/

};



// The next function was taken from the test_utils.js file of the parse5 source code. 
// Some constants were replaced to avoid dependency with html.js

//Here the parse5 license
/*
Copyright (c) 2013-2014 Ivan Nikulin (ifaaan@gmail.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
function serializeToTestDataFormat(rootNode, treeAdapter) {
    function getSerializedTreeIndent(indent) {
        var str = '|';

        for (var i = 0; i < indent + 1; i++)
            str += ' ';

        return str;
    }

    function getElementSerializedNamespaceURI(element) {
        switch (treeAdapter.getNamespaceURI(element)) {
            case 'http://www.w3.org/2000/svg':
                return 'svg ';
            case 'http://www.w3.org/1998/Math/MathML':
                return 'math ';
            default :
                return '';
        }
    }

    function serializeNodeList(nodes, indent) {
        var str = '';

        nodes.forEach(function (node) {
            str += getSerializedTreeIndent(indent);

            if (treeAdapter.isCommentNode(node))
                str += '<!-- ' + treeAdapter.getCommentNodeContent(node) + ' -->\n';

            else if (treeAdapter.isTextNode(node))
                str += '"' + treeAdapter.getTextNodeContent(node) + '"\n';

            else if (treeAdapter.isDocumentTypeNode(node)) {
                var parts = [],
                    publicId = treeAdapter.getDocumentTypeNodePublicId(node),
                    systemId = treeAdapter.getDocumentTypeNodeSystemId(node);

                str += '<!DOCTYPE';

                parts.push(treeAdapter.getDocumentTypeNodeName(node) || '');

                if (publicId !== null || systemId !== null) {
                    parts.push('"' + (publicId || '') + '"');
                    parts.push('"' + (systemId || '') + '"');
                }

                parts.forEach(function (part) {
                    str += ' ' + part;
                });

                str += '>\n';
            }

            else {
                var tn = treeAdapter.getTagName(node);

                str += '<' + getElementSerializedNamespaceURI(node) + tn + '>\n';

                var childrenIndent = indent + 2,
                    serializedAttrs = [];

                treeAdapter.getAttrList(node).forEach(function (attr) {
                    var attrStr = getSerializedTreeIndent(childrenIndent);

                    if (attr.prefix)
                        attrStr += attr.prefix + ' ';

                    attrStr += attr.name + '="' + attr.value + '"\n';

                    serializedAttrs.push(attrStr);
                });

                str += serializedAttrs.sort().join('');

                if (tn === 'template' && treeAdapter.getNamespaceURI(node) === 'http://www.w3.org/1999/xhtml') {
                    str += getSerializedTreeIndent(childrenIndent) + 'content\n';
                    childrenIndent += 2;
                    node = treeAdapter.getChildNodes(node)[0];
                }

                str += serializeNodeList(treeAdapter.getChildNodes(node), childrenIndent);
            }
        });

        return str;
    }

    return serializeNodeList(treeAdapter.getChildNodes(rootNode), 0);
};
