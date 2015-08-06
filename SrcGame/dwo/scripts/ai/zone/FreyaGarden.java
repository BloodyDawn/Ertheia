package dwo.scripts.ai.zone;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.type.L2ScriptZone;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 19.04.12
 * Time: 19:41
 */

public class FreyaGarden extends Quest
{
	private static final int ZONE_ID = 33016;
	private static final L2ScriptZone ZONE = (L2ScriptZone) ZoneManager.getInstance().getZoneById(ZONE_ID);
	private static final int[] doors = {23140001, 23140002};
	private boolean doorsOpened;

	public FreyaGarden()
	{
		addEnterZoneId(ZONE_ID);
	}

	public static void main(String[] args)
	{
		new FreyaGarden();
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance && !doorsOpened)
		{
			for(int doorId : doors)
			{
				DoorGeoEngine.getInstance().getDoor(doorId).openMe();
			}
			doorsOpened = true;
			ThreadPoolManager.getInstance().scheduleGeneral(new CloseTask(), 60000);
		}
		return null;
	}

	private class CloseTask implements Runnable
	{
		@Override
		public void run()
		{
			if(ZONE.getCharactersInside().isEmpty())
			{
				for(int doorId : doors)
				{
					DoorGeoEngine.getInstance().getDoor(doorId).closeMe();
				}
				doorsOpened = false;
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new CloseTask(), 60000);
			}
		}
	}
}