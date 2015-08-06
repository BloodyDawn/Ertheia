package dwo.scripts.npc.teleporter;

import dwo.config.Config;
import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2GrandBossInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.type.L2BossZone;
import dwo.gameserver.util.Rnd;
import dwo.scripts.ai.individual.raidbosses.Valakas;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.12.12
 * Time: 17:16
 */

public class HeartOfVolcano extends Quest
{
	// НПЦ
	private static final int NPC = 31385;
	private static final int VALAKAS_RB = 29028;
	private static HeartOfVolcano _instance;
	// Количество зашедших игроков
	private static int playerCount;

	public HeartOfVolcano()
	{
		addTeleportRequestId(NPC);
	}

	public static void main(String[] args)
	{
		_instance = new HeartOfVolcano();
	}

	public static HeartOfVolcano getInstance()
	{
		return _instance;
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		if(Valakas.getInstance() != null)
		{
			int status = GrandBossManager.getInstance().getBossStatus(VALAKAS_RB);

			if(status == 0 || status == 1)
			{
				if(playerCount >= 200)
				{
					return "heart_of_volcano004.htm";
				}
				else
				{
					L2BossZone zone = GrandBossManager.getInstance().getZone(212852, -114842, -1632);

					if(zone != null)
					{
						zone.allowPlayerEntry(player, 30);
					}

					player.teleToLocation(204328 + Rnd.get(600), -111874 + Rnd.get(600), 70);

					playerCount++;

					if(status == 0)
					{
						L2GrandBossInstance valakas = GrandBossManager.getInstance().getBoss(29028);
						QuestManager.getInstance().getQuest(Valakas.class).startQuestTimer("1001", Config.VALAKAS_WAIT_TIME, valakas, null);
						GrandBossManager.getInstance().setBossStatus(29028, 1);
					}
					return null;
				}
			}

			else
			{
				return status == 2 ? "heart_of_volcano003.htm" : "heart_of_volcano002.htm";
			}
		}
		return null;
	}

	public int getEnteredToValakasPlayersCount()
	{
		return playerCount;
	}
}