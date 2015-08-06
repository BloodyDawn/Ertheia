package dwo.scripts.npc.teleporter;

import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2GrandBossInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.type.L2BossZone;
import dwo.gameserver.util.Rnd;
import dwo.scripts.ai.individual.raidbosses.Antharas;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.12.12
 * Time: 17:45
 */

public class HeartOfWarding extends Quest
{
	// НПЦ
	private static final int NPC = 13001;
	private static final int ANTHARAS_RB = 29028;
	private static final int ANTHARAS_RB_WEAK = 29066;
	private static final int ANTHARAS_RB_NORMAL = 29067;
	private static final int ANTHARAS_RB_STR = 29068;

	// Количество зашедших игроков
	private static int playerCount;

	public HeartOfWarding()
	{
		addTeleportRequestId(NPC);
	}

	public static void main(String[] args)
	{
		new HeartOfWarding();
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		if(Antharas.getInstance() != null)
		{
			int status = GrandBossManager.getInstance().getBossStatus(ANTHARAS_RB);
			int statusW = GrandBossManager.getInstance().getBossStatus(ANTHARAS_RB_WEAK);
			int statusN = GrandBossManager.getInstance().getBossStatus(ANTHARAS_RB_NORMAL);
			int statusS = GrandBossManager.getInstance().getBossStatus(ANTHARAS_RB_STR);

			if(status == 2 || statusW == 2 || statusN == 2 || statusS == 2)
			{
				return "heart_of_warding003.htm";
			}
			else if(status == 3 || statusW == 3 || statusN == 3 || statusS == 3)
			{
				return "heart_of_warding002.htm";
			}
			else if(status == 0 || status == 1) //If entrance to see Antharas is unlocked (he is Dormant or Waiting)
			{
				if(playerCount >= 200)
				{
					return "heart_of_warding005.htm";
				}
				else
				{
					if(player.getItemsCount(3865) > 0)
					{
						L2BossZone zone = GrandBossManager.getInstance().getZone(179700, 113800, -7709);

						if(zone != null)
						{
							zone.allowPlayerEntry(player, 30);
						}

						player.teleToLocation(179700 + Rnd.get(700), 113800 + Rnd.get(2100), -7709);
						playerCount++;

						if(status == 0)
						{
							L2GrandBossInstance antharas = GrandBossManager.getInstance().getBoss(29019);
							QuestManager.getInstance().getQuest(Antharas.class).notifyEvent("waiting", antharas, player);
						}
					}
					else
					{
						return "heart_of_warding004.htm";
					}
				}
			}
		}
		return null;
	}
}