package com.riomas.app.getlinks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
		String saveFileName = cmd.getOptionValue('o') + File.separatorChar + StringUtils.join(keywords, "")
		+ ".out";

		int start = Integer.valueOf(cmd.getOptionValue('b'));
		int stop = Integer.valueOf(cmd.getOptionValue('e'));
		
		File outputFile = new File(outputFileName);
		File saveFile = new File(saveFileName);
		
		List<Episode> episodes = readFromFile(saveFile);
		
		//String context = String.format(CONTEXT_URL, StringUtils.join(keywords, "")).toLowerCase();

		if (episodes==null) {
			episodes = GetLinksUtil.getAllEpisodes(HOSTNAME, searchQuery, start, stop);
		} else {
			GetLinksUtil.updateMissingEpisodes(HOSTNAME, searchQuery, start, stop, episodes);
		}
		
		System.out.println("Total episodes : " + episodes.size());

		saveToFile(saveFile, episodes);
		
		String buffer = GetLinksUtil.toHTML(novela, episodes);
		
		System.out.println("HTML generated!");

		try {
			FileWriter fw = new FileWriter(outputFile);
			fw.append(buffer);
			fw.flush();
			fw.close();
			System.out.println("Write episodes to '"+outputFile.getAbsolutePath()+"'");
		} catch (IOException e) {
			System.err.println("Unexpected exception:" + e.getMessage());
		}
		System.out.println("End process.");

	}

	@SuppressWarnings("unchecked")
	private static List<Episode> readFromFile(File file) {
		ObjectInputStream oi = null;
		try {
			FileInputStream fi = new FileInputStream(file);
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
