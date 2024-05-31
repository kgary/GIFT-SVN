<html>
	<head>	
		<title>RenderingSDK test page</title>
		<meta content="text/html; charset=UTF-8" http-equiv="Content-Type">		
 		<link href="./css/test/main.css" type="text/css" rel="stylesheet">
		<link href="./css/test/imageTbl.css" type="text/css" rel="stylesheet">   	
    	<link href="./css/test/jquery-ui-1.9.1.custom.css" type="text/css" rel="stylesheet">
    			
    	<script src="./css/test/js/jquery-1.8.2.js" type="text/javascript" ></script>  
    	<script src="./css/test/js/jquery-ui-1.9.1.custom.js" type="text/javascript" ></script> 	
	</head>
	<body>
		<div id="container">
			<div id="header">
				<h1 class="title">
					<img src="css/test/images/armyStar.jpg" width="50px" height="50px" 
					     style="vertical-align: middle;padding-right: 15px;">Services Test Page
				</h1>
				<div class="nav">RenderingSDK Project</div>
			</div>
			<div id="content">
				<div id="pingDiv">	
					<div style="margin-bottom:35px;float:left">SEC RenderingSDK Test pages for the spring web services.</div>
					<div style="margin-bottom:35px;float:right"><span style="padding-right:8px;">Service Avaliable:</span>
						<img id="avaibleImg" style="vertical-align: middle;" height=22 width=22 src="css/test/images/bullet_green.png"/>
					</div>		
					<div class="space"></div>
				</div>
					
				<h1 class="section" style="margin-top:50px;">Service Details</h1>
					<table class="t1" summary="Services" id="background-image">
						<thead>
							<tr>
								<th scope="col">Service</th>
								<th scope="col">Service Type</th>
								<th scope="col">Content Type</th>
								<th scope="col">Return Type</th>
								<th scope="col">URL</th>
							</tr>
						</thead>
						<tfoot>
							<tr>
								<td colspan="5">IE 6 users may not see the transparent background</td>
							</tr>
						</tfoot>
						<tbody>
							<tr>
								<td>ImageGeneratorController</td>
								<td>Spring</td>
								<td>Image</td>
								<td>byte[]</td>
								<td class="urlField">
									<ul>
										<li style="padding-bottom:8px;"><a href="http://localhost:8080/mil-sym-service/renderer/image/SFGP-----------?T=uniquedesignation&n=ENY&h=USA" target="_blank">http://localhost:8080/mil-sym-service/renderer/image/SFGP-----------?T=uniquedesignation&amp;n=ENY&amp;h=USA</a></li>
										<li><a href="http://localhost:8080/mil-sym-service/renderer/image/GFGPGPP-------X?T=uniquedesignation&n=ENY&h=USA" target="_blank">http://localhost:8080/mil-sym-service/renderer/image/GFGPGPP-------X?T=uniquedesignation&amp;n=ENY&amp;h=USA</a></li>
									</ul>
								</td>
							</tr>
							<tr>
								<td>ImageGeneratorServlet</td>
								<td>Spring</td>
								<td>kml</td>
								<td>byte[]</td>
								<td class="urlField">									
									<ul>
										<li><a href="http://localhost:8080/mil-sym-service/renderer/kml/SFGP-----------?name=testKML&description=globeView&lat=-39&lon=-78&alt=25000&id=test1" target="_blank">localhost:8080/mil-sym-service/renderer/kml/SFGP-----------?name=testKML&amp;description=globeView&amp;lat=-39&amp;lon=-78&amp;alt=25000&amp;id=test1</a></li>
									</ul>
								</td>
							</tr>
							<tr>
								<td>DirectoryReaderController</td>
								<td>Spring</td>
								<td>http</td>
								<td>comma delimited String</td>
								<td class="urlField">
									<ul>
										<li><a href="http://localhost:8080/mil-sym-service/renderer/pluginList" target="_blank">http://localhost:8080/mil-sym-service/renderer/pluginList</a></li>
									</ul>
								</td>
							</tr>
							<tr>
								<td>DirectoryReaderServlet</td>
								<td>Web Servlet 3.0</td>
								<td>http</td>
								<td>comma delimited String</td>
								<td class="urlField">									
									<ul>
										<li><a href="http://localhost:8080/mil-sym-service/directoryReader" target="_blank">http://localhost:8080/mil-sym-service/directoryReader</a></li>
									</ul>
								</td>
							</tr>
						</tbody>
					</table>
									
