package com.riomas.app.getlinks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class GetLinksUtil {

	public static final String USER_AGENT_EDGE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 Edge/15.15063";
	public static final String USER_AGENT_CHROME = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36";

	static final CloseableHttpClient client = HttpClientBuilder.create().build();
	static final WebClient webClient = new WebClient(BrowserVersion.CHROME);

	public static List<Episode> getAllEpisodes(String hostname, String searchQueryUrl, String contextUrl) {

		List<Episode> episodes = new ArrayList<Episode>();

		for (int i = 1; i <= 320; i++) {
			System.out.println("--------------------------------------------------------");
			String queryUrl = hostname + searchQueryUrl + i;
			System.out.println("queryUrl: " + queryUrl);
			try {
				Episode episode = getEpisode(i, queryUrl);
				System.out.println("URL: " + episode.getEpisodeUrl());
				episodes.add(episode);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
		return episodes;
	}

	private static Episode getEpisode(int id, String queryUrl) throws IOException {
		String content = getHttpClientAsString(queryUrl);
		String tagEpisode = "--Episodio-" + id + "--";
		int endIndex = content.indexOf(tagEpisode);
		if (endIndex == -1) {
			throw new IOException(content + "\nNot found : '" + tagEpisode + "'");
		}
		int beginIndex = content.substring(0, endIndex).lastIndexOf("=\"");
		endIndex = content.indexOf("\"", endIndex);
		return new Episode(id, content.substring(beginIndex + 2, endIndex));
	}

	static String getVideoHtml(String queryUrl)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		HtmlPage page = webClient.getPage(queryUrl);
		while (!page.getReadyState().equals(DomNode.READY_STATE_COMPLETE)) {
			try {
				System.out.println("Loading page, waiting...");
				Thread.currentThread().wait(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		DomNode video = page.getElementsByTagName("video").get(0).getParentNode();
		return video.asXml();
	}

	static HtmlPage getPage(String queryUrl) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		HtmlPage page = webClient.getPage(queryUrl);
		while (!page.getReadyState().equals(DomNode.READY_STATE_COMPLETE)) {
			try {
				System.out.println("Loading page, waiting...");
				Thread.currentThread().wait(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return page;
	}

	static String getVideoUrl(HtmlPage page) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		DomElement element = page.getElementsByTagName("source").get(0);
		return element.getAttribute("src");
	}

	static String getImageUrl(HtmlPage page) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		DomElement element = page.getElementsByTagName("video").get(0);
		return element.getAttribute("poster");
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

	static String getHttpClientAsString(String queryUrl) throws IOException {

		HttpGet request = new HttpGet(queryUrl);

		// add request header
		request.addHeader("User-Agent", USER_AGENT_EDGE);
		HttpResponse response = client.execute(request);

		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		if (response.getStatusLine().getStatusCode() != 200) {
			return "";
		}
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		rd.close();

		return result.toString();
	}

	static String getWebClientAsString(String queryUrl)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		HtmlPage page = webClient.getPage(queryUrl);
		while (!page.getReadyState().equals(DomNode.READY_STATE_COMPLETE)) {
			try {
				System.out.println("Loading page, waiting...");
				Thread.currentThread().wait(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return page.asXml();
	}

	public static String toHTML(List<Episode> episodes) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<!DOCTYPE html>\n<html>\n");
		buffer.append("<head>\n<meta charset=\"UTF-8\"/>\n");
		buffer.append(
				"<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css\" integrity=\"sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M\" crossorigin=\"anonymous\">");
		buffer.append("<link rel=\"stylesheet\" href=\"http://getbootstrap.com/docs/4.0/examples/album/album.css\" >");
		buffer.append("</head>\n");
		buffer.append("<body>\n");
		buffer.append("<div class=\"album\">\n");
		buffer.append("<div class=\"container\">\n");
		buffer.append("<div class=\"row\">\n");
		for (Episode ep : episodes) {
			buffer.append("<div class=\"card\" id=\"episode");
			buffer.append(ep.getEpisodeId());
			buffer.append("\">\n");
			buffer.append("<video preload=\"none\" style=\"width: 100%; height: 256px; display: block;\" controls ");
			buffer.append("poster=\"");
			buffer.append(ep.getImageUrl());
			buffer.append("\">\n");
			buffer.append("<source src=\"");
			buffer.append(ep.getVideoUrl());
			buffer.append("\" type=\"video/mp4\" />\n");
			buffer.append("</video>\n");
			buffer.append("<p class=\"card-text\">\n");
			buffer.append("<h1>\n");
			buffer.append("<a target=\"_blank\" href=\"");
			buffer.append(ep.getEpisodeUrl());
			buffer.append("\">");
			buffer.append(ep.getTitle());
			buffer.append("</a>\n</h1>\n");
			if (!ep.getDescription().isEmpty()) {
				buffer.append("<span>\n");
				buffer.append(ep.getDescription());
				buffer.append("</span><br/>\n");
			}
			buffer.append("</p>\n</div>\n");
		}
		buffer.append("</div>\n</div>\n</div>\n</body>\n</html>");
		return buffer.toString();
	}

}
