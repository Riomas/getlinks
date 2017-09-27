package com.riomas.app.getlinks;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;

public class GetLinksUtil {

	public static final String USER_AGENT_EDGE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 Edge/15.15063";
	public static final String USER_AGENT_CHROME = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36";

	//static final CloseableHttpClient client = HttpClientBuilder.create().build();
	// static final WebClient webClient = new
	// WebClient(BrowserVersion.BEST_SUPPORTED);

	public static List<Episode> getAllEpisodes(String hostname, String searchQueryUrl, int start, int stop) {

		List<Episode> episodes = new ArrayList<Episode>();

		for (int i = start; i <= stop; i++) {
			System.out.println("--------------------------------------------------------");
			String queryUrl = hostname + searchQueryUrl + i;
			System.out.println("queryUrl: " + queryUrl);
			try {
				Episode episode = getEpisode(i, queryUrl);
				System.out.println("URL: " + episode.getEpisodeUrl());
				episodes.add(episode);
			} catch (IOException e) {
				System.out.println("Skip episode "+i);
				continue;
			}
		}
		return episodes;
	}

	private static Episode getEpisode(int id, String queryUrl) throws IOException {
		// String content = getHttpClientAsString(queryUrl);
		String content = getPage(queryUrl, id).getBody().asXml();
		String tagEpisode = "--Episodio-" + id + "--";
		int endIndex = content.indexOf(tagEpisode);
		if (endIndex == -1) {
			Episode ep = new Episode(id);
			ep.setSearchUrl(queryUrl);
			return ep;
			// throw new IOException(content + "\nNot found : '" + tagEpisode +
			// "'");
		}
		int beginIndex = content.substring(0, endIndex).lastIndexOf("=\"");
		endIndex = content.indexOf("\"", endIndex);
		return new Episode(id, content.substring(beginIndex + 2, endIndex));
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

	static HtmlPage getPage(String queryUrl, int id) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		WebClient webClient = gethtmlUnitClient();
        //webClient.waitForBackgroundJavaScript(60000);

		try {
			HtmlPage page = null;
			try {
				page = webClient.getPage(queryUrl);
				int cpt = 0;
				while(cpt < 20 && ( /* !page.asText().contains("não obteve resultados") ||*/ !page.asText().contains("Episódio "+id+" "))){
					System.out.println("wait ... "+cpt++);
			        webClient.waitForBackgroundJavaScript(1000);
			        //page.wait(100);
			    }
//		        while (webClient.waitForBackgroundJavaScript(60000) > 0) {
//		            synchronized (page) {
//		                page.wait(200);
//		            }
//		        }
			} catch (Exception e) {
				System.out.println("Get page error: " + e.getMessage());
			}
//			JavaScriptJobManager manager = page.getEnclosingWindow().getJobManager();
//			while (manager.getJobCount() > 0) {
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					System.out.println(e.getMessage());
//				}
//			}
			
			// webClient.getPage(queryUrl);
			// while
			// (!page.getReadyState().equals(DomNode.READY_STATE_COMPLETE)) {
			// try {
			// System.out.println("Loading page, waiting...");
			// Thread.currentThread().wait(1000);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
			return page;
		} finally {
			webClient.close();
		}

	}

	static public WebClient gethtmlUnitClient() {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        
        webClient.setIncorrectnessListener(new IncorrectnessListener() {
            public void notify(String arg0, Object arg1) {
            }
        });
        webClient.setCssErrorHandler(new ErrorHandler() {

            public void warning(CSSParseException arg0) throws CSSException {
                // TODO Auto-generated method stub

            }

            public void fatalError(CSSParseException arg0) throws CSSException {
                // TODO Auto-generated method stub

            }

            public void error(CSSParseException arg0) throws CSSException {
                // TODO Auto-generated method stub

            }
        });
        webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {

            public void timeoutError(HtmlPage arg0, long arg1, long arg2) {
                // TODO Auto-generated method stub

            }

            public void scriptException(HtmlPage arg0, ScriptException arg1) {
                // TODO Auto-generated method stub

            }

            public void malformedScriptURL(HtmlPage arg0, String arg1, MalformedURLException arg2) {
                // TODO Auto-generated method stub

            }

            public void loadScriptError(HtmlPage page, java.net.URL scriptUrl, Exception exception) {
				// TODO Auto-generated method stub
				
			}
        });
        webClient.setHTMLParserListener(new HTMLParserListener() {

            public void error(String message, java.net.URL url, String html, int line, int column, String key) {
				// TODO Auto-generated method stub
				
			}

			public void warning(String message, java.net.URL url, String html, int line, int column, String key) {
				// TODO Auto-generated method stub
				
			}
        });
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setDownloadImages(false);

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

//	static String getHttpClientAsString(String queryUrl) throws IOException {
//
//		HttpGet request = new HttpGet(queryUrl);
//
//		// add request header
//		request.addHeader("User-Agent", USER_AGENT_EDGE);
//		HttpResponse response = client.execute(request);
//
//		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
//		if (response.getStatusLine().getStatusCode() != 200) {
//			return "";
//		}
//		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//
//		StringBuffer result = new StringBuffer();
//		String line = "";
//		while ((line = rd.readLine()) != null) {
//			result.append(line);
//		}
//		rd.close();
//
//		return result.toString();
//	}

