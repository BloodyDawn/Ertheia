package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.scripts.quests._10315_ToThePrisonOfDarkness;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 25.12.12
 * Time: 22:23
 */

public class RukhiTeleporter extends Quest
{
	private static final int NPC = 32912;

	public RukhiTeleporter()
	{
		addAskId(NPC, 1);
		addAskId(NPC, 111);
		addAskId(NPC, 112);
		addAskId(NPC, 222);
		addTeleportRequestId(NPC);
	}

	public static void main(String[] args)
	{
		new RukhiTeleporter();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == 1)
		{
			switch(reply)
			{
				case 1: // Деревня Охотников (9200 аден)
					if(player.getAdenaCount() < 9200)
					{
						return "ruki_teleporter006.htm";
					}
					else
					{
						player.reduceAdena(ProcessType.NPC, 9200, npc, true);
						player.teleToLocation(117088, 76931, -2688);
						return null;
					}
				case 2: // Гиран (14000 аден)
					if(player.getAdenaCount() < 14000)
					{
						return "ruki_teleporter006.htm";
					}
					else
					{
						player.reduceAdena(ProcessType.NPC, 14000, npc, true);
						player.teleToLocation(83463, 148045, -3400);
						return null;
					}
				case 3: // Аден (8800 аден)
					if(player.getAdenaCount() < 8800)
					{
						return "ruki_teleporter006.htm";
					}
					else
					{
						player.reduceAdena(ProcessType.NPC, 8800, npc, true);
						player.teleToLocation(146783, 25808, -2008);
						return null;
					}
				case 4: // Руна (6400 аден)
					if(player.getAdenaCount() < 6400)
					{
						return "ruki_teleporter006.htm";
					}
					else
					{
						player.reduceAdena(ProcessType.NPC, 6400, npc, true);
						player.teleToLocation(43835, -47749, -792);
						return null;
					}
			}
		}
		else if(ask == 111)
		{
			switch(reply)
			{
				case 0: // Prison of Darkness
					QuestState pSt = player.getQuestState(_10315_ToThePrisonOfDarkness.class);
					return pSt != null && pSt.isStarted() && pSt.getCond() == 1 ? "ruki_teleporter001a.htm" : "ruki_teleporter001b.htm";
				case 1: // Garden of Genesis
					return "ruki_teleporter001c.htm";
				case 2: // Orbis's Hall
					return "ruki_teleporter001d.htm";
				case 5: // Timiniel's Embrace
					player.teleToLocation(214432, 79857, 816);
					return null;
			}
		}
		else if(ask == 112) // Orbis's Hall
		{
			switch(reply)
			{
				case 0: // Вход в Храм Орбиса
					player.teleToLocation(198703, 86034, -192);
					return null;
				case 1: // 1 этаж Храма Орбиса
					player.teleToLocation(213023, 52487, -8416);
					return null;
				case 2: // 2 этаж Храма Орбиса
					player.teleToLocation(213025, 50424, -14640);
					return null;
				case 3: // 3 этаж Храма Орбиса
					player.teleToLocation(214292, 115551, -12736);
					return null;
			}
		}
		else if(ask == 222)
		{
			if(reply == 1) // Garden of Genesis
			{
				player.teleToLocation(207548, 112214, -2064);
				return null;
			}
		}
		return null;
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		if(player.hasBadReputation())
		{
			return "ruki_teleporter003.htm";
		}
		if(player.getTransformationId() == 111 || player.getTransformationId() == 112 || player.getTransformationId() == 124)
		{
			return "ruki_teleporter004.htm";
		}
		if(player.isCombatFlagEquipped())
		{
			return "ruki_teleporter005.htm";
		}
		return "ruki_teleporter002.htm";
	}
}