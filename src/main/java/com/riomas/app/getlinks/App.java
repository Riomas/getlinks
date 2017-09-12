package com.riomas.app.getlinks;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
	static final String HOSTNAME="http://sic.sapo.pt";
	static final String SEARCH_QUERY_URL = "/pesquisa?q=amor+maior+Episodio+";
	static final String CONTEXT_URL = "/Programas/amormaior/episodios";
    public static void main( String[] args )
    {
    	
    	List<Episode> episodes = GetLinksUtil.getAllEpisodes(HOSTNAME, SEARCH_QUERY_URL, CONTEXT_URL);
    	
    	String buffer = GetLinksUtil.toHTML(episodes);
        
        try {
			FileWriter fw = new FileWriter("C:\\Dev\\AmorMaior.html");
			
			fw.append(buffer);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        
    }
}
