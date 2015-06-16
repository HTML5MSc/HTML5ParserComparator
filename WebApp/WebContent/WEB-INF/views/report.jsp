<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<title>Spring MVC Tutorial by Crunchify - Hello World Spring MVC
	Example</title>
</head>
<body>



	<h2>
		<small>Report details</small>
	</h2>

	<dl class="dl-horizontal">
		<dt class="text-uppercase">Number of tests</dt>
		<dd>${report.numberOfTests}</dd>
		<dt class="text-uppercase">Equals</dt>
		<dd>${report.testsEqual}</dd>
		<dt class="text-uppercase">Different</dt>
		<dd>${report.testsDifferent}</dd>
		<dt class="text-uppercase">Date</dt>
		<dd>${report.testDate}</dd>
	</dl>



	<c:if test="${not empty report.testResults}">
		<h2>
			<small>Test results</small>
		</h2>
		<table class="table table-striped table-bordered">
			<thead>
				<tr>
					<th>Parser name</th>
					<th>Passed</th>
					<th>Failed</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="testResult" items="${report.testResults}">
					<tr>
						<td><c:out value="${testResult.parserName}" /></td>
						<td><c:out value="${testResult.passed}" /></td>
						<td><c:out value="${testResult.failed}" /></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</c:if>

	<c:if test="${not empty report.testCases}">
		<h2>
			<small>Test list</small>
		</h2>
		<table id="testCases" class="table table-striped table-bordered">
			<thead>
				<tr>
					<th>Name</th>
					<th>All equal</th>
					<th>Number of trees</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="testCase" items="${report.testCases}">
					<tr>
						<td><c:out value="${testCase.name}" /></td>
						<td><c:if test="${testCase.numberOfTrees == 1}">
							Yes&nbsp;|&nbsp;<a
									href="testdetails.html?testName=${testCase.name}">View tree</a>
							</c:if> <c:if test="${testCase.numberOfTrees != 1}">
							No&nbsp;|&nbsp;<a
									href="testdetails.html?testName=${testCase.name}">View
									differences</a>
							</c:if></td>
						<td>${testCase.numberOfTrees}</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</c:if>
</body>
</html>

<content tag="local_script"> <script type="text/javascript">
	$(document).ready(function() {
		$('#testCases').DataTable({
			stateSave: true
		});
	});
</script> </content>