package dwo.gameserver.util.fileio.readers;

import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.util.crypt.datapack.CryptUtil;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XMLReader
{
	private static Logger _log = LogManager.getLogger(XMLReader.class);

	private final File _file;
	private final List<StatsSet> _sets;
	private final String _type;

	public XMLReader(File file, String type)
	{
		_file = file;
		_type = type;
		_sets = new ArrayList<>();
	}

	public List<StatsSet> parseDocument()
	{
		if(_file == null)
		{
			_log.log(Level.WARN, "XMLReader: Couldn't find the XML File!");
			return null;
		}
		parse();

		return _sets;
	}

	private Document parse()
	{
		Document doc;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(CryptUtil.decryptOnDemand(_file), _file.getParentFile().getAbsolutePath() + File.separator);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "XMLReader: Error loading file " + _file, e);
			return null;
		}

		try
		{
			parseDocument(doc);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "XMLReader: Error in file " + _file, e);
			return null;
		}
		return doc;
	}

	private void parseDocument(Document doc)
	{
		for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if("list".equalsIgnoreCase(n.getNodeName()))
			{
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if(_type.equalsIgnoreCase(d.getNodeName()))
					{
						parseItem(d);
					}
				}
			}
		}
	}

	private void parseItem(Node n)
	{
		StatsSet set = new StatsSet();

		try
		{
			set.set("id", Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue()));
		}
		catch(Exception e)
		{
			// !
		}

		Node first = n.getFirstChild();
		for(n = first; n != null; n = n.getNextSibling())
		{
			if("set".equalsIgnoreCase(n.getNodeName()))
			{
				parseBeanSet(n, set);
			}
		}
		_sets.add(set);
	}

	private void parseBeanSet(Node n, StatsSet set)
	{
		if(n == null)
		{
			return;
		}

		String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
		String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();

		set.set(name, value);
	}
}