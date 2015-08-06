package dwo.scripts.hellbound;

import dwo.config.FilePath;
import dwo.gameserver.Announcements;
import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.instancemanager.HellboundManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.crypt.datapack.CryptUtil;
import org.apache.log4j.Level;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Engine extends Quest implements Runnable
{
	private static final int UPDATE_INTERVAL = 10000;

	private static final int[][] DOOR_LIST = {
		{
			19250001, 5
		}, {
		19250002, 5
	}, {
		20250001, 9
	}, {
		20250002, 7
	}
	};

	private static final int[] MAX_TRUST = {
		0, 300000, 600000, 1000000, 1010000, 1400000, 1490000, 2000000, 2000001, 2500000, 4000000, 0
	};

	private static final String ANNOUNCE = "Уровень Хеллбаунда достиг уровня: %lvl%";
	private static Map<Integer, PointsInfoHolder> pointsInfo = new HashMap<>();
	private int _cachedLevel = -1;

	public Engine()
	{
		HellboundManager.getInstance().registerEngine(this, UPDATE_INTERVAL);
		loadPointsInfoData();

		// register onKill for all rewardable monsters
		pointsInfo.keySet().forEach(this::addKillId);

		_log.log(Level.INFO, "HellboundEngine: Mode: levels 0-3");
		_log.log(Level.INFO, "HellboundEngine: Level: " + HellboundManager.getInstance().getLevel());
		_log.log(Level.INFO, "HellboundEngine: Trust: " + HellboundManager.getInstance().getTrust());
		if(HellboundManager.getInstance().isLocked())
		{
			_log.log(Level.INFO, "HellboundEngine: State: Locked");
		}
		else
		{
			_log.log(Level.INFO, "HellboundEngine: State: Unlocked");
		}
	}

	public static void main(String[] args)
	{
		new Engine();
	}

	private void onLevelChange(int newLevel)
	{
		try
		{
			HellboundManager.getInstance().setMaxTrust(MAX_TRUST[newLevel]);
			HellboundManager.getInstance().setMinTrust(MAX_TRUST[newLevel - 1]);
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			HellboundManager.getInstance().setMaxTrust(0);
			HellboundManager.getInstance().setMinTrust(0);
		}

		HellboundManager.getInstance().updateTrust(0, false);
		HellboundManager.getInstance().doSpawn();

		for(int[] doorData : DOOR_LIST)
		{
			try
			{
				L2DoorInstance door = DoorGeoEngine.getInstance().getDoor(doorData[0]);
				if(door.isOpened())
				{
					if(newLevel < doorData[1])
					{
						door.closeMe();
					}
				}
				else
				{
					if(newLevel >= doorData[1])
					{
						door.openMe();
					}
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "HellboundEngine: Error while operating with doors (onLevelChange)! ", e);
			}
		}

		if(_cachedLevel > 0)
		{
			Announcements.getInstance().announceToAll(ANNOUNCE.replace("%lvl%", String.valueOf(newLevel)));
			_log.log(Level.INFO, "HellboundEngine: New Level: " + newLevel);
		}
		_cachedLevel = newLevel;
	}

	private void loadPointsInfoData()
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File file = FilePath.HELL_BOUND_TRUST_POINTS_DATA;
		Document doc = null;

		if(file.exists())
		{
			try
			{
				doc = factory.newDocumentBuilder().parse(CryptUtil.decryptOnDemand(file), file.getAbsolutePath());
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Could not parse HellBoundTrustPointsData.xml file: " + e.getMessage(), e);
			}

			for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if("list".equalsIgnoreCase(n.getNodeName()))
				{
					for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if("npc".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							Node att;

							att = attrs.getNamedItem("id");
							if(att == null)
							{
								_log.log(Level.ERROR, "[Hellbound Trust Points Info] Missing NPC ID, skipping record");
								continue;
							}

							int npcId = Integer.parseInt(att.getNodeValue());

							att = attrs.getNamedItem("points");
							if(att == null)
							{
								_log.log(Level.ERROR, "[Hellbound Trust Points Info] Missing reward point info for NPC ID " + npcId + ", skipping record");
								continue;
							}
							int points = Integer.parseInt(att.getNodeValue());

							att = attrs.getNamedItem("minHellboundLvl");
							if(att == null)
							{
								_log.log(Level.ERROR, "[Hellbound Trust Points Info] Missing minHellboundLvl info for NPC ID " + npcId + ", skipping record");
								continue;
							}
							int minHbLvl = Integer.parseInt(att.getNodeValue());

							att = attrs.getNamedItem("maxHellboundLvl");
							if(att == null)
							{
								_log.log(Level.ERROR, "[Hellbound Trust Points Info] Missing maxHellboundLvl info for NPC ID " + npcId + ", skipping record");
								continue;
							}
							int maxHbLvl = Integer.parseInt(att.getNodeValue());

							att = attrs.getNamedItem("lowestTrustLimit");
							int lowestTrustLimit = 0;
							if(att != null)
							{
								lowestTrustLimit = Integer.parseInt(att.getNodeValue());
							}

							pointsInfo.put(npcId, new PointsInfoHolder(points, minHbLvl, maxHbLvl, lowestTrustLimit));
						}
					}
				}
			}
		}
		else
		{
			_log.log(Level.WARN, "Can't locate points info file: HellBoundTrustPointsData.xml");
		}

		_log.log(Level.INFO, "HellboundEngine: Loaded: " + pointsInfo.size() + " trust point reward data");
	}

	@Override
	public void run()
	{
		int level = HellboundManager.getInstance().getLevel();
		if(level > 0 && level == _cachedLevel)
		{
			if(HellboundManager.getInstance().getTrust() == HellboundManager.getInstance().getMaxTrust() && level != 4) // only exclusion is kill of Derek
			{
				level++;
				HellboundManager.getInstance().setLevel(level);
				onLevelChange(level);
			}
		}
		else
		{
			onLevelChange(level); // first run or changed by admin
		}
	}

	// Let's try to manage all trust changes for killing here
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if(pointsInfo.containsKey(npcId))
		{
			PointsInfoHolder npcInfo = pointsInfo.get(npcId);

			if(HellboundManager.getInstance().getLevel() >= npcInfo.minHbLvl && HellboundManager.getInstance().getLevel() <= npcInfo.maxHbLvl && (npcInfo.lowestTrustLimit == 0 || HellboundManager.getInstance().getTrust() > npcInfo.lowestTrustLimit))
			{
				HellboundManager.getInstance().updateTrust(npcInfo.pointsAmount, true);
			}

			if(npc.getNpcId() == 18465 && HellboundManager.getInstance().getLevel() == 4)
			{
				HellboundManager.getInstance().setLevel(5);
			}
		}

		return super.onKill(npc, killer, isPet);
	}

	// Holds info about points for mob killing
	private class PointsInfoHolder
	{
		protected int pointsAmount;
		protected int minHbLvl;
		protected int maxHbLvl;
		protected int lowestTrustLimit;

		protected PointsInfoHolder(int points, int min, int max, int trust)
		{
			pointsAmount = points;
			minHbLvl = min;
			maxHbLvl = max;
			lowestTrustLimit = trust;
		}
	}
}
