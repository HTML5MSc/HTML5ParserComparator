<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<html>
<head>
<title>HTML5 tracer</title>
</head>
<body>	
	<h1 class="page-header">
		<small>Tracer details</small>
	</h1>	
	<form:form action="traceform.html" role="form" commandName="tracerInput"
		cssClass="checkboxForm">	
		
		<ul class="nav nav-tabs" role="tablist">
			<li role="output" class="active">
			<a href="#inputPanel" aria-controls="inputPanel" role="tab" data-toggle="tab" class="active">
				Input</a></li>
			<c:if test="${not empty eventTypes}">
				<li role="output">
				<a href="#eventsPanel" aria-controls="eventTypes" role="tab" data-toggle="tab">
					Event exclusions</a></li>
				<li role="output">
				<a href="#sectionsPanel" aria-controls="sections" role="tab" data-toggle="tab">
					Spec section exclusions</a></li>
			</c:if>
			<li>
				<label class="checkbox-inline">
					<form:checkbox id="cbPrettify" path="prettify" />&nbsp;Pretty code&nbsp;</label>
				<button type="submit" class="btn btn-primary">Parse and
					trace!!!</button>
			</li>
		</ul>
		<div class="tab-content">
			<div role="tabpanel" class="tab-pane form-horizontal active" id="inputPanel">
				<c:if test="${not empty error}">
					<p class="text-danger">${error}</p></c:if>
				<dl>						
					<dt>Type of input</dt>
					<dd><div style="width: 200px">
						<form:select path="type" items="${inputTypeList}"
							class="form-control input-small" /></div></dd>
					<dt>Input:</dt>
					<dd><form:textarea path="input" class="form-control"
						placeholder="Insert the string here" rows="8" /></dd>
				</dl>
			</div>									
			<c:if test="${not empty eventTypes}">
			<div role="tabpanel" class="tab-pane" id="eventsPanel">
				<span><input type="checkbox" id="selectAllEventTypes">
					<label for="selectAllEventTypes">Select all</label></span>
				<form:checkboxes items="${eventTypes}" path="eventTypes" />
			</div>
			<div role="tabpanel" class="tab-pane" id="sectionsPanel">
				<span><input type="checkbox" id="selectAllSections">
					<label for="selectAllSections">Select all</label></span>
				<form:checkboxes items="${sections}" path="sections" />
			</div>		
			</c:if>
		</div>		
	</form:form><br>
	<c:if test="${not empty output}"><div>
		<div class="col-md-6">
			<ul class="nav nav-tabs" role="tablist">	
				<li role="output" class="active">
				<a href="#outputPanel" aria-controls="tree0" role="tab" data-toggle="tab" class="active">
				Parser output</a></li>				    
			</ul>	
			<div class="tab-content">
				<div role="tabpanel" class="tab-pane active" id="outputPanel">													
					<!--?prettify lang=html linenums=true?-->
					<pre><c:out value="${output}" escapeXml="false" /></pre>
				</div>
			</div>
		</div>
		<div class="col-md-6">
			<ul class="nav nav-tabs" role="tablist">
				<li role="output" class="active">						
			    <a href="#tracerPanel" aria-controls="tree0" role="tab" data-toggle="tab" class="active">
			    Tracer log</a></li>
			    <li role="output">						
			    <a href="#summaryPanel" aria-controls="tree0" role="tab" data-toggle="tab">
			    Tracer summary</a></li>				    
			</ul>										 
			<div class="tab-content">
				<div role="tabpanel" class="tab-pane active" id="tracerPanel">
					<div>
						<div style="display:inline-block" class="tracer_Algorithm"><b>Algorithms</b></div>
						<div style="display:inline-block" class="tracer_InsertionMode"><b>Insertion modes</b></div>
						<div style="display:inline-block" class="tracer_ParseError"><b>Parse errors</b></div>
						<div style="display:inline-block" class="tracer_TokenizerState"><b>Tokenizer states</b></div>	
					</div> 
					<c:set var="maxEventsToDisplay" value="300"/>
					<c:if test="${fn:length(tracer.parseEvents) > maxEventsToDisplay}">
						<div class="alert alert-warning"><strong>Log too large to display</strong>
						&nbsp;Try making some exclusions to display it.</div></c:if>
					<c:if test="${fn:length(tracer.parseEvents) <= maxEventsToDisplay}">  													
					<c:forEach var="event" items="${tracer.parseEvents}">
						<div class="tracer_${event.type}">										
							<b><c:out value="${event.section}" />&nbsp;
							<c:out value="${event.description}" />&nbsp;</b>
							<c:out value="${event.additionalInfo}" />
						</div>
					</c:forEach>
					</c:if>
				</div>
				<div role="tabpanel" class="tab-pane" id="summaryPanel">
					<dl class="dl-horizontal">						
						<dt>Algorithms</dt>
						<dd>
							<a role="button" data-toggle="collapse" 
								href="#collapseAlgorithms" aria-expanded="false" 
								aria-controls="collapseAlgorithms">
							  	${tracer.summary.algorithms} of ${fn:length(algorithms)}
								&nbsp;<i class="fa fa-expand fa-fw"></i></a>
							<div class="collapse checkboxForm" id="collapseAlgorithms">							
								<form:checkboxes items="${algorithms}" 
									path="tracer.usedAlgorithms" disabled="true" /></div>
						</dd>
						<dt>Insertion modes</dt>
						<dd>
							<a role="button" data-toggle="collapse" 
								href="#collapseInsertionModes" aria-expanded="false" 
								aria-controls="collapseInsertionModes">
							  	${tracer.summary.insertionModes} of 23 
								&nbsp;<i class="fa fa-expand fa-fw"></i></a>
							<div class="collapse checkboxForm" id="collapseInsertionModes">							
								<form:checkboxes items="${insertionModes}" 
									path="tracer.usedInsertionModes" disabled="true" /></div>
						</dd>
						<dt>Tokenizer states</dt>
						<dd>
							<a role="button" data-toggle="collapse" 
								href="#collapseTokenizerStates" aria-expanded="false" 
								aria-controls="collapseTokenizerStates">
							  	${tracer.summary.tokenizerStates} of 69
							  	&nbsp;<i class="fa fa-expand fa-fw"></i></a>
							<div class="collapse checkboxForm" id="collapseTokenizerStates">
								<form:checkboxes items="${tokenizerStates}" 
									path="tracer.usedTokenizerStates" disabled="true" /></div>
						</dd>
						<dt>Tokens emitted</dt>
						<dd>${tracer.summary.emittedTokens}</dd>
						<dt>Parse errors</dt>
						<dd>${tracer.summary.parseErrors}</dd>
						<dt>Formatting elements&nbsp;
							<a href="#" data-toggle="tooltip" data-placement="top" 
								title="W3C Recommendation - Formatting elements: ${formattingElements}">
								<i class="fa fa-info-circle fa-fw"></i></a></dt>
						<dd><div class="checkboxForm"><form:checkbox disabled="true"
							path="tracer.summary.formattingElements" /></div></dd>
						<dt>Special elements&nbsp;
							<a href="#" data-toggle="tooltip" data-placement="top" 
								title="W3C Recommendation - Special elements: ${specialElements}">
								<i class="fa fa-info-circle fa-fw"></i></a></dt>
						<dd><div class="checkboxForm"><form:checkbox disabled="true"
							path="tracer.summary.specialElements" /></div></dd>
						<dt>MathML elements</dt>
						<dd><div class="checkboxForm"><form:checkbox disabled="true"
							path="tracer.summary.mathMlElements" /></div></dd>
						<dt>SVG elements</dt>
						<dd><div class="checkboxForm"><form:checkbox disabled="true"
							path="tracer.summary.svgElements" /></div></dd>
						<dt>New HTML5 Form elements
							<a href="#" data-toggle="tooltip" data-placement="top" 
								title="New HTML5 Form elements: ${html5FormElements}">
								<i class="fa fa-info-circle fa-fw"></i></a></dt>
						<dd><div class="checkboxForm"><form:checkbox disabled="true"
							path="tracer.summary.html5FormElements" /></div></dd>
						<dt>New HTML5 Graphic elements
							<a href="#" data-toggle="tooltip" data-placement="top" 
								title="New HTML5 Graphic elements: ${html5GraphicElements}">
								<i class="fa fa-info-circle fa-fw"></i></a></dt>
						<dd><div class="checkboxForm"><form:checkbox disabled="true"
							path="tracer.summary.html5GraphicElements" /></div></dd>
						<dt>New HTML5 Media elements
							<a href="#" data-toggle="tooltip" data-placement="top" 
								title="New HTML5 Media elements: ${html5MediaElements}">
								<i class="fa fa-info-circle fa-fw"></i></a></dt>
						<dd><div class="checkboxForm"><form:checkbox disabled="true"
							path="tracer.summary.html5MediaElements" /></div></dd>
						<dt>New HTML5 Semantic/Structural elements
							<a href="#" data-toggle="tooltip" data-placement="top" 
								title="New HTML5 Semantic / Structural elements: ${html5SemanticElements}">
								<i class="fa fa-info-circle fa-fw"></i></a></dt>
						<dd><div class="checkboxForm"><form:checkbox disabled="true"
							path="tracer.summary.html5SemanticStructuralElements" /></div></dd>
					</dl>
				</div>
			</div>
  		</div>
  	</div></c:if>
