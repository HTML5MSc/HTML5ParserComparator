function parse {
	parse5output=`nodejs parse5/parser5.js "$3" "$4"`
	jsoupOutput=`java -jar jsoup/JsoupParser.jar "$3" "$4"`
	html5libOutput=`python html5lib/html5libAdapter.py "$3" "$4"`
	echo `java -jar comparator/Comparator.jar "$1" "$2" "$html5libOutput" "html5lib" "$parse5output" "parse5" "$jsoupOutput" "jsoup"`
	#expected=`cat $(echo $filename | sed 's/data/expected/g')`
	#echo `java -jar comparator/Comparator.jar "$filename" "$reportName" "$expected" "expected" "$html5libOutput" "html5lib" "$parse5output" "parse5" "$jsoupOutput" "jsoup"`
}

#default values
reportName="report2.xml"
inputType="-s"
inputValue="test"
while getopts n:f:s:u: option 
do case "${option}" in 
	n) reportName=${OPTARG};; 
	f) inputType="-f" inputValue=${OPTARG};; 
	s) inputType="-s" inputValue=${OPTARG};;
	u) inputType="-u" inputValue=${OPTARG};;  
esac done

rm -f "$reportName"
echo "Report" > "$reportName"

if [ "$inputType" = "-f" ]; then
	if [ "$inputValue" = "" ]; then
		inputValue="./html5libTests/data/adoption01"	
	fi
	for filename in $(find $inputValue -name '*.txt'); do
		echo 'Processing '$filename
		parse "$filename" "$reportName" "$inputType" "$filename"			
	done
else
	parse "ParserComparison" "$reportName" "$inputType" "$inputValue"	
fi