	@Deprecated
	static String getWebClientAsString(String queryUrl, int id)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		return getPage(queryUrl, id).asXml();
	}

	public static String toHTML(String title, List<Episode> episodes) {
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("<!DOCTYPE html>\n<html>\n");
		buffer.append("<head>\n<meta charset=\"UTF-8\"/>\n");
		buffer.append("<title>");
		buffer.append(title);
		buffer.append("</title>\n");
		buffer.append(
				"<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css\" integrity=\"sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M\" crossorigin=\"anonymous\">");
		buffer.append("<link rel=\"stylesheet\" href=\"http://getbootstrap.com/docs/4.0/examples/album/album.css\" >");
		buffer.append("</head>\n");
		buffer.append("<body>\n");
		
		// navbar
		buffer.append("<div class=\"container\">\n");
		buffer.append("<nav class=\"navbar fixed-top navbar-expand-lg navbar-dark bg-dark\">\n");
		buffer.append("<a class=\"navbar-brand\" href=\"#\">");
		buffer.append(title);
		buffer.append("</a>\n");
		buffer.append("<button class=\"navbar-toggler\" type=\"button\" data-toggle=\"collapse\" data-target=\"#navbarepisodes\" aria-controls=\"navbarepisodes\" aria-expanded=\"false\" aria-label=\"Toggle navigation\">");
		buffer.append("<span class=\"navbar-toggler-icon\"></span>");
		buffer.append("</button>");
		buffer.append("<div class=\"collapse navbar-collapse\" id=\"navbarepisodes\">\n");
		buffer.append("<div class=\"navbar-nav\">\n");
		for (int i=1; i<episodes.size(); i=i+10) {
			//buffer.append("<li class=\"nav-item\">\n");
			buffer.append("<a class=\"nav-item nav-link\" href=\"#episode"+i+"\">"+i+"</a>\n");
			//buffer.append("</li>\n");
		}
		buffer.append("</div>\n");
		buffer.append("</div>\n");
		buffer.append("</nav>\n");
		buffer.append("</div>\n");
		
		// episodes
		buffer.append("<div class=\"album\">\n");
		buffer.append("<div class=\"container\">\n");
		buffer.append("<div class=\"row\">\n");
		for (Episode ep : episodes) {
			buffer.append("<div class=\"card\" id=\"episode");
			buffer.append(ep.getEpisodeId());
			buffer.append("\">\n");
			buffer.append("<a target=\"_blank\" href=\"");
			buffer.append(ep.getVideoUrl());
			buffer.append("\" title=\"");
			buffer.append(ep.getTitle());
			buffer.append("\">");
			buffer.append("<img src=\"");
			if (ep.getImageUrl().startsWith("//")) {
				buffer.append("http:");
			}
			buffer.append(ep.getImageUrl());
			buffer.append("\" style=\"width: 100%; display: block;\"/>");
			buffer.append("</a>\n");
			buffer.append("<p class=\"card-text\">\n");
			buffer.append("<h4>\n");
			buffer.append(ep.getTitle());
			buffer.append("</h4>\n");
			if (!ep.getDescription().isEmpty()) {
				buffer.append("<span>\n");
				buffer.append(ep.getDescription());
				buffer.append("</span><br/>\n");
			}
			buffer.append("<a target=\"_blank\" href=\"");

			if (!ep.getEpisodeUrl().isEmpty()) {
				buffer.append(ep.getEpisodeUrl());
				buffer.append("\">Ver...");
			} else {
				buffer.append(ep.getSearchUrl());
				buffer.append("\">Procurar...");
			}
			buffer.append("</a>\n");
			buffer.append("</p>\n</div>\n");
		}
		buffer.append("</div>\n</div>\n</div>\n</body>\n</html>");
		return buffer.toString();
	}

	public static void updateMissingEpisodes(String hostname, String searchQueryUrl, int start, int stop, final List<Episode> episodes) {
		
		for (int i = start; i <= stop; i++) {
			System.out.println("--------------------------------------------------------");
			String queryUrl = hostname + searchQueryUrl + i;
			System.out.println("queryUrl: " + queryUrl);
			try {
				Episode episode = episodes.get(i-1);
				if (episode==null || episode.getVideoUrl().isEmpty()) {
					episodes.remove(i-1);
					episode = getEpisode(i, queryUrl);
					
					System.out.println("URL: " + episode.getEpisodeUrl());
					episodes.add(i-1, episode);
				}
			} catch (IOException e) {
				System.out.println("Skip episode "+i);
				continue;
			}
		}

	}

}
