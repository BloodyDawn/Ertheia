package dwo.gameserver.cache;

import dwo.config.Config;
import dwo.config.main.ConfigLocalization;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.crypt.datapack.CryptUtil;
import dwo.gameserver.util.fileio.filters.HtmFilter;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Layane
 */

public class HtmCache
{
	private static final String HTML_CACHE = "data/cache/html.cache";
	private static final String _htmlDir = "data/html/";
	private static Logger _log = LogManager.getLogger(HtmCache.class);
	private final HtmFilter htmFilter = new HtmFilter();
	private Map<Integer, String> _cache;
	private long _bytesBuffLen;

	private HtmCache()
	{
		reload();
	}

	public static HtmCache getInstance()
	{
		return SingletonHolder._instance;
	}

	public void reload()
	{
		_cache = new HashMap<>();
		_bytesBuffLen = 0;
		reload(new File(Config.DATAPACK_ROOT.toString() + '/' + _htmlDir));
	}

	public void reload(File f)
	{
		_log.log(Level.INFO, "Html cache start...");
		boolean loaded = false;
		if(Config.USE_HTML_CACHE)
		{
			ObjectInputStream ois = null;
			try
			{
				ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(f, HTML_CACHE)), 1024 << 10));
				_cache = (HashMap<Integer, String>) ois.readObject();
				loaded = true;
			}
			catch(IOException | ClassNotFoundException ignored)
			{
			}
			finally
			{
				try
				{
					if(ois != null)
					{
						ois.close();
					}
				}
				catch(IOException ignored)
				{
				}
			}
		}

		if(!loaded)
		{
			parseDir(f);
		}

		_log.log(Level.INFO, "Cache[HTML]: " + _cache.size() + " files loaded");
	}

	public void reloadPath(File f)
	{
		parseDir(f);
		_log.log(Level.INFO, "Cache[HTML]: Reloaded specified path.");
	}

	private void parseDir(File dir)
	{
		File[] files = dir.listFiles(htmFilter);

		for(File file : files)
		{
			if(file.isDirectory())
			{
				parseDir(file);
			}
			else
			{
				loadFile(file);
			}
		}
	}

	public String loadFile(File file)
	{
		String relpath = Util.getRelativePath(Config.DATAPACK_ROOT, file);
		String hashcode = relpath.toLowerCase();

		// Замена папки ( для диалогов у которых нету сервероного имени )
		hashcode = hashcode.replace("default_ex", "default");

		int hesh = hashcode.hashCode();

		if(file.exists() && !file.isDirectory())
		{
			String content;
			InputStream fis = null;
			try
			{
				fis = CryptUtil.decryptOnDemand(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				if(file.getName().endsWith(".pack"))
				{
					ObjectInputStream object = null;
					try
					{
						object = new ObjectInputStream(bis);
						_cache.putAll((HashMap<Integer, String>) object.readObject());
					}
					catch(IOException e)
					{
						_log.log(Level.ERROR, "Problem with htm file " + e.getMessage(), e);
					}
					return null;
				}

				int bytes = bis.available();
				byte[] raw = new byte[bytes];

				bis.read(raw);
				content = new String(raw, "UTF-8");
				content = content.replaceAll("\r\n", "").replaceAll("\n", "");

				String oldContent = _cache.get(hesh);

				if(oldContent == null)
				{
					_bytesBuffLen += bytes;
				}
				else
				{
					_bytesBuffLen = _bytesBuffLen - oldContent.length() + bytes;
				}

				_cache.put(hesh, content);

				return content;
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Problem with htm file " + e.getMessage(), e);
			}
			finally
			{
				try
				{
					fis.close();
				}
				catch(Exception e1)
				{
					_log.log(Level.ERROR, "Problem with closing file input stream!", e1);
				}
			}
		}

		return null;
	}

	/**
	 * @param prefix префикс
	 * @param path путь до HTM
	 * @return текстовое содержание HTM
	 */
	public String getHtm(String prefix, String path)
	{
		return getHtmLocal("/", prefix, path);
	}

	public String getHtmQuest(String prefix, String path)
	{
		return getHtmLocal("/scripts/", prefix, path);
	}

	private String getHtmLocal(String dir, String prefix, String path)
	{
		String newPath = null;
		String content = null;
		if(prefix != null && !prefix.isEmpty())
		{
			newPath = _htmlDir + prefix + dir + path;
			content = getHtm(newPath);
			if(content != null)
			{
				return content;
			}
		}

		// Ищем диалог в других языках
		for(String lang : ConfigLocalization.MULTILANG_ALLOWED)
		{
			content = getHtm(_htmlDir + lang + dir + path);
			if(content != null && newPath != null)
			{
				_cache.put(newPath.toLowerCase().hashCode(), content);
			}
		}

		return content;
	}

	public String getNoFoundHtml(String path)
	{
		_log.error("NoFoundHtmlDialog: " + path);
		return "<html><body><br>Ссылка неисправна, сообщите администратору.<br>" + path + "</body></html>";
	}

	/**
	 * @param path путь до HTM
	 * @return текстовое содержание HTM
	 */
	private String getHtm(String path)
	{
		if(path == null || path.isEmpty())
		{
			return "";
		}

		String content = getCache(path.toLowerCase());
		if(content == null && (path.endsWith("html") || path.endsWith("htm")))
		{
			content = loadFile(new File(Config.DATAPACK_ROOT, path));
		}

		return content;
	}

	// Служит для проверки наличия html
	public boolean containsHtml(String path)
	{
		for(String lang : ConfigLocalization.MULTILANG_ALLOWED)
		{
			if(getCache(_htmlDir + lang + '/' + path.toLowerCase()) != null)
			{
				return true;
			}
		}
		return false;
	}

	public int getLoadedFiles()
	{
		return _cache.size();
	}

	private String getCache(String path)
	{
		if(path.contains("#"))
		{
			path = path.split("#")[0];
		}

		return _cache.get(path.hashCode());
	}

	private static class SingletonHolder
	{
		protected static final HtmCache _instance = new HtmCache();
	}
}