<%-- 	 	    		<%System.out.println("\u0026");%>
		    		<%System.out.println("\u0026");%>
		    		<h1>&#0233;</h1>
		    		<h1><%="\u0026" %></h1> 
				</div>	--%>
				
				<h1 class="section" style="margin-top:50px">Point Test Pages</h1>
				<div class="h2_content">	
					<table summary="PointServer" id="background-image" style="background:url()">
						<thead>
							<tr>
								<th scope="col">Name</th>
								<th scope="col">Description</th>
								<th scope="col">URL</th>
								<th scope="col">Web Service Generated Graphic</th>
							</tr>
						</thead>
						<tfoot>
							<tr>
								<td colspan="5">IE 6 users may not see the transparent background</td>
							</tr>
						</tfoot>
						<tbody>
							<tr>
								<td><a href="http://localhost:8080/mil-sym-service/singlePoints.html" target="_blank">SinglePoints Test Page</a></td>
								<td>test single point graphic creation</td>
								<td><a href="http://localhost:8080/mil-sym-service/singlePoints.html" target="_blank">http://localhost:8080/mil-sym-service/singlePoints.html</a></td>
								<td><img src="http://localhost:8080/mil-sym-service/renderer/image/GFGPGPP-------X?T=uniquedesignation&n=ENY&h=USA" alt="GFGPGPP-------X"/> </td>
							</tr>
							<tr>
								<td><a href="http://localhost:8080/mil-sym-service/multiPoints.html" target="_blank">MultiPoints Test Page</a></td>
								<td>test multiple points graphic creation</td>
								<td><a href="http://localhost:8080/mil-sym-service/multiPoints.html" target="_blank">http://localhost:8080/mil-sym-service/multiPoints.html</a></td>
								<td>N/A</td>
							</tr>
						</tbody>
					</table>
				</div>
				
				<div class="space"></div>	
				<div id="symbolCheckerDiv" >
					<div class="divLeft">
						<!-- Start HTML form -->
						<h1 class="section">Symbol Code Checker</h1>	 
					   	<form name="form" method="GET" id="third" action="">								
					        <!-- SymbolCodeChecker -->
							<label for="symbolCode"><strong><span class="blue">*</span> Symbol Code : </strong></label>
							<input id="symbolCode" class="textinput" name="symbolCode" type="text" class="validate['required','length[15,15]']" />
								
							<div class="space"></div>						
							<label for="ReturnTypeRadio"><strong><span class="blue">*</span> Return Type: </strong></label>
							<div id="ReturnTypeRadio">											
								<label class="radio" for="imageRetType">Image</label>
								<input type="radio" id="imageRetType" name="returnType" value="image" />
														
								<label class="radio" for="kmlReturnType"><span class="blue">KML</span></label>	
								<input type="radio" id="kmlReturnType" name="returnType" value="kml" />					
							</div>
												
							<input id="symbolCodeFetcher" style="display:block;margin-top:25px;" type="submit" class="buttonSubmit" value="Preview Symbol Code" />		
						</form>
					</div>				
					<div id="result" class="divRight">						 	
					</div>
				</div>
				<div class="space"></div>				
				<div>
					<input id="ping" style="display:inline;margin-top:15px;" type="submit" value="Ping Service" />
				
					<div id="pingResult" class="pingResult"><span></span></div>
				</div>	
			</div>
			
			
			<script type="text/javascript">		
				var baseUrl = "http://localhost:8080/mil-sym-service/renderer/";
				
				$(function() {
					$("#symbolCodeFetcher").button()
						.click( function( event ) {
							event.preventDefault();							
							$("#result").empty();
							var baseUrl = "http://localhost:8080/mil-sym-service/renderer/";
							var retType = $('input:radio[name=returnType]:checked').val();
							var symbolInfo = $('#symbolCode').val();
							var url = baseUrl + retType + '/' +  symbolInfo;							
							var data = null;
							var tdata = $.get(url);
							
							// ImageEX: SHGPUCDM---M---?T=uniquedesignation&n=ENY&h=USA
							// ImageEX: GFGPGPP-------X?T=uniquedesignation&n=ENY&h=USA
							//		OHVPA----------?symstd=2525B&H=Arson2525
							//      SFGPUCDM---M---?V=35&T=TFOOisangry&H=C--is silly&N=ENY&W=Date&G=G&M=M&Z=speed&X=X&F=R									
							// KML EX : SFGP-----------?name=testKML&description=globeView&lat=-39&lon=-78&alt=25000&id=test1
							if (retType == 'image') {
								data = $("<img />").attr('src', url);								
								$("#result").append(data);								
							} else {
								tdata = $.get(url, function() {
									data = "<p>" + tdata.responseText + "</p>";
									$("#result").append(data);										
								});								
							}										
						});					
				});
				
				
				$(function() {
					$("#ping").button()
						.click( function( event ) {
							event.preventDefault();
							pingService();
						});					
				});

					
				$(function() {
						$("#ReturnTypeRadio").buttonset();
				});
					
				$(document).ready(function() {						
						var watermark = 'Please enter Symbol Code';
						
						// init, set water mark text and class
						$('#symbolCode').val(watermark).addClass('watermark');
						
						// if blur and no value inside,m  set watermark text and class agina.
						$('#symbolCode').blur( function() {
							if ( $(this).val().length == 0 ) {
								$(this).val(watermark).addClass('watermark');
								$(this).removeClass('textInputSelected');
							}
						});
						
						// if focus and text is watermark, set it to empty and remove the watermark class
						$('#symbolCode').focus(function() {
							if ($(this).val() == watermark) {
								$(this).val('').removeClass('watermark');
								$(this).addClass('textInputSelected');
							}
						});
						
						// default check
						$('#imageRetType').attr('checked', true);		
						
						// Ping service for availability
						pingService();
						
						$("#pingDiv").mouseover( function(event) {
			        		pingService();
			        	});			
				});
				
				function pingService() {
					var pingURL = baseUrl + 'ping';
					$.ajax(pingURL, {
						statusCode: {
							404: function() {
								$('#avaibleImg').attr('src', 'css/test/images/bullet_red.png');
								$('#pingResult span').text('FAIL');
								$('#pingResult span').removeClass('success');
								$('#pingResult span').addClass('failure');
							},
							200: function() {
								$('#avaibleImg').attr('src', 'css/test/images/bullet_green.png');
								$('#pingResult span').text('SUCCESS');		
								$('#pingResult span').removeClass('failure');
								$('#pingResult span').addClass('success');																		
							}
						}
					});
				}
						
			</script>

	
			<div id="footer">
				<a href="#">placeHolder1</a> | <a href="#">software</a> | 
				<a href="#">placeHolder2</a> | <a href="#">online</a> | 
				<a href="#">project management</a> | <a href="#">svn</a> | <a href="#">sec</a>
				<p>
					<a href="#"> <img width="35px" height="35px" src="./css/test/images/cecom-logo.png"></a>
				</p>
			</div>			
		</div>	
	</body>
</html>