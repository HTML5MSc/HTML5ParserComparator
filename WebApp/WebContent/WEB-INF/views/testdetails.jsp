<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<html>
<head>
<title>HTML5 report</title>
</head>
<body>

	<a href="report.html?reportName=${reportName}">Return to Report</a>

	<c:if test="${not empty test}">
		<h2>
			<small>Test details</small>
		</h2>
		<dl class="dl-horizontal">
			<dt class="text-uppercase">Name</dt>
			<dd>${test.name}</dd>
		</dl>
		<table class="table">
			<thead>
				<tr>
					<c:forEach var="output" items="${test.outputs}">
						<th><c:forEach var="parser" items="${output.parsers}"
								varStatus="i">
								<c:out value="${parser}" />
								<c:if test="${i.isLast() == false}">,</c:if>
							</c:forEach></th>
					</c:forEach>
				</tr>
			</thead>
			<tbody>
				<tr>
					<c:forEach var="output" items="${test.outputs}">
						<td>
						<!--?prettify lang=html linenums=true?-->
						<pre style="width:${1200/fn:length(test.outputs)- 20}px"><c:out value="${output.tree}" escapeXml="false" /></pre>
						<c:if test="${output.editDistance > 0}">
								<b>Edit distance:</b>&nbsp;${output.editDistance}
						</c:if></td>
					</c:forEach>
				</tr>
			</tbody>
		</table>
	</c:if>

</body>
</html>

<content tag="local_script"> <script type="text/javascript">
	$(document).ready(function() {
		var subCatContainer = $("pre");
		subCatContainer.scroll(function() {
			subCatContainer.scrollTop($(this).scrollTop());
			subCatContainer.scrollLeft($(this).scrollLeft());
		});
		
		prettyPrint();
	});
</script> 
</content>