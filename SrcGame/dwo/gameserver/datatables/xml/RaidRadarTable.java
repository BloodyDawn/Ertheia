package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.RaidRadarHolder;
import dwo.gameserver.model.world.zone.Location;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.10.12
 * Time: 22:33
 * TODO: что за хуйня тут происходит 0_о?
 */

public class RaidRadarTable extends XmlDocumentParser
{
	private static final Map<Long, List<RaidRadarHolder>> _radarInfo = new HashMap<>();

	private RaidRadarTable()
	{
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

	public static RaidRadarTable getInstance()
	{
		return SingletonHolder._instance;
	}

	@Override
	public void load() throws JDOMException, IOException {
		_radarInfo.clear();
		parseDirectory(FilePath.RAIDRADAR_TABLE);
		_log.log(Level.INFO, RaidRadarTable.class.getSimpleName() + ": Loaded " + _radarInfo.size() + " radar group(s).");
	}

	@Override
	protected void parseDocument(Element rootElement)
	{

	}

	/***
	 * Добавит игроку радар по указанной группе и индексу
	 * @param player инстанс игрока
	 * @param groupId ID группы радаров
	 * @param indexId Index радара
	 */
	public void addRadar(L2PcInstance player, long groupId, int indexId)
	{
		if(_radarInfo.containsKey(groupId))
		{
			List<RaidRadarHolder> temp = _radarInfo.get(groupId);
			if(temp.size() >= indexId)
			{
				Location radarCoords = temp.get(indexId).getLoc();
				player.getRadar().addMarker(radarCoords.getX(), radarCoords.getY(), radarCoords.getZ());
			}
		}
	}

	/**
	 * @param groupId ID группы радаров
	 * @return список радаров в указанной группе
	 */
	public List<RaidRadarHolder> getRadarGroup(long groupId)
	{
		return _radarInfo.get(groupId);
	}

	private static class SingletonHolder
	{
		protected static final RaidRadarTable _instance = new RaidRadarTable();
	}
}