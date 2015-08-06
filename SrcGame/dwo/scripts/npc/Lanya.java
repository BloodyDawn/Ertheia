package dwo.scripts.npc;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.scripts.quests._10369_NoblesseTheTestOfSoul;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.06.13
 * Time: 14:19
 */
public class Lanya extends Quest
{
	private static final int NPC = 33696;

	private static final int EMPTY_BOTTLE = 34887;

	public Lanya()
	{
		addAskId(NPC, -1021);
	}

	public static void main(String[] args)
	{
		new Lanya();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == NPC)
		{
			if(ask == -1021)
			{
				if(reply == 1)
				{
					QuestState st = player.getQuestState(_10369_NoblesseTheTestOfSoul.class);
					if(st != null && !st.hasQuestItems(EMPTY_BOTTLE) && st.getCond() == 5)
					{
						st.giveItem(EMPTY_BOTTLE);
						return null;
					}
					else
					{
						return "ranya002.htm";
					}
				}
			}
		}
		return null;
	}
}