</body>
</html>

<content tag="local_script"> <script type="text/javascript">
	$(document).ready(function() {
		
		$('#selectAllEventTypes').click(function(event) {
			var checkAll = this.checked;
            $("input[name='eventTypes']").each(function() { 
                this.checked = checkAll;               
            });	        
	    });
		
		$('#selectAllSections').click(function(event) { 
			var checkAll = this.checked;
            $("input[name='sections']").each(function() { 
                this.checked = checkAll;               
            });	        
	    });
		
		if($('#cbPrettify').prop('checked'))
	    	prettyPrint();
		
		$('[data-toggle="tooltip"]').tooltip(); 
		
		$('#type').on('change', function() {
			$('#input').removeAttr('placeholder');
			$('#input').removeAttr('rows');
			$('#input').removeAttr('style');
			if($(this).val() == 'URL'){
				$('#input').attr('placeholder','Insert the URL here');
				$('#input').attr('rows','1');
			}
			else{
				$('#input').attr('placeholder','Insert the string here');
				$('#input').attr('rows','12');
				
			}
		});
		
		if($('#type').val() == 'URL'){
			$('#input').attr('placeholder','Insert the URL here');
			$('#input').attr('rows','1');
		}
		else{
			$('#input').attr('placeholder','Insert the string here');
			$('#input').attr('rows','12');
			
		}
	});	
	
	$('#cbPrettify').change(function(){
	    if(this.checked)
	    	prettyPrint();
	});
</script> 
</content>