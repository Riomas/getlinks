package com.riomas.app.getlinks;

import java.io.IOException;
import java.net.MalformedURLException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Episode {

	private int episodeId;
	private String episodeUrl="";
	private String description="";
	private String videoHtml="";
	private String videoUrl="";
	private String imageUrl="";
	private String title="";
	private String searchUrl="";

	public Episode(int episodeId) {
		this.episodeId = episodeId;
		this.title = "Episódio " + episodeId;
	}
	
	public Episode(int episodeId, String episodeUrl) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		this.setEpisodeId(episodeId);
		this.setEpisodeUrl(episodeUrl);
	}

	public int getEpisodeId() {
		return episodeId;
	}

	public void setEpisodeId(int episodeId) {
		this.episodeId = episodeId;
	}

	public String getEpisodeUrl() {
		return episodeUrl;
	}

	public void setEpisodeUrl(String episodeUrl) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		this.episodeUrl = episodeUrl;
		//setVideoHtml(GetLinksUtil.getVideoHtml(episodeUrl));
		HtmlPage page = GetLinksUtil.getPage(episodeUrl);
		try {
			setVideoUrl(GetLinksUtil.getVideoUrl(page));
		} catch (TagNameNotFoundException e) {
			System.out.println(e.getMessage());
		}
		try {
			setImageUrl(GetLinksUtil.getImageUrl(page));
		} catch (TagNameNotFoundException e) {
			System.out.println(e.getMessage());
		}
		setTitle(GetLinksUtil.getTitle(page));
		setDescription(GetLinksUtil.getDescription(page));
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVideoHtml() {
		return videoHtml;
	}

	public void setVideoHtml(String videoHtml) {
		this.videoHtml = videoHtml;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSearchUrl() {
		return searchUrl;
	}

	public void setSearchUrl(String searchUrl) {
		this.searchUrl = searchUrl;
	}

}
