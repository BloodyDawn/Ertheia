package dwo.gameserver.model.world.communitybbs.Manager;

import dwo.config.FilePath;
import dwo.config.mods.ConfigCommunityBoardPVP;
import dwo.gameserver.util.fileio.readers.XMLParser;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class RssBBSManager
{
	public static Logger _log = LogManager.getLogger(RssBBSManager.class);
	private static List<String> quotes = new ArrayList<>();

	public static String getPage(String url_server, String url_document)
	{
		StringBuilder buf = new StringBuilder();
		Socket s;
		try
		{
			try
			{
				s = new Socket(url_server, 80);
			}
			catch(Exception e)
			{
				return null;
			}

			s.setSoTimeout(30000);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "Cp1251"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"));

			out.print("GET http://" + url_server + '/' + url_document + " HTTP/1.1\r\n" + //
				"User-Agent: L2GOD Server\r\n" + //
				"Host: " + url_server + "\r\n" + //
				"Accept: */*\r\n" + //
				"Connection: close\r\n" + //
				"\r\n");
			out.flush();

			boolean header = true;
			for(String line = in.readLine(); line != null; line = in.readLine())
			{
				if(header && line.startsWith("<?xml "))
				{
					header = false;
				}
				if(!header)
				{
					buf.append(line).append("\r\n");
				}
				if(!header && line.startsWith("</rss>"))
				{
					break;
				}
			}
			s.close();
		}
		catch(IOException e)
		{
			_log.log(Level.ERROR, "Community RSS: Failed with getting RSS.", e);
		}
		return buf.toString();
	}

	private static void writeFile(File file, String string)
	{
		if(string == null || string.isEmpty())
		{
			_log.log(Level.ERROR, "Community RSS: Empty data from RSS Server.");
			return;
		}
		try
		{
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(string.getBytes("UTF-8"));
			fos.close();
		}
		catch(IOException e)
		{
			_log.log(Level.ERROR, "Community RSS: Failed with writing cache.");
		}
	}

	public static RssBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public String showQuote(String param, String html)
	{
		int page = 1;
		int totalPages = quotes.size();

		try
		{
			page = Integer.parseInt(param);
		}
		catch(NumberFormatException e)
		{
			//WrongPage
			return null;
		}

		if(page > totalPages && page == 1)
		{
			//NotPage
			return null;
		}

		if(page > totalPages || page < 1)
		{
			//WrongPage
			return null;
		}

		html = html.replaceFirst("%quote%", quotes.get(page - 1));
		html = html.replaceFirst("%page%", String.valueOf(page));
		html = html.replaceFirst("%total_pages%", String.valueOf(totalPages));
		html = html.replaceFirst("%back_page%", String.valueOf(page - 1));
		html = html.replaceFirst("%next_page%", String.valueOf(page + 1));

		return html;
	}

	public void loadData()
	{
		String data;
		try
		{
			data = getPage(ConfigCommunityBoardPVP.COMMUNITY_BOARD_RSS_SERVER, ConfigCommunityBoardPVP.COMMUNITY_BOARD_RSS_DOCUMENT);
		}
		catch(Exception E)
		{
			data = null;
		}
		if(data == null)
		{
			_log.log(Level.ERROR, "Community RSS: RSS data download failed.");
			return;
		}
		data = data.replaceFirst("windows-1251", "utf-8");

		writeFile(FilePath.RSS_CACHE, data);

		quotes.clear();

		new Parser(FilePath.RSS_CACHE);

		if(quotes.isEmpty())
		{
			_log.log(Level.WARN, "Community RSS: RSS data parse error.");
			return;
		}

		_log.log(Level.INFO, "Community RSS: Loaded " + quotes.size() + " news.");
	}

	private static class SingletonHolder
	{
		protected static final RssBBSManager _instance = new RssBBSManager();
	}

	private class Parser extends XMLParser
	{
		public Parser(File file)
		{
			super(file);
		}

		@Override
		public void parseDoc(Document doc)
		{
			for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if("rss".equalsIgnoreCase(n.getNodeName()))
				{
					for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if("channel".equalsIgnoreCase(d.getNodeName()))
						{
							for(Node i = d.getFirstChild(); i != null; i = i.getNextSibling())
							{
								if("item".equalsIgnoreCase(i.getNodeName()))
								{
									for(Node z = i.getFirstChild(); z != null; z = z.getNextSibling())
									{
										if("description".equalsIgnoreCase(z.getNodeName()))
										{
											quotes.add(z.getTextContent().replaceAll("\\\\", "").replaceAll("\\$", ""));
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}