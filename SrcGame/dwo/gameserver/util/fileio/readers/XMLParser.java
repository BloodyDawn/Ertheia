package dwo.gameserver.util.fileio.readers;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * @author UnAfraid, ANZO
 */

public abstract class XMLParser
{
	private static final Logger _log = LogManager.getLogger(XMLParser.class);

	private final boolean _validate;
	private final boolean _ignoreComments;
	private final File _file;

	protected XMLParser(File f)
	{
		_validate = false;
		_ignoreComments = false;
		_file = f;
		doParse();
	}

	public boolean isIgnoringComments()
	{
		return _ignoreComments;
	}

	public boolean isValidating()
	{
		return _validate;
	}

	public void doParse()
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(_validate);
		factory.setIgnoringComments(_ignoreComments);
		Document doc = null;

		if(_file.exists())
		{
			try
			{
				doc = factory.newDocumentBuilder().parse(_file);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Could not parse " + _file.getName() + " file: " + e.getMessage(), e);
				return;
			}

			try
			{
				parseDoc(doc);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error while parsing doc: " + e.getMessage(), e);
			}

		}
		else
		{
			_log.log(Level.ERROR, "Could not found " + _file.getName() + " file!");
		}
	}

	public File getXML()
	{
		return _file;
	}

	public abstract void parseDoc(Document doc);
}
