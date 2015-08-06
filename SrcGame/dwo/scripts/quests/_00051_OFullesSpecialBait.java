package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00051_OFullesSpecialBait extends Quest
{
	int OFulle = 31572;
	int FetteredSoul = 20552;

	int LostBaitIngredient = 7622;
	int IcyAirFishingLure = 7611;

	public _00051_OFullesSpecialBait()
	{
		addStartNpc(OFulle);
		addTalkId(OFulle);
		addKillId(FetteredSoul);
		questItemIds = new int[]{LostBaitIngredient};
	}

	public static void main(String[] args)
	{
		new _00051_OFullesSpecialBait();
	}

	@Override
	public int getQuestId()
	{
		return 51;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		if(event.equals("31572-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equals("takeitem"))
		{
			if(st.getQuestItemsCount(LostBaitIngredient) < 100)
			{
				htmltext = "31572-07.htm";
			}
			else
			{
				htmltext = "31572-06.htm";
				st.unset("cond");
				st.takeItems(LostBaitIngredient, 100);
				st.giveItems(IcyAirFishingLure, 4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == FetteredSoul && st.getCond() == 1)
		{
			if(st.getQuestItemsCount(LostBaitIngredient) < 100 && Rnd.getChance(30))
			{
				st.giveItems(LostBaitIngredient, 1);
				if(st.getQuestItemsCount(LostBaitIngredient) == 100)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(2);
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		QuestStateType id = st.getState();
		String htmltext = getNoQuestMsg(st.getPlayer());

		if(npcId == OFulle)
		{
			if(id == COMPLETED)
			{
				htmltext = getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
			}
			else if(cond == 1)
			{
				htmltext = "31572-05.htm";
			}
			else if(cond == 2)
			{
				htmltext = "31572-04.htm";
			}
			else if(cond == 0)
			{
				if(st.getPlayer().getLevel() > 35 && st.getPlayer().getLevel() < 39)
				{
					htmltext = "31572-01.htm";
				}
				else
				{
					htmltext = "31572-02.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
			}
		}
		return htmltext;
	}
}
