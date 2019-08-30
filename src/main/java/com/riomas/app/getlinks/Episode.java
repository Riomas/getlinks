package com.riomas.app.getlinks;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Episode implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2945752214067798146L;
	
	private int episodeId;
	private String episodeUrl="";
	private String description="";
	private String videoUrl="";
	private String imageUrl="";
	private String title="";
	private String searchUrl="";
	private double duration=0.0;
	private String durationAsMinutes="";
	private String destinationPath="";

	public Episode(int episodeId) {
		this.episodeId = episodeId;
		this.title = "Episódio " + episodeId;
	}
	
	public Episode(int episodeId, String episodeUrl) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		this.setEpisodeId(episodeId);
		this.setEpisodeUrl(episodeUrl, episodeId);
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

	public void setEpisodeUrl(String episodeUrl, int id) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		this.episodeUrl = episodeUrl;
		
		HtmlPage page = GetLinksUtil.getPage(episodeUrl, id);
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

	public void downloadVideo(String prefixPath, String extention, boolean forceDownload) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		setDestinationPath(prefixPath + File.separator + getTitle() + "." + extention);
		File videoFile = GetLinksUtil.downloadFile(videoUrl, getDestinationPath(), forceDownload);
		setDuration(GetLinksUtil.getDuration(videoFile));
	}
	
	public void setDestinationPath(String destinationPath) {
		this.destinationPath = destinationPath;		
	}

	public String getDestinationPath() {
		return destinationPath;
	}
	

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		if (videoUrl != null && videoUrl.startsWith("//")) {
			this.videoUrl = "http:"+videoUrl;
			System.out.println("Update videoUrl : '"+this.videoUrl+"'");
		} else {
			this.videoUrl = videoUrl;
		}
		
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

	public String getDurationAsMinutes() {
		return durationAsMinutes;
	}

	public double getDuration() {
		return duration;
	}
	
	public void setDuration(double duration) {
		this.duration = duration;
		try {
			Date seconds = GetLinksUtil.secondsFormat.parse(String.valueOf((double) this.duration));
			if (this.duration >= 3600.0) {
				this.durationAsMinutes = GetLinksUtil.literalHoursMinutesFormat.format(seconds);
			} else {
				this.durationAsMinutes = GetLinksUtil.literalMinutesFormat.format(seconds);
			}
			System.out.println("Length of video in minutes : " + this.durationAsMinutes);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "Episode [episodeId=" + episodeId + ", episodeUrl=" + episodeUrl + ", description=" + description
				+ ", videoUrl=" + videoUrl + ", imageUrl=" + imageUrl + ", title=" + title + ", searchUrl=" + searchUrl
				+ ", duration=" + duration + "]";
	}

	public void deleteVideo() throws IOException {
		FileUtils.forceDeleteOnExit(new File(getDestinationPath()));
		setDestinationPath("");
	}
	
}
