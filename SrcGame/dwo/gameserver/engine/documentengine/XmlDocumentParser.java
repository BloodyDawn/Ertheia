package dwo.gameserver.engine.documentengine;

import dwo.gameserver.util.Util;
import dwo.gameserver.util.fileio.filters.XmlFilter;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Zoey76
 * @correct: GenCloud
 */

public abstract class XmlDocumentParser
{
	private static final XmlFilter xmlFilter = new XmlFilter();
	protected final Logger _log = LogManager.getLogger(XmlDocumentParser.class);
    private File _file;

	protected int[] parseIntArray(String name)
	{
        String[] tempStringArray = name.split(",");
        int[] tempIntArray = new int[tempStringArray.length];
        for (int i = 0; i < tempIntArray.length; i++) 
        {
            tempIntArray[i] = Integer.parseInt(tempStringArray[i]);
        }
		return tempIntArray;
	}

	/**
	 * This method can be used to load/reload the data.<br>
	 * It's highly recommended to clear the data storage, either the list or map.
	 */
	public abstract void load() throws JDOMException, IOException;

	/**
	 * Parses a single XML file.<br>
	 * Validation is enforced.
	 * @param f the XML file to parse.
	 */
	protected void parseFile(File f) throws JDOMException, IOException {
		if(!xmlFilter.accept(f))
		{
			_log.log(Level.WARN, "Could not parse " + f.getName() + " is not a file or it doesn't exist!");
			return;
		}
        _file = f;
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(f);
        Element rootElement = document.getRootElement();
		parseDocument(rootElement);
	}

	/**
	 * Wrapper for {@link #parseDirectory(File)}.
	 * @param path the path to the directory where the XML files are.
	 * @return {@code false} if it fails to find the directory, {@code true} otherwise.
	 */
	protected boolean parseDirectory(String path) throws JDOMException, IOException {
		return parseDirectory(new File(path));
	}

	/**
	 * Loads all XML files from {@code path} and calls {@link #parseFile(File)} for each one of them.
	 * @param dir the directory object to scan.
	 * @return {@code false} if it fails to find the directory, {@code true} otherwise.
	 */
	protected boolean parseDirectory(File dir) throws JDOMException, IOException {
		if(!dir.exists())
		{
			_log.log(Level.WARN, "Folder " + dir.getAbsolutePath() + " doesn't exist!");
			return false;
		}

		List<File> listOfFiles = Util.getAllFileList(dir, "xml");
        if (listOfFiles != null)
        {
            for (File f : listOfFiles)
            {
                parseFile(f);
            }
        }
		return true;
	}



	/**
	 * Abstract method that when implemented will parse the current document.<br>
	 * Is expected to be call from {@link #parseFile(File)}.
	 */
	protected abstract void parseDocument(Element element);

    public File getCurrentFile() {
        return _file;
    }
}
