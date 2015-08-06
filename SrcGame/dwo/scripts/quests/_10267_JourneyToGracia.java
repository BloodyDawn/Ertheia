package dwo.scripts.quests;

/**
 * @author ANZO
 */

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _10267_JourneyToGracia extends Quest
{
	private static final int ORVEN = 30857;
	private static final int KEUCEREUS = 32548;
	private static final int PAPIKU = 32564;

	private static final int LETTER = 13810;

	public _10267_JourneyToGracia()
	{
		addStartNpc(ORVEN);
		addTalkId(ORVEN, KEUCEREUS, PAPIKU);
	}

	public static void main(String[] args)
	{
		new _10267_JourneyToGracia();
	}

	@Override
	public int getQuestId()
	{
		return 10267;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && event.equals("quest_accept") && !st.isCompleted())
		{
			if(!st.isStarted())
			{
				st.startQuest();
				st.giveItems(LETTER, 1);
			}
			return "highpriest_orven_q10267_08.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState qs, int reply)
	{
		switch(npc.getNpcId())
		{
			case ORVEN:
				switch(reply)
				{
					case 1:
						return "highpriest_orven_q10267_04.htm";
					case 2:
						return "highpriest_orven_q10267_06.htm";
					case 3:
						return "highpriest_orven_q10267_07.htm";
				}
				break;
			case KEUCEREUS:
				switch(reply)
				{
					case 1:
						if(qs.getCond() == 2)
						{
							qs.takeItems(LETTER, -1);
							qs.giveAdena(1135000, true);
							qs.addExpAndSp(5326400, 6000000);
							qs.unset("cond");
							qs.exitQuest(QuestType.ONE_TIME);
							qs.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							return "kserth_q10267_03.htm";
						}
				}
				break;
			case PAPIKU:
				switch(reply)
				{
					case 1:
						if(qs.getCond() == 1)
						{
							qs.setCond(2);
							qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "magister_papiku_q10267_02.htm";
						}
				}
				break;
		}

		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		switch(st.getState())
		{
			case COMPLETED:
				if(npcId == KEUCEREUS)
				{
					return "kserth_q10267_02.htm";
				}
				if(npcId == ORVEN)
				{
					return "highpriest_orven_q10267_03.htm";
				}
				if(npcId == PAPIKU)
				{
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
				}
			case CREATED:
				if(npcId == ORVEN)
				{
					return st.getPlayer().getLevel() < 75 ? "highpriest_orven_q10267_02.htm" : "highpriest_orven_q10267_01.htm";
				}
			case STARTED:
				if(npcId == ORVEN)
				{
					return "highpriest_orven_q10267_09.htm";
				}
				else if(npcId == PAPIKU)
				{
					return cond == 1 ? "magister_papiku_q10267_01.htm" : "magister_papiku_q10267_03.htm";
				}
				else if(npcId == KEUCEREUS && cond == 2)
				{
					return "kserth_q10267_01.htm";
				}
		}
		return null;
	}
}