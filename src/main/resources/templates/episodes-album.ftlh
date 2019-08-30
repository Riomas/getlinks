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
			            <a target="${ep.title}" href="${ep.videoUrl}" title="${ep.videoUrl}">
							<#if ep.imageUrl?startsWith("//")>
								<#assign imagePath = "http:"+ep.imageUrl />
							<#else>
								<#assign imagePath = ep.imageUrl />
							</#if>
							<img src="${imagePath}" style="width: 100%; display: block;"/>
						</a>
						<div class="card-body">
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
			                <#if ep.destinationPath?hasContent>
		                      <a title="${ep.destinationPath}">
		                        <svg width="1rem" aria-hidden="true" focusable="false" data-prefix="far" data-icon="file-video" class="svg-inline--fa fa-file-video fa-w-12" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 384 512"><path fill="currentColor" d="M369.941 97.941l-83.882-83.882A48 48 0 0 0 252.118 0H48C21.49 0 0 21.49 0 48v416c0 26.51 21.49 48 48 48h288c26.51 0 48-21.49 48-48V131.882a48 48 0 0 0-14.059-33.941zM332.118 128H256V51.882L332.118 128zM48 464V48h160v104c0 13.255 10.745 24 24 24h104v288H48zm228.687-211.303L224 305.374V268c0-11.046-8.954-20-20-20H100c-11.046 0-20 8.954-20 20v104c0 11.046 8.954 20 20 20h104c11.046 0 20-8.954 20-20v-37.374l52.687 52.674C286.704 397.318 304 390.28 304 375.986V264.011c0-14.311-17.309-21.319-27.313-11.314z"></path></svg>
						      </a>
						    </#if>
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
</body>

</html>