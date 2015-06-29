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
		<dl>
			<dt class="text-uppercase">Name</dt>
			<dd>${test.name}</dd>
		</dl>
		<c:if test="${not empty test.input}">
		<dl>
			<dt class="text-uppercase">Input</dt>
			<dd>
				<!--?prettify lang=html linenums=true?-->
				<pre><c:out value="${test.input}" /></pre>
			</dd>
		</dl>
		</c:if>
		<dl>
			<dt class="text-uppercase">Output</dt>
			<dd>
				<form:form method="POST" action="testdetails.html?reportName=${reportName}&testName=${testName}"
					commandName="formatOptions" role="form">
					<b>Format options:</b>&nbsp;
					<a href="#" data-toggle="tooltip" data-placement="top" 
						title="The content will be removed only if all outputs present no changes on it.">
						<i class="fa fa-info-circle fa-fw"></i>
					</a>&nbsp;
					<label class="checkbox-inline">
						<form:checkbox id="cbPrettify" 
							path="prettify" /> Pretty code
					</label>
					<label class="checkbox-inline">
					  	<form:checkbox id="cbRemoveTextAfterLastDiff" 
					  		path="removeTextAfterLastDiff"/> Remove text after last difference
					</label>
					<label class="checkbox-inline">
					  	<form:checkbox id="cbRemoveScriptContent" 
					  		path="removeScriptContent"/> Remove script content
					</label>
					<label class="checkbox-inline">
					  	<form:checkbox id="cbRemoveStyleContent" 
					  		path="removeStyleContent"/> Remove style content
					</label>					
					<label class="checkbox-inline">
					  	<form:checkbox id="cbRemoveComments" 
					  		path="removeComments"/> Remove comments
					</label>
										
					<button type="submit" class="btn btn-default">Apply changes!!!</button>
				</form:form><br/>
				<c:if test="${fn:length(test.outputs) == 1}"><c:set var="active" value=""/></c:if>
				<c:if test="${fn:length(test.outputs) > 1}"><c:set var="active" value="col-md-6"/></c:if>
				<div class="${active}">
					<ul class="nav nav-tabs" role="tablist">
						<li role="output" class="active">
						<a href="#tree0" aria-controls="tree0" role="tab" data-toggle="tab" class="active">
							<c:forEach var="parser" items="${test.outputs[0].parsers}"
								varStatus="i">
								<c:out value="${parser}" />
								<c:if test="${i.isLast() == false}">,</c:if></c:forEach>
						</a></li>
					</ul>
					<div class="tab-content">
						<div role="tabpanel" class="tab-pane active" id="tree0">
							&nbsp;<br>&nbsp;
							<!--?prettify lang=html linenums=true?-->
							<pre><c:out value="${test.outputs[0].tree}" escapeXml="false" /></pre>
						</div>
					</div>
				</div>
				<c:if test="${fn:length(test.outputs) > 1}">
					<div class="col-md-6">
						<ul class="nav nav-tabs" role="tablist">
						<c:forEach var="output" items="${test.outputs}" varStatus="i">
						<c:if test="${i.isFirst() == false}">
							<c:set var="tree" value="tree${i.index}"/>
							<c:if test="${i.index == 1}"><c:set var="active" value="active"/></c:if>
							<c:if test="${i.index > 1}"><c:set var="active" value=""/></c:if>
						    <li role="output" class="${active}">
						    <a href="#${tree}" aria-controls="${tree}" role="tab" data-toggle="tab">
						    	<c:forEach var="parser" items="${output.parsers}"
									varStatus="i">
									<c:out value="${parser}" />
									<c:if test="${i.isLast() == false}">,</c:if>
								</c:forEach>
						    </a></li>
						</c:if>
						</c:forEach>
						</ul>
						
						<div class="tab-content">
							<c:forEach var="output" items="${test.outputs}" varStatus="i">
							<c:if test="${i.isFirst() == false}">
								<c:set var="tree" value="tree${i.index}"/>
								<c:if test="${i.index == 1}"><c:set var="active" value=" active"/></c:if>
								<c:if test="${i.index > 1}"><c:set var="active" value=""/></c:if>								
    							<div role="tabpanel" class="tab-pane${active}" id="${tree}">
    								<b>Insertions:</b>&nbsp;${output.insertions}&nbsp;(${output.charInsertions} chars)<br>
									<b>Deletions:</b>&nbsp;${output.deletions}&nbsp;(${output.charDeletions} chars)
									<c:if test="${output.editDistance > 0}">
										<b>Edit distance:</b>&nbsp;${output.editDistance}
									</c:if>
									<!--?prettify lang=html linenums=true?-->
									<pre><c:out value="${output.tree}" escapeXml="false" /></pre>
    							</div>
	    					</c:if>
	    					</c:forEach>
	    				</div>
					</div>
				</c:if>			
			</dd>
		</dl>			
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
		
		if($('#cbPrettify').prop('checked'))
	    	prettyPrint();
		
		$('[data-toggle="tooltip"]').tooltip(); 
	});
	
	$('#cbPrettify').change(function(){
	    if(this.checked)
	    	prettyPrint();
	});
</script> 
</content>