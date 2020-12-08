package com.riomas.app.getlinks;

import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Get links Application
 */
public class App {
	static final String DEFAULT_HOSTNAME = "https://sic.pt";
	static final String API_EPISODES_URL = "/api/molecule/category/Programas%s/episodios";

	public static void main(String[] args) {
		CommandLine cmd = getCommandLine(args);
		assert cmd != null;
		String hostname = cmd.getOptionValue("host", DEFAULT_HOSTNAME);
		String urlNovela = cmd.getOptionValue("url");
		String novela = cmd.getOptionValue('n');
		String[] keywords = novela.split(" ");

		String outputFileName = cmd.getOptionValue('o') + File.separatorChar + StringUtils.join(keywords, "")
				+ ".html";
		String saveFileName = cmd.getOptionValue('o') + File.separatorChar + StringUtils.join(keywords, "")
		+ ".out";

		int start = Integer.parseInt(cmd.getOptionValue('b', "0"));
		int stop = Integer.parseInt(cmd.getOptionValue('e', "0"));

		boolean force = cmd.hasOption('f');
		boolean downloadVideos = cmd.hasOption('D');
		boolean durationVideos = cmd.hasOption('d');

		String urlApi;
		if (urlNovela.startsWith("/api/")) {
			urlApi = urlNovela;
		} else {
			urlApi = String.format(API_EPISODES_URL, urlNovela);
		}
		File outputFile = new File(outputFileName);
		File saveFile = new File(saveFileName);
		try {
			FileUtils.forceMkdirParent(saveFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<Episode> episodes = readFromFile(saveFile);

		GetLinksUtil.setNovelaPath(novela);
		GetLinksUtil.setDownloadVideos(downloadVideos);
		GetLinksUtil.setDurationVideos(durationVideos);
		
		if (episodes==null || force) {
			episodes = GetLinksUtil.getAllEpisodesFromApi(hostname, urlApi, start, stop, force);
		} else {
			GetLinksUtil.updateMissingEpisodesFromApi(hostname, urlApi, start, stop, episodes, force);
		}
		
		System.out.println("Total episodes : " + episodes.size());

		saveToFile(saveFile, episodes);

		Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
		// Specify the source where the template files come from. Here I set a
		// plain directory for it, but non-file-system sources are possible too:
		try {
			cfg.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// From here we will set the settings recommended for new projects. These
		// aren't the defaults for backward compatibilty.

		// Set the preferred charset template files are stored in. UTF-8 is
		// a good choice in most applications:
		cfg.setDefaultEncoding("UTF-8");

		// Sets how errors will appear.
		// During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		// Don't log exceptions inside FreeMarker that it will thrown at you anyway:
		cfg.setLogTemplateExceptions(false);

		// Wrap unchecked exceptions thrown during template processing into TemplateException-s:
		cfg.setWrapUncheckedExceptions(true);

		// Do not fall back to higher scopes when reading a null loop variable:
		cfg.setFallbackOnNullLoopVariable(false);
		
		String bufferHTML=null;
		try {
			bufferHTML = GetLinksUtil.toHTML(novela, episodes, cfg.getTemplate("episodes-album.ftlh"));
		} catch (TemplateNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MalformedTemplateNameException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (freemarker.core.ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println("HTML generated!");

		FileWriter fw=null;
		try {
			fw = new FileWriter(outputFile);
			fw.append(bufferHTML);
			fw.flush();
			System.out.println("Write episodes to '"+outputFile.getAbsolutePath()+"'");
		} catch (IOException e) {
			System.err.println("Unexpected exception:" + e.getMessage());
		} finally {
			assert fw!=null;
			try {
				fw.close();
			} catch (IOException e) {
				System.err.println("Unexpected exception:" + e.getMessage());
			}
		}
		System.out.println("End process.");

	}

	@SuppressWarnings("unchecked")
	private static List<Episode> readFromFile(File file) {
		ObjectInputStream oi = null;
		FileInputStream fi = null;
		try {
			fi = new FileInputStream(file);
			oi = new ObjectInputStream(fi);
			// Read objects
			return (List<Episode>) oi.readObject();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fi!=null) {
				try {
					fi.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (oi!=null){
				try {
					oi.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("Load episodes from file: '"+file.getAbsolutePath()+"'");
		}
		return null;
	}

	private static CommandLine getCommandLine(String[] args) {
		// create the command line parser
		CommandLineParser parser = new DefaultParser();

		// create the Options
		Options options = new Options();
		options.addOption("o", "out", true, "output folder.");
		options.addOption("n", "name", true, "Name of Novela.");
		options.addOption("b", "begin", true, "Begin at episode number.");
		options.addOption("e", "end", true, "End at episode number .");
		options.addOption("f", "force", false, "Force update all episodes");
		options.addOption("D", "download", false, "Download videos of episodes");
		options.addOption("d", "duration", false, "Get duration of episodes");
		options.addOption("h", "host", true, "base url official website");
		options.addOption(null, "url", true, "url of the novela");

		try {
			// parse the command line arguments
			return parser.parse(options, args);
		} catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
		}
		return null;
	}
	
	public synchronized static void saveToFile(File file, Object object) {

		FileOutputStream fout = null;
		ObjectOutputStream oos = null;

		try {

			fout = new FileOutputStream(file, false);
			oos = new ObjectOutputStream(fout);
			oos.writeObject(object);

			System.out.println("Save episodes to file: '"+file.getAbsolutePath()+"'");

		} catch (Exception ex) {

			ex.printStackTrace();

		} finally {

			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}


}
