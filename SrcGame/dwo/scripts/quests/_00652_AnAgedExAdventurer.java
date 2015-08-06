package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: Yukio
 * Date: 10.09.12
 * Time: 21:16:07
 */

public class _00652_AnAgedExAdventurer extends Quest
{
	// NPCs
	private static final int TANTAN = 32012;
	private static final int SARA = 30180;

	// ITEMs
	private static final int SOULSHOT_C = 1464;
	private static final int ENCHANT_ARMOR_C = 952;
	private static final int ELIXIR_OF_LIFE_C = 8624;

	public _00652_AnAgedExAdventurer()
	{
		addStartNpc(TANTAN);
		addTalkId(SARA, TANTAN);
	}

	public static void main(String[] args)
	{
		new _00652_AnAgedExAdventurer();
	}

	@Override
	public int getQuestId()
	{
		return 652;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && st.getPlayer().getLevel() >= 46)
		{
			if(st.getCond() == 0)
			{
				if(st.getQuestItemsCount(SOULSHOT_C) >= 100)
				{
					st.takeItems(SOULSHOT_C, 100);
					st.startQuest();
					return "seer_moirase_q0122_0104.htm";
				}
				else
				{
					st.exitQuest(QuestType.REPEATABLE);
					return "retired_oldman_tantan_q0652_05a.htm";
				}
			}
			else
			{
				return "retired_oldman_tantan_q0652_05.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		switch(npcId)
		{
			case TANTAN:
				if(cond == 0)
				{
					if(st.getPlayer().getLevel() >= 46)
					{
						return "retired_oldman_tantan_q0652_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "retired_oldman_tantan_q0652_01a.htm";
					}
				}
				else
				{
					return "retired_oldman_tantan_q0652_02.htm";
				}
			case SARA:
				if(cond == 1)
				{
					if(Rnd.getChance(50))
					{
						st.giveItems(ELIXIR_OF_LIFE_C, 2);
					}
					else
					{
						st.giveItems(ENCHANT_ARMOR_C, 1);
						st.giveItems(ELIXIR_OF_LIFE_C, 1);
					}
					st.exitQuest(QuestType.REPEATABLE);
					return "sara_q0652_01.htm";
				}
				else
				{
					return "sara_q0652_02.htm";
				}
		}
		return null;
	}
}