package com.riomas.app.getlinks;

import com.coremedia.iso.IsoFile;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class GetLinksUtil {


	private static boolean downloadEnabled = false;
	private static boolean durationEnabled = false;
	private static String novelaPath = "";

	public static final String DEFAULT_VIDEO_HOSTNAME = "https://videos.impresa.pt";
	public static final String DEFAULT_IMAGE_HOSTNAME = "//images.impresa.pt";

	private GetLinksUtil() {
	}

	public static List<Episode> getAllEpisodesFromApi(final String hostname, String url, int start,
													   int stop, boolean forceDownload) {
		HtmlPage page = getPage(hostname + url)
				.orElseThrow(() -> new NullPointerException("Page not accessible: " + hostname + url));
		System.out.printf("Parse page url:  %s%n", page.getUrl());

		DomNodeList<DomElement> figures = page.getElementsByTagName("figure");
		List<DomElement> allFigures = new LinkedList<>(figures);
		DomNodeList<DomElement> allH1Tags = page.getElementsByTagName("h1");
		List<String> titles = allH1Tags.stream()
				.filter(domElement -> domElement.getFirstElementChild().getAttribute("itemprop").equalsIgnoreCase("url"))
				.map(domElement -> "Episódio"+getEpisodeId(domElement.getTextContent(), 0))
				.collect(Collectors.toList());

		boolean hasMoreEpisodes = !titles.contains("Episódio1");

		while (hasMoreEpisodes) {
			// Read page tags
			String lastDate = getLastDate(page, "p", "input");
			page = getPage(hostname + url+"?offset="+lastDate)
				.orElseThrow(() -> new NullPointerException("Page not accessible: " + hostname + url+"?offset="+lastDate));

			System.out.println("Parse page url:  " + page.getUrl());
			figures = page.getElementsByTagName("figure");
			if (figures != null) {
				allFigures.addAll(figures);
				allH1Tags = page.getElementsByTagName("h1");
				titles = allH1Tags.stream()
						.filter(domElement -> domElement.getFirstElementChild().getAttribute("itemprop").equalsIgnoreCase("url"))
						.map(domElement -> "Episódio" + getEpisodeId(domElement.getTextContent(), 0))
						.collect(Collectors.toList());
			}
			hasMoreEpisodes = !titles.contains("Episódio1");
		}

		Collections.reverse(allFigures);

		List<DomElement> anchors = allFigures.stream().map(DomElement::getFirstElementChild).collect(Collectors.toList());

		ArrayList<Episode> episodes = new ArrayList<>();

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
				episodes.add(episode);
				count++;
			} catch (IOException | NullPointerException e) {
				try {
					assert episode != null;
					episode.deleteVideo();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("Skip episode " + count);
			}

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
			if (!dateTimes.isEmpty()) {
				return dateTimes.get(dateTimes.size() - 1);
			}
		}
		return null;
	}

	private static Episode getEpisode(int id, String pageUrl, boolean forceDownload)
			throws IOException {

		String episodeUrl = "";
		try {
			episodeUrl = getVideoUrl(getPage(pageUrl)
					.orElseThrow(() -> new NullPointerException("Page not accessible: " + pageUrl)));
		} catch (TagNameNotFoundException | AttributeSrcNotFoundException e) {
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

	static Optional<HtmlPage> getPage(String queryUrl) {

		try (WebClient webClient = gethtmlUnitClient()) {
			HtmlPage page = null;
			try {
				page = webClient.getPage(queryUrl);
				while (page.getReadyState().equals("loading")) {
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				System.out.println("Get page error: " + e.getMessage());
			}

			return Optional.ofNullable(page);
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
		} catch (IOException ignored) {
		}
		return inputData;
	}

	public static WebClient gethtmlUnitClient() {
		WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setDownloadImages(false);
		webClient.getOptions().setActiveXNative(false);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setTimeout(5000);
		return webClient;

	}

	static String getVideoUrl(HtmlPage page) throws TagNameNotFoundException, AttributeSrcNotFoundException {

		HtmlElement articleElement = page
				.getBody()
				.getElementsByTagName("article")
				.stream()
				.filter(htmlElement -> htmlElement.getAttribute("class").contains("AT-video"))
				.findFirst()
				.orElseThrow(() -> new TagNameNotFoundException(
						String.format("Could not found tagName: 'article' in this page: '%s'", page.getTitleText())));

		int begin = articleElement.asXml().indexOf(DEFAULT_VIDEO_HOSTNAME);
		if (begin>-1) {
			int end = articleElement.asXml().indexOf('"', begin);
			String videoUrl = articleElement.asXml().substring(begin, end);
			if (!videoUrl.isEmpty()) {
				return videoUrl;
			}
		}
		throw new AttributeSrcNotFoundException(
				"Could not found tagName: 'article' in this page: '" + page.getTitleText() + "'");
	}

	static String getImageUrl(HtmlPage page) throws TagNameNotFoundException, AttributeSrcNotFoundException {
		HtmlElement articleElement = page
				.getBody()
				.getElementsByTagName("article")
				.stream()
				.filter(htmlElement -> htmlElement.getAttribute("class").contains("AT-video"))
				.findFirst()
				.orElseThrow(() -> new TagNameNotFoundException(
						String.format("Could not found tagName: 'article' in this page: '%s'", page.getTitleText())));

		int begin = articleElement.asXml().indexOf(DEFAULT_IMAGE_HOSTNAME);
		if (begin>-1) {
			int end = articleElement.asXml().indexOf('"', begin);
			String imageUrl = articleElement.asXml().substring(begin, end);
			if (!imageUrl.isEmpty()) {
				return imageUrl.startsWith("//")?"https:"+imageUrl:imageUrl;
			}
		}
		throw new AttributeSrcNotFoundException(
				"Could not found tagName: 'article' in this page: '" + page.getTitleText() + "'");
	}

	static String getTitle(HtmlPage page) {
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

	static String getDescription(HtmlPage page) {
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
			throws IOException {

		File destination = new File(destinationPath); // To store the file at a certain destination for temporary usage
		if (forceDownload || !destination.exists()) {
			System.out.println("Downloading video from url : " + url);
			System.out.println("to file : " + destination.getAbsolutePath());
			FileUtils.copyURLToFile(new URL(url), destination);
		}
		return destination;
	}

	static double getDuration(File videoFile)
			throws IOException {

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

	public static void updateMissingEpisodesFromApi(String hostname, String url, int start,
													int stop, final List<Episode> episodes, boolean forceDownload) {
		System.out.println("--------------------------------------------------------");

		List<Episode> allEpisodesFromApi = getAllEpisodesFromApi(hostname, url, start, stop, false);
		System.out.println("Updating all episodios: "+allEpisodesFromApi.size());
		int count = 1;
		Episode episode = null;
		for (Episode episodeFromApi : allEpisodesFromApi) {
			if (count < start && start > 0) {
				count++;
			}
			if (count > stop && stop > 0) {
				break;
			}

			System.out.println("Episodio "+episodeFromApi.getEpisodeId());

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

				if (episode == null || episode.getVideoUrl() ==null || episode.getVideoUrl().isEmpty() || episode.getVideoUrl().startsWith("//")) {
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
			StringWriter out = new StringWriter();
			temp.process(root, out);
			return out.getBuffer().toString();

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
