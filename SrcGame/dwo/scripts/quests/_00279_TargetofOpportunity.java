package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

/**
 * @author ANZO
 */

public class _00279_TargetofOpportunity extends Quest
{
	public _00279_TargetofOpportunity()
	{
		addStartNpc(32302);
		addTalkId(32302, 22373, 22374, 22375, 22376);
		questItemIds = new int[]{15517, 15518, 15519, 15520};
	}

	public static void main(String[] args)
	{
		new _00279_TargetofOpportunity();
	}

	@Override
	public int getQuestId()
	{
		return 279;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		if(event.equalsIgnoreCase("32302-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("32302-06.htm"))
		{
			st.giveItems(15515, 1);
			st.giveItems(15516, 1);
			st.takeItems(15517, 1);
			st.takeItems(15518, 1);
			st.takeItems(15519, 1);
			st.takeItems(15520, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.REPEATABLE);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(Rnd.getChance(50))
		{
			L2PcInstance plr = getRandomPartyMember(player, "1");
			if(plr == null)
			{
				return super.onKill(npc, player, isPet);
			}
			QuestState st = plr.getQuestState(getClass());
			int item = 0;
			switch(npc.getNpcId())
			{
				case 22373:
					item = 15517;
					break;
				case 22374:
					item = 15518;
					break;
				case 22375:
					item = 15519;
					break;
				case 22376:
					item = 15520;
					break;
			}
			if(!st.hasQuestItems(item))
			{
				st.giveItems(item, 1);
				if(st.hasQuestItems(15517) && st.hasQuestItems(15518) && st.hasQuestItems(15519) && st.hasQuestItems(15520))
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return htmltext;
		}
		if(st.getState() == CREATED)
		{
			htmltext = player.getLevel() >= 82 ? "32302-00.htm" : "32302-00a.htm";
		}
		int cond = st.getCond();
		if(cond == 1)
		{
			htmltext = "32302-04.htm";
		}
		else if(cond == 2)
		{
			htmltext = "32302-05.htm";
		}
		return htmltext;
	}
}