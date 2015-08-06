package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.Location;
import dwo.scripts.quests._00240_ImtheOnlyOneYouCanTrust;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 17.05.12
 * Time: 23:37
 */

public class Kintaijin extends Quest
{
	private static final Location[] teleportData = {
		new Location(80456, -52322, -5640), new Location(88718, -46214, -4640), new Location(87464, -54221, -5120),
		new Location(80848, -49426, -5128), new Location(87682, -43291, -4128)
	};

	private static final int kintaijin = 32640;

	public Kintaijin()
	{
		addAskId(kintaijin, -240);
	}

	public static void main(String[] args)
	{
		new Kintaijin();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		QuestState accessQuest = player.getQuestState(_00240_ImtheOnlyOneYouCanTrust.class);
		switch(reply)
		{
			case 1:
				return accessQuest != null && accessQuest.isCompleted() ? "kintaijin002.htm" : "kintaijin002a.htm";
			case 10:
				return "kintaijin010.htm";
			case 11:
				return "kintaijin011.htm";
			case 12:
				return "kintaijin012.htm";
			case 13:
				return "kintaijin013.htm";
			case 14:
				return "kintaijin014.htm";
			case 20:
			case 21:
			case 22:
			case 23:
			case 24:
				if(accessQuest != null && accessQuest.isCompleted())
				{
					return "kintaijin002.htm";
				}
				if(player.getParty() != null)
				{
					for(L2PcInstance member : player.getParty().getMembers())
					{
						member.teleToLocation(teleportData[reply - 20]);
					}
				}
				player.teleToLocation(teleportData[reply - 20]);
				return "kintaijin020.htm";
		}
		return null;
	}
}