<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<html>
<head>
<title>HTML5 report</title>
</head>
<body>

	<div style="padding-top:50px">
	<a href="report.html?reportName=${reportName}" class="btn btn-primary">Return to Report</a>
	</div>

	<c:if test="${not empty test}">
		<h1 class="page-header">
			<small>Comparison details</small>
		</h1>
		<dl>
			<dt>Name</dt>
			<dd>${test.name}</dd>
		</dl>
		<c:if test="${not empty test.input}">
		<dl>
			<dt>Input</dt>
			<dd>
				<!--?prettify lang=html linenums=true?-->
				<pre><c:out value="${test.input}" /></pre>
			</dd>
		</dl>
		</c:if>
		<dl>
			<dt>Output</dt>
			<dd>
				<c:if test="${fn:length(test.outputs) == 1}"><c:set var="active" value=""/></c:if>
				<c:if test="${fn:length(test.outputs) > 1}"><c:set var="active" value="col-md-6"/></c:if>
				<div class="${active}">
					<ul class="nav nav-tabs" role="tablist">
						<li role="output">
							<a href="#formatOptions" aria-controls="tree0" role="tab" data-toggle="tab">
								Format options</a></li>
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
							<c:if test="${fn:length(test.outputs) > 1}">&nbsp;<br>&nbsp;</c:if>
							<!--?prettify lang=html linenums=true?-->
							<pre><c:out value="${test.outputs[0].tree}" escapeXml="false" /></pre>
						</div>
						<div role="tabpanel" class="tab-pane" id="formatOptions">
							<form:form action="testdetails.html?reportName=${reportName}&testName=${testName}"
								method="POST" commandName="formatOptions" role="form" cssClass="checkboxForm">
								<b>Format options:</b>&nbsp;
								<a href="#" data-toggle="tooltip" data-placement="top" 
									title="The content will be removed only if all outputs present no changes on it.">
									<i class="fa fa-info-circle fa-fw"></i></a>
								<span><form:checkbox id="cbPrettify" path="prettify" />
									<label>Pretty code</label></span>
								<span><form:checkbox id="cbOriginalOutput" 
										path="originalOutput" />
									<label>Show original output</label></span>
								<span><form:checkbox id="cbRemoveTextAfterLastDiff" 
								  		path="removeTextAfterLastDiff"/>
								  	<label>Remove text after last difference</label></span>
								<span><form:checkbox id="cbRemoveScriptContent" 
								  		path="removeScriptContent"/>
								  	<label>Remove script elements</label></span>
								<span><form:checkbox id="cbRemoveStyleContent" 
								  		path="removeStyleContent"/>
								  	<label>Remove style elements</label></span>
								<span><form:checkbox id="cbRemoveMetaContent" 
								  		path="removeMetaContent"/>
								  	<label>Remove meta elements</label></span>
								<span><form:checkbox id="cbRemoveLinkContent" 
								  		path="removeLinkContent"/>
								  	<label>Remove link elements</label></span>			
								<span><form:checkbox id="cbRemoveComments" 
								  		path="removeComments"/>
								  	<label>Remove comments</label></span>													
								<button type="submit" class="btn btn-primary">Apply changes!!!</button>
							</form:form>
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
						    <a href="#${tree}" aria-controls="${tree}" role="tab" data-toggle="tab" class="tab">
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
									<div style='float:right; width: 30px;'>
									<div style="position:fixed;" class="panel panel-info">
									    <a href="#" id="display2" class="display"><i class="fa fa-arrow-up fa-fw"></i>top</a><br>
									    <a href="#" id="display1" class="display"><i class="fa fa-backward fa-fw"></i>prev</a><br>
									    <a href="#" id="display" class="display"><i class="fa fa-forward fa-fw"></i>next</a><br>
									    <a href="#" id="display2" class="display"><i class="fa fa-arrow-down fa-fw"></i>bottom</a>									    
									</div></div>
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
				
		$('#tree1').find('span').addClass('current');
	});
	
	$('#cbPrettify').change(function(){
	    if(this.checked)
	    	prettyPrint();
	});
	
	$('a.tab').on('click', function(e) {
        $('span').removeClass('current');
		var tree = $(this).attr('href');
		$(tree).find('span').addClass('current');
	});
	
	$('a.display').on('click', function(e) {
		e.preventDefault();
		var topOffset = 150;
	    var t = $(this).text(),
	    that = $(this);

	    if (t === 'top') {
	    	$('body').animate({
	        	scrollTop: $('span.current').parent().offset().top - topOffset    
	    	});
	    } else if (t === 'bottom') {
	    	$('body').animate({
	        	scrollTop: $('span.current').parent().height() - topOffset    
	    	});
	    } else if (t === 'next' && $('.current').next('span').length > 0) {
	        var $next = $('.current').next('span');
	        var top = $next.offset().top - topOffset;
	        
	        $('.current').removeClass('current');
	      
	        $('body').animate({
	          	scrollTop: top     
	        }, function () {
		        $next.addClass('current');
	        });
	  	} else if (t === 'prev' && $('.current').prev('span').length > 0) {
	        var $prev = $('.current').prev('span');
	        var top = $prev.offset().top - topOffset;
	        
	        $('.current').removeClass('current');
	      
	        $('body').animate({
	          	scrollTop: top     
	        }, function () {
				$prev.addClass('current');
	        });
	  	} else {
			$('body').animate({
	        	scrollTop: $('span.current').offset().top - topOffset    
	    	});
		}
	});
</script> 
</content>