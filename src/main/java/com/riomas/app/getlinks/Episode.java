package com.riomas.app.getlinks;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Data
@ToString
@Builder
@AllArgsConstructor
public class Episode implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2945752214067798146L;
	
	private int episodeId;
	@Builder.Default
	private String episodeUrl="";
	@Builder.Default
	private String description="";
	@Builder.Default
	private String videoUrl="";
	@Builder.Default
	private String imageUrl="";
	@Builder.Default
	private String title="";
	@Builder.Default
	private String searchUrl="";
	@Builder.Default
	private double duration=0.0;
	@Builder.Default
	private String durationAsMinutes="";
	@Builder.Default
	private String literalDuration="";
	@Builder.Default
	private String destinationPath="";

	public Episode(int episodeId) {
		this.episodeId = episodeId;
		this.title = "Episódio " + episodeId;
	}
	
	public Episode(int episodeId, String episodeUrl) {
		this.setEpisodeId(episodeId);
		this.setEpisodeUrl(episodeUrl);
	}

	public void setEpisodeUrl(String episodeUrl) {
		this.episodeUrl = episodeUrl;
		
		HtmlPage page = GetLinksUtil.getPage(this.episodeUrl)
				.orElseThrow(() -> new NullPointerException("Page not accessible: " + this.episodeUrl));
		try {
			setVideoUrl(GetLinksUtil.getVideoUrl(page));
		} catch (TagNameNotFoundException | AttributeSrcNotFoundException e) {
			System.out.println(e.getMessage());
		}
		try {
			setImageUrl(GetLinksUtil.getImageUrl(page));
		} catch (TagNameNotFoundException | AttributeSrcNotFoundException e) {
			System.out.println(e.getMessage());
		}
		setTitle(GetLinksUtil.getTitle(page));
		setDescription(GetLinksUtil.getDescription(page));
		setEpisodeId(GetLinksUtil.getEpisodeId(getTitle(), episodeId));
	}

	public void downloadVideo(String prefixPath, String extention, boolean forceDownload) throws IOException {
		setDestinationPath(prefixPath + File.separator + getTitle().trim() + "." + extention);
		File videoFile = GetLinksUtil.downloadFile(videoUrl, getDestinationPath(), forceDownload);
		setDuration(GetLinksUtil.getDuration(videoFile));
	}
	
	public void setDestinationPath(String destinationPath) {
		if (destinationPath != null) {
			this.destinationPath = destinationPath.replace(File.separatorChar, '/');
		}
	}

	public void setVideoUrl(String videoUrl) {
		if (videoUrl != null && videoUrl.startsWith("//")) {
			this.videoUrl = "http:"+videoUrl;
			System.out.println("Update videoUrl : '"+this.videoUrl+"'");
		} else {
			this.videoUrl = videoUrl;
		}
		
	}

	public void setDuration(double duration) {
		this.duration = duration;
		try {
			Date seconds = FormatFactory.getSecondsFormat().parse(String.valueOf(this.duration));
			if (this.duration >= 3600.0) {
				this.literalDuration = FormatFactory.getLiteralHoursMinutesFormat().format(seconds);
				this.durationAsMinutes = FormatFactory.getHoursMinutesFormat().format(seconds);
			} else {
				this.literalDuration = FormatFactory.getLiteralMinutesFormat().format(seconds);
				this.durationAsMinutes = FormatFactory.getMinutesFormat().format(seconds);
			}
			System.out.println("Length of video in minutes : " + this.durationAsMinutes);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
	}

	public void deleteVideo() throws IOException {
		assert getDestinationPath() != null;
		FileUtils.forceDeleteOnExit(new File(getDestinationPath()));
		setDestinationPath("");
	}

	@Override
	public int hashCode() {
		return Objects.hash(episodeId, title);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Episode other = (Episode) obj;
		return episodeId == other.episodeId && Objects.equals(title, other.title);
	}
}
