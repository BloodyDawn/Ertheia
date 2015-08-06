package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExperienceTable extends XmlDocumentParser
{
	private static final Map<Integer, Long> _expTable = new HashMap<>();
    protected static ExperienceTable _instance;

    private byte MAX_LEVEL;
	private byte MAX_PET_LEVEL;

	protected ExperienceTable()
	{
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

	public static ExperienceTable getInstance()
	{
		return _instance == null ? _instance = new ExperienceTable() : _instance;
	}

	@Override
	public void load() throws JDOMException, IOException {
		_expTable.clear();
		parseFile(FilePath.EXPERIENCE_TABLE);
		_log.log(Level.INFO, "ExperienceTable: Loaded " + _expTable.size() + " levels.");
		_log.log(Level.INFO, "ExperienceTable: Max Player Level is: " + (MAX_LEVEL - 1) + '.');
		_log.log(Level.INFO, "ExperienceTable: Max Pet Level is: " + (MAX_PET_LEVEL - 1) + '.');
	}

	@Override
	protected void parseDocument(Element rootElement)
	{
		MAX_LEVEL = (byte) (Byte.parseByte(rootElement.getAttributeValue("maxLevel")) + 1);
		MAX_PET_LEVEL = (byte) (Byte.parseByte(rootElement.getAttributeValue("maxPetLevel")) + 1);

		for(Element element : rootElement.getChildren())
		{
            final String name = element.getName();
			if(name.equals("experience"))
			{
				int level = Integer.parseInt(element.getAttributeValue("level"));
				long exp = Long.parseLong(element.getAttributeValue("tolevel"));
				_expTable.put(level, exp);
			}
		}
	}

	public long getExpForLevel(int level)
	{
		return _expTable.get(level);
	}

	public byte getMaxLevel()
	{
		return MAX_LEVEL;
	}

	public byte getMaxPetLevel()
	{
		return MAX_PET_LEVEL;
	}

	public double getExpPercent(int level, long exp)
	{
		return (exp - getExpForLevel(level)) / ((getExpForLevel(level + 1) - getExpForLevel(level)) / 100.0D) * 0.01D;
	}
}