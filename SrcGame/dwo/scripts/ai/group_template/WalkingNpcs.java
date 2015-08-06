package dwo.scripts.ai.group_template;

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.instancemanager.WalkingManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 19.01.12
 * Time: 19:07
 */

public class WalkingNpcs extends Quest
{
	public WalkingNpcs()
	{
		// Авто-спаун НПЦ
		addSpawn(31361, new Location(22418, 10249, -3648));
		addSpawn(32072, new Location(84429, -144065, -1542));
		addSpawn(32070, new Location(90271, -143869, -1547));
		addSpawn(33241, new Location(-114544, 258706, -1192));
		addSpawn(33246, new Location(-114920, 259866, -1192));
		addSpawn(4309, new Location(-13238, 121248, -2990));
		addSpawn(4310, new Location(84871, 147769, -3405));
		addSpawn(4311, new Location(146359, 24213, -2013));
		addSpawn(4312, new Location(43262, -47084, -798));
		addSpawn(33106, new Location(-112424, 257813, -1395));
		addSpawn(33228, new Location(-113494, 255759, -1395));
		addSpawn(33121, new Location(-113361, 255703, -1395));
		addSpawn(33208, new Location(-114281, 257288, -995));
		addSpawn(33120, new Location(-114702, 255075, -1395));
		addSpawn(33207, new Location(-114666, 254736, -1395));
		addSpawn(32901, new Location(206413, 88164, -1130));
		addSpawn(32902, new Location(205359, 88653, -1040));
		addSpawn(32903, new Location(206166, 87839, -1115));

		//Управляемый спаун НПЦ по ID Роутов
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(33105, new Location(-114058, 253867, -1551), 0), 23);
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(33105, new Location(-114730, 254433, -1555), 0), 24);
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(33105, new Location(-113992, 254166, -1543), 0), 25);
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(33105, new Location(-113888, 254994, -1539), 0), 26);

		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(33119, new Location(-115204, 237366, -3095), 0), 27);
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(33119, new Location(-115192, 237342, -3095), 0), 28);

		//Солдаты из руин страданий
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(33427, new Location(-42526, 120213, -3419), 0), 29);
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(33427, new Location(-41044, 122583, -2841), 0), 30);

		//Солдаты из руин отчаяния
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(33432, new Location(-20578, 135348, -3823), 0), 31);
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(33432, new Location(-20546, 137810, -3822), 0), 32);

		//Солдаты из Пустоши
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(33435, new Location(-16622, 208785, -3500), 0), 33);
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(33435, new Location(-16652, 207365, -3500), 0), 34);

		// Холм ветрянных мельниц
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(33422, new Location(-76392, 166496, -3475), 0), 35);
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(33422, new Location(-75839, 169533, -3757), 0), 36);
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(33422, new Location(-70648, 172233, -3700), 0), 37);

		/*
		 * routeId: 38 - 45 - заняты под волкеров -> _0015_SeedOfHellfire.java
		 */

		// Солдаты-охранники на Грации
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(32628, new Location(-148777, 254544, -186), 0), 59);
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(32628, new Location(-148804, 254521, -187), 0), 60);
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(32629, new Location(-149929, 254543, -187), 0), 61);
		WalkingManager.getInstance().startMoving(getNpcSpawnInstance(32629, new Location(-149909, 254506, -187), 0), 62);
	}

	public static void main(String[] args)
	{
		new WalkingNpcs();
	}

	private L2Npc getNpcSpawnInstance(int npcId, Location loc, int respawnDelay)
	{
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		try
		{
			L2Spawn npcSpawn = new L2Spawn(npcTemplate);
			npcSpawn.setLocation(loc);
			npcSpawn.setHeading(0);
			npcSpawn.setAmount(1);
			if(respawnDelay == 0)
			{
				npcSpawn.stopRespawn();
			}
			else
			{
				npcSpawn.setRespawnDelay(respawnDelay);
				npcSpawn.startRespawn();
			}
			SpawnTable.getInstance().addNewSpawn(npcSpawn);
			return npcSpawn.spawnOne(false);
		}
		catch(Exception ignored)
		{
		}
		return null;
	}
}
