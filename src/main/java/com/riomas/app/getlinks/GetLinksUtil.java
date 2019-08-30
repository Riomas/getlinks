package com.riomas.app.getlinks;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.coremedia.iso.IsoFile;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

public class GetLinksUtil {

	public static final String USER_AGENT_EDGE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 Edge/15.15063";
	public static final String USER_AGENT_CHROME = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36";

	public static final DateFormat secondsFormat = new SimpleDateFormat("ss.SSS");
	public static final DateFormat minutesFormat = new SimpleDateFormat("mm:ss");
	public static final DateFormat hoursMinutesFormat = new SimpleDateFormat("H:mm:ss");
	
	public static final DateFormat literalMinutesFormat = new SimpleDateFormat("mm' min'");
	public static final DateFormat literalHoursMinutesFormat = new SimpleDateFormat("H:mm' min'");
	private static boolean downloadEnabled = false;
	private static boolean durationEnabled = false;
	private static String novelaPath = "";

	public static List<Episode> getAllEpisodes(String hostname, String seasonTag, String searchQueryUrl, int start,
			int stop, boolean forceDownload) {

		List<Episode> episodes = new ArrayList<Episode>();
		Episode episode = null;
		for (int i = start; i <= stop; i++) {
			System.out.println("--------------------------------------------------------");
			String queryUrl = hostname + searchQueryUrl + i;
			System.out.println("queryUrl: " + queryUrl);
			try {
				episode = getEpisode(i, hostname, seasonTag, queryUrl, forceDownload);
				System.out.println("URL: " + episode.getEpisodeUrl());
				
			} catch (IOException e) {
				try {
					episode.deleteVideo();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("Skip episode " + i);
				continue;
			}
			episodes.add(episode);
		}
		return episodes;
	}

	private static Episode getEpisode(int id, String hostname, String seasonTag, String queryUrl, boolean forceDownload) throws IOException {
		String content = getPage(queryUrl, id).getBody().asXml();
		String tagEpisode = seasonTag + "---Episodio-" + id + "-";
		int endIndex = content.indexOf(tagEpisode);
		if (endIndex == -1) {
			Episode ep = new Episode(id);
			ep.setSearchUrl(queryUrl);
			return ep;
		}
		int beginIndex = content.substring(0, endIndex).lastIndexOf("=\"");
		endIndex = content.indexOf("\"", endIndex);
		Episode episode = new Episode(id, hostname + content.substring(beginIndex + 2, endIndex));
		if (downloadEnabled || durationEnabled) {
			episode.downloadVideo(FileUtils.getUserDirectoryPath() + File.separator + "Videos" + novelaPath, "mpg", forceDownload);
			if (!downloadEnabled) {
				episode.deleteVideo();
			}
		}
		return episode;
	}

	static String getVideoHtml(String queryUrl, int id)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException, TagNameNotFoundException {
		HtmlPage page = getPage(queryUrl, id);
		DomNodeList<DomElement> elements = page.getElementsByTagName("video");
		if (elements.size() > 0) {
			return elements.get(0).getParentNode().asXml();
		}
		throw new TagNameNotFoundException(
				"Could not found tagName: 'source' in this page: '" + page.getTitleText() + "'");
	}

	static HtmlPage getPage(String queryUrl, int id)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		WebClient webClient = gethtmlUnitClient();
		// webClient.waitForBackgroundJavaScript(60000);

		try {
			HtmlPage page = null;
			try {
				page = webClient.getPage(queryUrl);
				int cpt = 0;
				while (cpt < 20 && !page.asText().contains("Episódio " + id + " ")) {
					System.out.println((cpt++) + " waiting js jobs : " + webClient.waitForBackgroundJavaScript(1000));
				}
			} catch (Exception e) {
				System.out.println("Get page error: " + e.getMessage());
			}

			return page;
		} finally {
			webClient.close();
		}

	}

	static public WebClient gethtmlUnitClient() {
		WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
		// webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		// webClient.setIncorrectnessListener(new IncorrectnessListenerImpl());
		// webClient.setCssErrorHandler(new DefaultCssErrorHandler());
		// webClient.setJavaScriptErrorListener(new DefaultJavaScriptErrorListener());
		// webClient.setHTMLParserListener(HTMLParserListener.LOG_REPORTER);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setDownloadImages(false);
		webClient.getOptions().setActiveXNative(false);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setTimeout(60000);
		return webClient;

	}

