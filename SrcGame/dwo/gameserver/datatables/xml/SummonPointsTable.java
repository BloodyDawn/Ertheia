package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 28.12.11
 * Time: 5:24
 */

public class SummonPointsTable extends XmlDocumentParser
{
	private static final Map<Integer, Integer> _summonPoints = new HashMap<>();

    protected static SummonPointsTable instance;

    private SummonPointsTable()
	{
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

	public static SummonPointsTable getInstance()
	{
		return instance == null ? instance = new SummonPointsTable() : instance;
	}

	@Override
	public void load() throws JDOMException, IOException {
		_summonPoints.clear();
		parseFile(FilePath.SUMMON_POINTS_TABLE);
		_log.log(Level.INFO, "SummonPointsTable: Loaded " + _summonPoints.size() + " summon-points sequences.");
	}

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equals("summonId"))
            {
                _summonPoints.put(Integer.parseInt(element.getAttributeValue("id")),
                        Integer.parseInt(element.getAttributeValue("points")));
            }
        }
    }

	/**
	 * @param npcId NpcId питомца
	 * @return очки для указанного ID питомца, если питомца в таблице нет, то возвращает -1
	 */
	public int getPointsForSummonId(int npcId)
	{
		Integer summonPoints = _summonPoints.get(npcId);
		if(summonPoints == null)
		{
			summonPoints = -1;
		}
		return summonPoints;
	}
}