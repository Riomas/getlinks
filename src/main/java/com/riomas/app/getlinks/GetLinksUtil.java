package com.riomas.app.getlinks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import org.apache.commons.io.FileUtils;

import com.coremedia.iso.IsoFile;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
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

	public static final DateFormat literalMinutesFormat = new SimpleDateFormat("m'm 's's'");
	public static final DateFormat literalHoursMinutesFormat = new SimpleDateFormat("H'h 'm'm 's's'");
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

	public static List<Episode> getAllEpisodesFromApi(final String hostname, String url, int start,
													   int stop, boolean forceDownload) {
		HtmlPage page = getPage(hostname + url);
		System.out.println("Parse page url:  " + page.getUrl());

		List<DomElement> allFigures = new LinkedList();
		DomNodeList<DomElement> figures = page.getElementsByTagName("figure");
		figures.stream().forEach(domElement -> allFigures.add(domElement));
		DomNodeList<DomElement> allH1Tags = page.getElementsByTagName("h1");
		List<String> titles = allH1Tags.stream()
				.filter(domElement -> domElement.getFirstElementChild().getAttribute("itemprop").equalsIgnoreCase("url"))
				.map(domElement -> "Episódio"+getEpisodeId(domElement.getTextContent(), 0))
				.collect(Collectors.toList());

		boolean hasMoreEpisodes = !titles.contains("Episódio1");

		while (hasMoreEpisodes) {
			// Read page tags
			String lastDate = getLastDate(page, "p", "input");

			page = getPage(hostname + url+"?offset="+lastDate);
			if (page==null) {
				break;
			}
			System.out.println("Parse page url:  " + page.getUrl());

			figures = page.getElementsByTagName("figure");
			if (figures==null) {
				break;
			}
			figures.stream().forEach(domElement -> allFigures.add(domElement));

			allH1Tags = page.getElementsByTagName("h1");
			titles = allH1Tags.stream()
					.filter(domElement -> domElement.getFirstElementChild().getAttribute("itemprop").equalsIgnoreCase("url"))
					.map(domElement -> "Episódio"+getEpisodeId(domElement.getTextContent(), 0))
					.collect(Collectors.toList());
			hasMoreEpisodes = !titles.contains("Episódio1");

		}

		Collections.reverse(allFigures);

		List<DomElement> anchors = allFigures.stream().map(DomElement::getFirstElementChild).collect(Collectors.toList());

		ArrayList episodes = new ArrayList();

		Episode episode = null;
		int count=1;

		for (DomElement anchor: anchors) {
			if (start > count) {
				count++;
				continue;
			}
			if (stop == count) {
				break;
			}

			String pageUrl = hostname + anchor.getAttribute("href");
			System.out.println("--------------------------------------------------------");
			System.out.println("pageUrl: " + pageUrl);
			try {
				episode = getEpisode(count, pageUrl, forceDownload);
				System.out.println("URL: " + episode.getEpisodeUrl());

			} catch (IOException e) {
				try {
					episode.deleteVideo();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("Skip episode " + count);
				continue;
			}
			episodes.add(episode);
			count++;
		}
		return episodes;
	}

	private static String getLastDate(HtmlPage page, String... tags) {

		for (String tag: tags) {
			DomNodeList<DomElement> allTags = page.getElementsByTagName(tag);

			// filter tags
			List<String> dateTimes = allTags.stream()
					.filter(domElement -> domElement.getAttribute("class").contains("publishedDate")
							|| domElement.getAttribute("itemprop").contains("datePublished"))
					.map(domElement -> domElement.getAttribute("datetime"))
					.collect(Collectors.toList());
			if (dateTimes.size() > 0) {
				return dateTimes.get(dateTimes.size() - 1);
			}
		}
		return null;
	}

	public static List<Episode> getAllEpisodesFromHtml(final String hostname, String inputFilename, int start,
											   int stop, boolean forceDownload) {

		File inputFile = FileUtils.getFile(inputFilename);
		assert inputFile.exists();

		ArrayList episodes = new ArrayList();

		try {
			String rawEpisodes = FileUtils.readFileToString(inputFile, Charset.forName("UTF8"));

			String[] listHtmlEpisodes = rawEpisodes.substring(rawEpisodes.indexOf("<li "), rawEpisodes.lastIndexOf("</li>")).split("<li ");

			Episode episode = null;
			int count=1;

		for (String htmlEpisode: listHtmlEpisodes) {
			if (start > count) {
				count++;
				continue;
			}
			if (stop == count) {
				break;
			}
			if (htmlEpisode.isEmpty()) {
				continue;
			}

			int figurePos = htmlEpisode.indexOf("<figure>");
			if (figurePos < 0) {
				continue;
			}
			int hrefPos = htmlEpisode.indexOf("href=", figurePos);
			int beginLinkPos = htmlEpisode.indexOf("\"", hrefPos)+1;
			int endlinkPos = htmlEpisode.indexOf("\"", beginLinkPos);

			String pageUrl = hostname + htmlEpisode.substring(beginLinkPos, endlinkPos);
			System.out.println("--------------------------------------------------------");
			System.out.println("pageUrl: " + pageUrl);
			try {
				episode = getEpisode(count, pageUrl, forceDownload);
				System.out.println("URL: " + episode.getEpisodeUrl());

			} catch (IOException e) {
				try {
					episode.deleteVideo();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("Skip episode " + count);
				continue;
			}
			episodes.add(episode);
			count++;
		}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return episodes;
	}

	private static Episode getEpisode(int id, String hostname, String seasonTag, String queryUrl, boolean forceDownload)
			throws IOException {
		String content = getPage(queryUrl).getBody().asXml();
		String tagEpisode = seasonTag + "---Episodio-" + id + "-";
		int endIndex = content.indexOf(tagEpisode);
		String episodeUrl = null;
		if (endIndex == -1) {
			episodeUrl = promptConsole("Saisissez l'url de l'épisode "+id, 20);
			if (episodeUrl.isEmpty()) {
				Episode ep = new Episode(id);
				ep.setSearchUrl(queryUrl);
				return ep;
			}
		} else {
			int beginIndex = content.substring(0, endIndex).lastIndexOf("=\"");
			endIndex = content.indexOf("\"", endIndex);
			episodeUrl = hostname + content.substring(beginIndex + 2, endIndex);
		}
		
		Episode episode = new Episode(id, episodeUrl);
		if (downloadEnabled || durationEnabled) {
			episode.downloadVideo(FileUtils.getUserDirectoryPath() + File.separator + "Videos" + novelaPath, "mpg",
					forceDownload);
			if (!downloadEnabled) {
				episode.deleteVideo();
			}
		}
		return episode;
	}

	private static Episode getEpisode(int id, String pageUrl, boolean forceDownload)
			throws IOException {

		String episodeUrl = "";
		try {
			episodeUrl = getVideoUrl(getPage(pageUrl));
		} catch (TagNameNotFoundException e) {
			System.out.println("No videoUrl");
		}
		if (episodeUrl.isEmpty()) {
			Episode ep = new Episode(id);
			ep.setSearchUrl(pageUrl);
			return ep;
		}

		Episode episode = new Episode(id, pageUrl);
		if (downloadEnabled || durationEnabled) {
			episode.downloadVideo(FileUtils.getUserDirectoryPath() + File.separator + "Videos" + novelaPath, "mpg",
					forceDownload);
			if (!downloadEnabled) {
				episode.deleteVideo();
			}
		}
		return episode;
	}

	static String getVideoHtml(String queryUrl, int id)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException, TagNameNotFoundException {
		HtmlPage page = getPage(queryUrl);
		DomNodeList<DomElement> elements = page.getElementsByTagName("video");
		if (elements.size() > 0) {
			return elements.get(0).getParentNode().asXml();
		}
		throw new TagNameNotFoundException(
				"Could not found tagName: 'source' in this page: '" + page.getTitleText() + "'");
	}

	static HtmlPage getPage(String queryUrl)
			throws FailingHttpStatusCodeException {
		WebClient webClient = gethtmlUnitClient();
		// webClient.waitForBackgroundJavaScript(60000);

		try {
			HtmlPage page = null;
			try {
//				webClient.waitForBackgroundJavaScript(20000);
				page = webClient.getPage(queryUrl);
//				int cpt = 0;

//				HtmlElement body = page.getBody();
//				while (cpt < 20 && !body.asXml().contains("<video")) {
//					System.out.println((cpt++) + " waiting js jobs ");
//					try {Thread.sleep(1000);} catch (InterruptedException e) {}
//					body = page.getBody();
//				}
			} catch (Exception e) {
				System.out.println("Get page error: " + e.getMessage());
			}

			return page;
		} finally {
			webClient.close();
		}

	}

	static String promptConsole(String message, int timeoutInSeconds) {

		String inputData = "";
		
		timeoutInSeconds *= 1000;

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		long startTime = System.currentTimeMillis();
		try {
			System.out.print(message+": ");
			while ((System.currentTimeMillis() - startTime) < timeoutInSeconds && !in.ready()) {
			}

			if (in.ready()) {
				inputData = in.readLine();
				System.out.println("Votre saisie: " + inputData);
			} else {
				System.out.println("Aucune saisie");
			}
		} catch (IOException e) {
		}
		return inputData;
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
		webClient.getOptions().setTimeout(5000);
		return webClient;

	}

	static String getVideoUrl(HtmlPage page)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException, TagNameNotFoundException {
		DomNodeList<HtmlElement> videoElements = page
				.getBody()
				.getElementsByTagName("video");
		String videoUrl = videoElements
				.stream()
				.findFirst()
				.map(htmlElement -> {
					String src = htmlElement.getElementsByTagName("source")
							.stream()
							.findFirst()
							.map(htmlElement1 -> htmlElement1.getAttribute("src")).orElse("");
					return src.startsWith("//")?"https:"+src:src;
				})
				.orElse("");
		if (!videoUrl.isEmpty()) {
			return videoUrl;
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

	static String getTitle(HtmlPage page) throws FailingHttpStatusCodeException {
		DomNodeList<DomElement> elements = page.getElementsByTagName("h1");
		for (DomElement element : elements) {
			if (element.getAttribute("class").contains("title")) {
				return element.getTextContent();
			}
		}
		return page.getTitleText();
	}

	static int getEpisodeId(String title, int defaultValue) {
		StringTokenizer tokenizer = new StringTokenizer(title, " - ");
		while (tokenizer.hasMoreTokens()) {
			String element = tokenizer.nextToken();
			if (element.contains("Episódio")) {
				return Integer.parseInt(tokenizer.nextToken());
			}
		}
		return defaultValue;
	}

	static String getDescription(HtmlPage page)
			throws FailingHttpStatusCodeException {
		DomNodeList<DomElement> elements = page.getElementsByTagName("p");
		for (DomElement element : elements) {
			if (element.getAttribute("class").contains("video-see-more")) {
				return element.getTextContent();
			}
		}
		elements = page.getElementsByTagName("h2");
		for (DomElement element : elements) {
			if (element.getAttribute("class").contains("lead")) {
				return element.getTextContent();
			}
		}
		return "";
	}

	static File downloadFile(String url, String destinationPath, boolean forceDownload)
			throws FailingHttpStatusCodeException, IOException {

		File destination = new File(destinationPath); // To store the file at a certain destination for temporary usage
		if (forceDownload || !destination.exists()) {
			System.out.println("Downloading video from url : " + url);
			System.out.println("to file : " + destination.getAbsolutePath());
			FileUtils.copyURLToFile(new URL(url), destination);
		}
		return destination;
	}

	static double getDuration(File videoFile)
			throws FailingHttpStatusCodeException, IOException {

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
					if ((downloadEnabled || durationEnabled) && !episode.getVideoUrl().isEmpty()) {
						episode.downloadVideo(FileUtils.getUserDirectoryPath() + File.separator + "Videos" + novelaPath,
								"mpg", forceDownload);

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

	public static void updateMissingEpisodesFromApi(String hostname, String url, int start,
													int stop, final List<Episode> episodes, boolean forceDownload) {
		List<Episode> allEpisodesFromApi = getAllEpisodesFromApi(hostname, url, start, stop, false);
		int count = 1;
		Episode episode = null;
		for (Episode episodeFromApi : allEpisodesFromApi) {
			if (count < start && start > 0) {
				count++;
			}
			if (count > stop && stop > 0) {
				break;
			}

			System.out.println("--------------------------------------------------------");


			try {
				if (count <= episodes.size()) {
					episode = episodes.get(count - 1);
					if ((downloadEnabled || durationEnabled) && !episode.getVideoUrl().isEmpty()) {
						episode.downloadVideo(FileUtils.getUserDirectoryPath() + File.separator + "Videos" + novelaPath,
								"mpg", forceDownload);

						if (!downloadEnabled) {
							episode.deleteVideo();
						}
					}
				} else {
					episode = null;
				}

				if (episode == null || episode.getVideoUrl().isEmpty() || episode.getVideoUrl().startsWith("//")) {
					int finalI = count;
					episode = allEpisodesFromApi
							.stream()
							.filter(episode1 -> episode1.getEpisodeId() == finalI)
							.collect(Collectors.toList()).stream().findFirst().orElse(null)
							;
					if (episode!=null) {
						System.out.println("URL: " + episode.getEpisodeUrl());
						if (count <= episodes.size()) {
							episodes.set(count - 1, episode);
						} else {
							episodes.add(count - 1, episode);
						}
					}
				}
				count++;
			} catch (IOException e) {
				try {
					episode.deleteVideo();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("Skip episode " + count);
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
		novelaPath = path.isEmpty() ? "" : File.separator + path;
	}

}
