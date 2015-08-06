package dwo.config;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * @author Noctarius
 */

public class ConfigProperties extends Properties
{
	private static final long serialVersionUID = 1L;

	private static Logger _log = LogManager.getLogger(ConfigProperties.class);

	public ConfigProperties()
	{
	}

	public ConfigProperties(String name) throws IOException
	{
		load(new FileInputStream(name));
	}

	public ConfigProperties(File file) throws IOException
	{
		load(new FileInputStream(file));
	}

	public ConfigProperties(InputStream inStream) throws IOException
	{
		load(inStream);
	}

	public ConfigProperties(Reader reader) throws IOException
	{
		load(reader);
	}

	public void load(String name) throws IOException
	{
		load(new FileInputStream(name));
	}

	public void load(File file) throws IOException
	{
		load(new FileInputStream(file));
	}

	@Override
	public synchronized void load(Reader reader) throws IOException
	{
		try
		{
			super.load(reader);
		}
		finally
		{
			reader.close();
		}
	}

	@Override
	public synchronized void load(InputStream inStream) throws IOException
	{
		try(InputStreamReader reader = new InputStreamReader(inStream, Charset.defaultCharset()))
		{
			super.load(reader);
		}
		finally
		{
			inStream.close();
		}
	}

	@Override
	public String getProperty(String key)
	{
		String property = super.getProperty(key);

		if(property == null)
		{
			_log.log(Level.WARN, "ConfigProperties: Missing property for key - " + key);

			return null;
		}

		return property.trim();
	}

	@Override
	public String getProperty(String key, String defaultValue)
	{
		String property = super.getProperty(key, defaultValue);

		if(property == null)
		{
			_log.log(Level.WARN, "ConfigProperties: Missing defaultValue for key - " + key);
			return null;
		}
		return property.trim();
	}
}
