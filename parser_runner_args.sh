parse () {
	parse5output=`nodejs parse5/parser5.js "$3" "$4"`
	jsoupOutput=`java -jar jsoup/JsoupParser.jar "$3" "$4"`
	html5libOutput=`python html5lib/html5libAdapter.py "$3" "$4"`
	#echo `java -jar comparator/Comparator.jar "$1" "$2" "$html5libOutput" "html5lib" "$parse5output" "parse5" "$jsoupOutput" "jsoup"`

	echo "<output name=\"$1\" report=\"$2\">" >> output.xml
	echo "<tree parser=\"parse5\"><![CDATA[$parse5output]]></tree>" >> output.xml
	echo "<tree parser=\"html5lib\"><![CDATA[$html5libOutput]]></tree>" >> output.xml
	echo "<tree parser=\"jsoup\"><![CDATA[$jsoupOutput]]></tree>" >> output.xml
	echo "</output>" >> output.xml

	echo `java -jar comparator/Comparator.jar output.xml`
	rm -f output.xml
	#expected=`cat $(echo $filename | sed 's/data/expected/g')`
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
#echo "$reportName" "$inputType" "$inputValue"

if [ "$inputType" = "-f" ]; then
	if [ "$inputValue" = "" ]; then
		inputValue="./html5libTests/data/adoption01"	
	fi
	for filename in $(find $inputValue -name '*.txt'); do
		echo 'Processing '$filename
		parse "$filename" "$reportName" "$inputType" "$filename"			
	done
else
	if [ "$inputType" = "-u" ]; then
		echo `nodejs parse5/requestUrl.js "$inputValue"` > request.txt
		inputType="-f"
		parse "$inputValue" "$reportName" "$inputType" request.txt
		rm request.txt
	else
		parse "ParserComparison" "$reportName" "$inputType" "$inputValue"
	fi	
fi
