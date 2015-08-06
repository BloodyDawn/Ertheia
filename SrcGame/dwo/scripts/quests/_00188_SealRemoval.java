package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00188_SealRemoval extends Quest
{
	//NPCs
	public static final int LORAIN = 30673;
	public static final int NIKOLA = 30621;
	public static final int DOROTHY = 30970;

	//Items
	public static final int LORAIN_SERTIFICAT = 10362;
	public static final int BROKEN_METAL = 10369;

	public _00188_SealRemoval()
	{
		addStartNpc(LORAIN);
		addTalkId(LORAIN, NIKOLA, DOROTHY);
		questItemIds = new int[]{BROKEN_METAL, LORAIN_SERTIFICAT};
	}

	public static void main(String[] args)
	{
		new _00188_SealRemoval();
	}

	@Override
	public int getQuestId()
	{
		return 188;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && qs.getPlayer().getLevel() >= 41 && qs.hasQuestItems(LORAIN_SERTIFICAT) && !qs.isCompleted())
		{
			qs.startQuest();
			qs.giveItems(BROKEN_METAL, 1);
			return "researcher_lorain_q0188_03.htm";
		}
		return event;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int cond = st.getCond();
		switch(npc.getNpcId())
		{
			case NIKOLA:
				switch(reply)
				{
					case 1:
						return "maestro_nikola_q0188_02.htm";
					case 2:
						return "maestro_nikola_q0188_03.htm";
					case 3:
						if(cond == 1)
						{
							st.setCond(2);
							return "maestro_nikola_q0188_04.htm";
						}
				}
			case DOROTHY:
				if(reply == 1)
				{
					return "dorothy_the_locksmith_q0188_02.htm";
				}
				else if(reply == 2 && cond == 2)
				{
					st.takeItems(BROKEN_METAL, 1);
					st.addExpAndSp(549120, 377296);
					st.giveAdena(110336, true);
					st.unset("cond");
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
					return "dorothy_the_locksmith_q0188_03.htm";
				}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(st.isCompleted())
		{
			return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
		}

		int cond = st.getCond();
		switch(npc.getNpcId())
		{
			case LORAIN:
				switch(cond)
				{
					case 0:
						if(st.getPlayer().getLevel() >= 41 && st.hasQuestItems(LORAIN_SERTIFICAT))
						{
							return "researcher_lorain_q0188_01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "researcher_lorain_q0188_02.htm";
						}
					case 1:
						return "researcher_lorain_q0188_04.htm";
				}

			case NIKOLA:
				switch(cond)
				{
					case 0:
						return "maestro_nikola_q0188_01.htm";
					case 1:
						return "maestro_nikola_q0188_05.htm";
				}
			case DOROTHY:
				if(cond == 2)
				{
					return "dorothy_the_locksmith_q0188_01.htm";
				}
		}
		return getNoQuestMsg(st.getPlayer());
	}
}