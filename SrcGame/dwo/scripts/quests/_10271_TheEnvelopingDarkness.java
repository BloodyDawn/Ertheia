// Done by ANZO

package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _10271_TheEnvelopingDarkness extends Quest
{
	// Квестовые персонажи
	private static final int ORBY = 32560;
	private static final int EL = 32556;
	private static final int MEDIBAL = 32528;

	// Квестовые итемы
	private static final int DOCMEDIBAL = 13852;

	public _10271_TheEnvelopingDarkness()
	{
		addStartNpc(ORBY);
		addTalkId(ORBY, EL, MEDIBAL);
	}

	public static void main(String[] args)
	{
		new _10271_TheEnvelopingDarkness();
	}

	@Override
	public int getQuestId()
	{
		return 10271;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		switch(event)
		{
			case "quest_accept":
				if(!st.isStarted())
				{
					st.startQuest();
					return "wharf_soldier_orbiu_q10271_05.htm";
				}
		}
		return event;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState qs, int reply)
	{
		switch(npc.getNpcId())
		{
			case ORBY:
				switch(reply)
				{
					case 1:
						return "wharf_soldier_orbiu_q10271_04.htm";
				}
				break;
			case EL:
				switch(reply)
				{
					case 1:
						return "soldier_el_q10271_03.htm";
					case 2:
						return "soldier_el_q10271_04.htm";
					case 3:
						return "soldier_el_q10271_05.htm";
					case 4:
						if(qs.getCond() == 1)
						{
							qs.setCond(2);
							qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "soldier_el_q10271_06.htm";
						}
					case 10:
						if(qs.getCond() == 3)
						{
							qs.setCond(4);
							qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "soldier_el_q10271_09.htm";
						}
				}
				break;
		}

		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		int cond = st.getCond();
		if(st.isCompleted())
		{
			return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
		}
		if(npc.getNpcId() == ORBY)
		{
			if(cond == 0)
			{
				return player.getLevel() >= 75 ? "wharf_soldier_orbiu_q10271_04.htm" : "wharf_soldier_orbiu_q10271_02.htm";
			}
			else if(cond >= 1 && cond < 4)
			{
				return "wharf_soldier_orbiu_q10271_06.htm";
			}
			else if(cond == 4)
			{
				st.takeItems(DOCMEDIBAL, -1);
				st.addExpAndSp(1109665, 1229015);
				st.giveAdena(236510, true);
				st.unset("cond");
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "wharf_soldier_orbiu_q10271_08.htm";
			}
			else if(st.isCompleted())
			{
				return "wharf_soldier_orbiu_q10271_03.htm";
			}
		}
		else if(npc.getNpcId() == EL)
		{
			if(st.isCompleted())
			{
				return "soldier_el_q10271_02.htm";
			}

			switch(cond)
			{
				case 1:
					return "soldier_el_q10271_01.htm";
				case 2:
					return "soldier_el_q10271_07.htm";
				case 3:
					return "soldier_el_q10271_08.htm";
				case 4:
					return "soldier_el_q10271_10.htm";
			}
		}
		else if(npc.getNpcId() == MEDIBAL)
		{
			if(cond == 2)
			{
				st.giveItems(DOCMEDIBAL, 1);
				st.setCond(3);
				return "corpse_of_medival_q10271_01.htm";
			}
			else if(cond == 3)
			{
				return "corpse_of_medival_q10271_03.htm";
			}
			else if(st.isCompleted())
			{
				return "corpse_of_medival_q10271_02.htm";
			}
		}
		return null;
	}
}