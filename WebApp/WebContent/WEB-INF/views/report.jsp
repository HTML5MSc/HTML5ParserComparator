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

	<c:if test="${not empty report.tests}">
		<h2>
			<small>Test list</small>
		</h2>
		<table class="table table-striped table-bordered">
			<thead>
				<tr>
					<th>Name</th>
					<th>All equal</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="test" items="${report.tests}">
					<tr>
						<td><c:out value="${test.name}" /></td>
						<td><c:out value="${test.allEqual}" /> <c:if
								test="${test.allEqual == false}">
							&nbsp;|&nbsp;<a href="testdetails.html?testName=${test.name}">View
									differences</a>
							</c:if></td>

					</tr>
				</c:forEach>
			</tbody>
		</table>
		<div class="container text-center">
			<ul class="pagination ">
				<li><a href="#">1</a></li>
				<li><a href="#">2</a></li>
				<li><a href="#">3</a></li>
				<li><a href="#">4</a></li>
				<li><a href="#">5</a></li>
			</ul>
		</div>
	</c:if>
</body>
</html>