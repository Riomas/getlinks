package com.riomas.app.getlinks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

/**
 * Hello world!
 *
 */
public class App {
	static final String HOSTNAME = "http://sic.sapo.pt";
	static final String SEARCH_QUERY_URL = "/pesquisa?q=%s+Episodio+";
	//static final String CONTEXT_URL = "/Programas/%s/episodios";

	public static void main(String[] args) {
		CommandLine cmd = getCommandLine(args);
		String novela = cmd.getOptionValue('n');
		String[] keywords = novela.split(" ");
		String searchQuery = String.format(SEARCH_QUERY_URL, StringUtils.join(keywords, '+'));

		String outputFileName = cmd.getOptionValue('o') + File.separatorChar + StringUtils.join(keywords, "")
				+ ".html";

		int start = Integer.valueOf(cmd.getOptionValue('b'));
		int stop = Integer.valueOf(cmd.getOptionValue('e'));
		
		File outputFile = new File(outputFileName);
		
		//String context = String.format(CONTEXT_URL, StringUtils.join(keywords, "")).toLowerCase();

		List<Episode> episodes = GetLinksUtil.getAllEpisodes(HOSTNAME, searchQuery, start, stop);

		String buffer = GetLinksUtil.toHTML(novela, episodes);
		try {
			FileWriter fw = new FileWriter(outputFile);
			fw.append(buffer);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		
		try {
			// parse the command line arguments
			return parser.parse(options, args);
		} catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
		}
		return null;
	}
}