	static String getVideoUrl(HtmlPage page)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException, TagNameNotFoundException {
		List<DomElement> elements = page.getElementsByTagName("source");
		if (elements.size() > 0) {
			return elements.get(0).getAttribute("src");
		}
		throw new TagNameNotFoundException(
				"Could not found tagName: 'source' in this page: '" + page.getTitleText() + "'");
	}

	static String getImageUrl(HtmlPage page)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException, TagNameNotFoundException {
		List<DomElement> elements = page.getElementsByTagName("video");
		if (elements.size() > 0) {
			return elements.get(0).getAttribute("poster");
		}
		throw new TagNameNotFoundException(
				"Could not found tagName: 'video' in this page: '" + page.getTitleText() + "'");
	}

	static String getTitle(HtmlPage page) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		DomNodeList<DomElement> elements = page.getElementsByTagName("h1");
		for (DomElement element : elements) {
			if (element.getAttribute("class").contains("title")) {
				return element.getTextContent();
			}
		}
		return page.getTitleText();
	}

	static String getDescription(HtmlPage page)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		DomNodeList<DomElement> elements = page.getElementsByTagName("p");
		for (DomElement element : elements) {
			if (element.getAttribute("class").contains("video-see-more")) {
				return element.getTextContent();
			}
		}
		return "";
	}

	static File downloadFile(String url, String destinationPath, boolean forceDownload)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException {

		File destination = new File(destinationPath); // To store the file at a certain destination for temporary usage
		if (forceDownload || !destination.exists()) {
			System.out.println("Downloading video from url : " + url);
			System.out.println("to file : " + destination.getAbsolutePath());
			FileUtils.copyURLToFile(new URL(url), destination);
		}
		return destination;
	}

	static double getDuration(File videoFile)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException {

		IsoFile isoFile = new IsoFile(videoFile.getAbsolutePath());

		double lengthInSeconds = (double) isoFile.getMovieBox().getMovieHeaderBox().getDuration()
				/ isoFile.getMovieBox().getMovieHeaderBox().getTimescale();

		try {
			isoFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return lengthInSeconds;

	}
	
	public static void updateMissingEpisodes(String hostname, String seasonTag, String searchQueryUrl, int start,
			int stop, final List<Episode> episodes, boolean forceDownload) {
		Episode episode = null;
		for (int i = start; i <= stop; i++) {
			System.out.println("--------------------------------------------------------");
			String queryUrl = hostname + searchQueryUrl + i;
			System.out.println("queryUrl: " + queryUrl);
			try {
				if (i <= episodes.size()) {
					episode = episodes.get(i - 1);
					if (downloadEnabled || durationEnabled) {
						episode.downloadVideo(FileUtils.getUserDirectoryPath() + File.separator + "Videos" + novelaPath, "mpg", forceDownload);
						if (!downloadEnabled) {
							episode.deleteVideo();
						}
					}
				} else {
					episode = null;
				}

				if (episode == null || episode.getVideoUrl().isEmpty() || episode.getVideoUrl().startsWith("//")) {
					episode = getEpisode(i, hostname, seasonTag, queryUrl, forceDownload);
					System.out.println("URL: " + episode.getEpisodeUrl());
					if (i <= episodes.size()) {
						episodes.set(i - 1, episode);
					} else {
						episodes.add(i - 1, episode);
					}
				}
			} catch (IOException e) {
				try {
					episode.deleteVideo();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("Skip episode " + i);
				continue;
			}
		}

	}

	public static String toHTML(String title, List<Episode> episodes, Template temp) {
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("title", title);
		root.put("total", episodes.size());
		root.put("episodes", episodes);
		for (Episode episode : episodes) {
			root.put("episode" + episode.getEpisodeId(), episode);
		}

		try {
			Writer out = new StringWriter();
			temp.process(root, out);
			return ((StringWriter) out).getBuffer().toString();

		} catch (TemplateNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedTemplateNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

	public static void setDownloadVideos(boolean enabled) {
		downloadEnabled = enabled;
	}

	public static void setDurationVideos(boolean enabled) {
		durationEnabled = enabled;

	}

	public static void setNovelaPath(String path) {
		novelaPath = path.isBlank()?"":File.separator + path;
	}

}
