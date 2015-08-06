package dwo.scripts.npc;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.scripts.quests._10285_MeetingSirra;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 07.11.12
 * Time: 17:44
 */

public class FreyaDeacon extends Quest
{
	private static final int FreyaDeaconNpc = 32029;

	public FreyaDeacon()
	{
		addAskId(FreyaDeaconNpc, 656);
		addAskId(FreyaDeaconNpc, -2317);
	}

	public static void main(String[] args)
	{
		new FreyaDeacon();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == FreyaDeaconNpc)
		{
			QuestState st = player.getQuestState(_10285_MeetingSirra.class);
			if(ask == 656)
			{
				if(reply == 1)
				{
					if(st != null && st.isStarted())
					{
						return "freya_deacon_q10285_01.htm";
					}
					else if(player.getLevel() >= 82)
					{
						player.teleToLocation(103045, -124361, -2768);
					}
					else
					{
						return "freya_deacon_q0656_01a.htm";
					}
				}
			}
			else if(ask == -2317)
			{
				if(reply == 1)
				{
					if(player.getLevel() >= 82)
					{
						if(st != null)
						{
							st.setCond(8);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
						player.teleToLocation(103045, -124361, -2768);
					}
					else
					{
						return "freya_deacon_q0656_01a.htm";
					}
				}
			}
		}
		return null;
	}
}