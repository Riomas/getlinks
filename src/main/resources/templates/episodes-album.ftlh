<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8"/>
	<title>${title}</title>
	<link href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
	<link rel="stylesheet" href="https://getbootstrap.com/docs/4.0/examples/album/album.css" >
</head>
<body>
	<nav class="navbar fixed-top navbar-dark bg-dark shadow-sm">
	    <div class="container d-flex justify-content-between">
	      <a class="navbar-brand d-flex align-items-center" href="#">
	        <strong>${title}</strong>
	      </a>
	      <#list episodes as episode>
			<a class="navbar-brand d-flex align-items-center" href="#episode${episode.episodeId}">${episode.episodeId}</a>
		  </#list>
	    </div>
	  </nav>
	
	<main role="main" class="container my-5 py-5">	
	<div class="album py-5 bg-light">
		
			<div class="row">
				<#list episodes as ep>
					<div class="col-md-4">
			          <div class="card mb-4 shadow-sm" id="episode${ep.episodeId}">
			          	<#if ep.videoUrl?hasContent>
			          		<#if ep.imageUrl?startsWith("//")>
								<#assign imagePath = "http:"+ep.imageUrl />
							<#else>
								<#assign imagePath = ep.imageUrl />
							</#if>
			          		<video id="streaming${ep.episodeId}"  poster="${imagePath}" preload="none"  width="100%" height="196" controls="controls">
							  <source src="${ep.videoUrl}" type="video/mp4">
							  <a target="${ep.title}" href="${ep.videoUrl}" title="${ep.videoUrl}">
								<img src="${imagePath}" style="width: 100%; display: block;"/>
							  </a>
			          		</video>
						<#else>
							<div style="min-height:196px; width: 100%; display: block; background-color:lightgrey">
							</div>
						</#if>
						<div class="card-body" style="min-height:200px;">
						<p>
							<#if ep.destinationPath?hasContent>
							  <#assign episodeUrl = "file:///"+ep.destinationPath />
		                      <a title="Ler video local" href="javascript:play('streaming${ep.episodeId}','${episodeUrl}');" class="btn btn-sm btn-outline-secondary">
		                        <svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
										 width="1.5rem" height="1.5rem" viewBox="0 0 64 64" enable-background="new 0 0 64 64" xml:space="preserve">
									<g id="DOCUMENT__x2F__OK_1_" enable-background="new    ">
										<g id="DOCUMENT__x2F__OK">
											<g>
												<path d="M30,23.008h19L27,2.964v17.037C27,21.662,28.343,23.008,30,23.008z M56,48c-0.828,0-1.578,0.336-2.121,0.879L48,54.757
													l-1.879-1.879C45.578,52.336,44.828,52,44,52c-1.657,0-3,1.343-3,3c0,0.829,0.336,1.578,0.879,2.121l4,4
													C46.422,61.664,47.172,62,48,62s1.578-0.336,2.121-0.879l8-8C58.664,52.579,59,51.828,59,51C59,49.343,57.657,48,56,48z M38,55
													c0-3.314,2.686-6,6-6c1.533,0,2.909,0.6,3.967,1.547L49,49.516v-23.5H30c-3.314,0-6-2.692-6-6.013V3H8C6.343,3,5,4.343,5,6v52
													c0,1.657,1.343,3,3,3h23l0,0h10.516l-1.758-1.757C38.672,58.157,38,56.657,38,55z"/>
											</g>
										</g>
									</g>
								  </svg>
								</a>
						    <#else>
						  		<a title="Nâo esta em local">
						    	<svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
										 width="1.5rem" height="1.5rem" viewBox="0 0 64 64" enable-background="new 0 0 64 64" xml:space="preserve">
									<g id="DOCUMENT__x2F__DELETE_1_" enable-background="new    ">
										<g id="DOCUMENT__x2F__DELETE">
											<g>
												<path d="M30,22.008h19L27,1.964v17.037C27,20.662,28.343,22.008,30,22.008z M41.516,54l-1.758-1.757
													C38.672,51.157,38,49.657,38,48c0-3.314,2.686-6,6-6c1.657,0,3.152,0.675,4.238,1.762l0.005-0.005L49,44.516v-19.5H30
													c-3.314,0-6-2.692-6-6.013V2H8C6.343,2,5,3.343,5,5v52c0,1.657,1.343,3,3,3h28l0,0h2c0-1.657,0.672-3.156,1.758-4.242
													l-0.001-0.001L41.516,54z M54.243,54l3.879-3.878C58.664,49.578,59,48.828,59,48c0-1.657-1.343-3-3-3
													c-0.828,0-1.578,0.336-2.121,0.879L50,49.758l-3.879-3.879C45.578,45.336,44.829,45,44,45c-1.657,0-3,1.343-3,3
													c0,0.828,0.336,1.578,0.879,2.121L45.757,54l-3.879,3.879C41.336,58.422,41,59.172,41,60c0,1.657,1.343,3,3,3
													c0.828,0,1.578-0.336,2.121-0.879L50,58.243l3.879,3.879C54.422,62.664,55.172,63,56,63c1.657,0,3-1.343,3-3
													c0-0.828-0.336-1.578-0.879-2.121L54.243,54z"/>
											</g>
										</g>
									</g>
								</svg>
								</a>
						    </#if>
						    <#if ep.videoUrl?hasContent>
								<a href="javascript:play('streaming${ep.episodeId}','${ep.videoUrl}');"  title="Ler video do site da SIC" class="btn btn-sm btn-outline-secondary">
									<svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
										 width="1.5rem" height="1.5rem" viewBox="0 0 64 64" enable-background="new 0 0 64 64" xml:space="preserve">
										<g id="PLAY_1_" enable-background="new    ">
											<g id="PLAY">
												<g>
													<path d="M46,32c0-1.099-0.592-2.06-1.475-2.583l0,0L22.561,16.438c-0.008-0.005-0.016-0.009-0.024-0.014l-0.011-0.007l0,0
														C22.079,16.153,21.557,16,21,16c-1.657,0-3,1.343-3,3v26c0,1.657,1.343,3,3,3c0.557,0,1.079-0.152,1.526-0.417l0,0l0.011-0.007
														c0.008-0.006,0.016-0.01,0.024-0.014l21.964-12.979l0,0C45.408,34.061,46,33.1,46,32z"/>
												</g>
											</g>
										</g>
									</svg>
								</a>
								<a target="${ep.title}" href="${ep.videoUrl}" title="Ler video do site da SIC (nova pagina)" class="btn btn-sm btn-outline-secondary">
									<svg xmlns="http://www.w3.org/2000/svg" id="Layer_1" enable-background="new 0 0 64 64" viewBox="0 0 64 64" x="0px" y="0px" 
										width="1.5rem" height="1.5rem" xmlns:xml="http://www.w3.org/XML/1998/namespace" xml:space="preserve" version="1.1">
										<g id="BROWSER_1_" enable-background="new    ">
											<g id="BROWSER">
												<g>
													<path d="M 61 4 H 3 C 1.343 4 0 5.343 0 7 v 50 c 0 1.657 1.343 3 3 3 h 58 c 1.657 0 3 -1.343 3 -3 V 7 C 64 5.343 62.657 4 61 4 Z M 24 7 c 1.657 0 3 1.343 3 3 s -1.343 3 -3 3 s -3 -1.343 -3 -3 S 22.343 7 24 7 Z M 15 7 c 1.657 0 3 1.343 3 3 s -1.343 3 -3 3 s -3 -1.343 -3 -3 S 13.343 7 15 7 Z M 6 7 c 1.657 0 3 1.343 3 3 s -1.343 3 -3 3 s -3 -1.343 -3 -3 S 4.343 7 6 7 Z M 61 57 H 3 V 16 h 58 V 57 Z" />
												</g>
											</g>
										</g>
									</svg>
									
								</a>
							</#if>
					    </p>
						
			              <p class="card-text">
			              	<h5>${ep.title}</h5>
							<#if ep.description?hasContent>
							<span>${ep.description}</span>
							<br/>
							</#if>
			              </p>
			              <div class="d-flex justify-content-between align-items-center">
			                <div class="btn-group">
			                  
							  <#if ep.episodeUrl?hasContent>
			                  <a target="${ep.title}" class="btn btn-sm btn-outline-secondary" href="${ep.episodeUrl}" title="${ep.episodeUrl}" role="button" aria-pressed="true">Ver no site de SIC</a>
			                  <#else>
			                  <a target="${ep.title}" class="btn btn-sm btn-outline-secondary" href="${ep.searchUrl}" title="${ep.searchUrl}" role="button" aria-pressed="true">Procurar no site da SIC</a>
			                  </#if>
			                </div>
			                
			                <#if ep.durationAsMinutes?hasContent>
			                <small class="text-muted">${ep.durationAsMinutes}</small>
			                </#if>
			              </div>
			            </div>
			          </div>
			        </div>
				</#list>
			</div>
		
	</div>
	</main>
	<script>
		var play = function(videoId, url) {
			var video = document.getElementById(videoId);
			
			if (unescape(video.src) == url && video.currentTime>0) {
				if (video.paused) {
					video.play();
				} else {
					video.pause();
				}
			} else {
				//alert('src: "'+unescape(video.src)+'"\n url: "'+url+'"');
				video.src=url;
				video.play();
			}
		}
		
	</script>
</body>
</html>