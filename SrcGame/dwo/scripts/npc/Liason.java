package dwo.scripts.npc;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 09.05.12
 * Time: 0:39
 */

public class Liason extends Quest
{
	// Квестовые персонажи
	private static final int Лией1 = 33155;
	private static final int Лией2 = 33406;

	public Liason()
	{
		addFirstTalkId(Лией1, Лией2);
		addAskId(Лией1, -7);
		addAskId(Лией2, -7);
	}

	public static void main(String[] args)
	{
		new Liason();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -7)
		{
			switch(reply)
			{
				case 1:
					if(npc.getNpcId() == Лией1)
					{
						player.teleToLocation(17225, 114173, -3440);
						player.getInstanceController().setInstanceId(0);
						return null;
					}
					else
					{
						player.teleToLocation(114649, 11115, -5120);
						player.getInstanceController().setInstanceId(0);
						return null;
					}
				case 2:
					return player.getInstanceId() > 0 ? "liason_starter002.htm" : "liason_starter004.htm";
				case 3:
					if(player.getInstanceId() > 0)
					{
						if(player.getPets().isEmpty())
						{
							return "liason_starter005.htm";
						}
						else
						{
							for(L2Summon summon : player.getPets())
							{
								// TODO: Баф
							}
							return "liason_starter003.htm";
						}
					}
					else
					{
						return "liason_starter005.htm";
					}
				case 4:
					return onFirstTalk(npc, player);
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getNpcId() == Лией1 ? "liason_starter006a.htm" : "liason_starter006b.htm";
	}
}