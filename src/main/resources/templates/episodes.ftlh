<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8"/>
	<title>${title}</title>
	<link href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
	<link rel="stylesheet" href="https://getbootstrap.com/docs/4.0/examples/album/album.css" >
</head>
<body>
	<div class="container">
		<nav class="navbar fixed-top navbar-expand-lg navbar-dark bg-dark">
			<a class="navbar-brand" href="#">${title}</a>
			<button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarepisodes" aria-controls="navbarepisodes" aria-expanded="false" aria-label="Toggle navigation">
				<span class="navbar-toggler-icon"></span>
			</button>
			<div class="collapse navbar-collapse" id="navbarepisodes">
				<div class="navbar-nav">
				<#list episodes as episode>
					<a class="nav-item nav-link" href="#episode${episode.episodeId}">${episode.episodeId}</a>
				</#list>
				</div>
			</div>
		</nav>
	</div>
		
	<div class="album">
		<div class="container">
			<div class="row">
				<#list episodes as ep>
					<div class="card" id="episode${ep.episodeId}">
						<a target="_blank" href="${ep.videoUrl}" title="${ep.title}">
							<#if ep.imageUrl?startsWith("//")>
								<#assign imagePath = "http:"+ep.imageUrl />
							<#else>
								<#assign imagePath = ep.imageUrl />
							</#if>
							<img src="${imagePath}" style="width: 100%; display: block;"/>
						</a>
						<p class="card-text">
							<h4>${ep.title}</h4>
							
							<#if ep.description??>
							<span>${ep.description}</span>
							<br/>
							</#if>
							
							<#if ep.episodeUrl??>
								<a target="_blank" href="${ep.episodeUrl}">Ver...</a>
							<#else>
								<a target="_blank" href="${ep.searchUrl}">Procurar...</a>
							</#if>
							
						</p>
					</div>
				</#list>
			</div>
		</div>
	</div>
</body>

</html>