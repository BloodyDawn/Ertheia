package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 10.06.13
 * Time: 20:45
 */

public class _10463_TheSoulofaSword extends Quest
{
	// Квестовые персонажи
	private static final int FLUTTER = 30677;

	// Квестовые предметы
	private static final int PRACTICE_STORMBRINGER = 36720;
	private static final int PRACTICE_STORMBRINGER_SA = 36723;
	private static final int PRACTICE_SA = 36721;
	private static final int PRACTICE_GEM = 36722;

	// Квестоые награды
	private static final int C_GEM = 2131;
	private static final int SA_RED = 4634;
	private static final int SA_GREEN = 4645;
	private static final int SA_BLUE = 4656;

	public _10463_TheSoulofaSword()
	{
		addStartNpc(FLUTTER);
		addTalkId(FLUTTER);
		questItemIds = new int[]{PRACTICE_STORMBRINGER, PRACTICE_STORMBRINGER_SA, PRACTICE_SA, PRACTICE_GEM};
	}

	public static void main(String[] args)
	{
		new _10463_TheSoulofaSword();
	}

	@Override
	public int getQuestId()
	{
		return 10463;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && !st.isCompleted())
		{
			st.startQuest();
			st.giveItem(PRACTICE_STORMBRINGER);
			st.giveItems(PRACTICE_GEM, 97);
			st.giveItem(PRACTICE_SA);
			return "head_blacksmith_flutter_q10463_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == FLUTTER)
		{
			switch(reply)
			{
				case 1:
					return "head_blacksmith_flutter_q10463_03.htm";
				case 2:
					return "head_blacksmith_flutter_q10463_04.htm";
				case 11:
					if(st.hasQuestItems(PRACTICE_STORMBRINGER_SA))
					{
						st.giveItem(SA_RED);
						st.giveItems(C_GEM, 97);
						st.addExpAndSp(504210, 5042);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						return "head_blacksmith_flutter_q10463_08.htm";
					}
					break;
				case 12:
					if(st.hasQuestItems(PRACTICE_STORMBRINGER_SA))
					{
						st.giveItem(SA_GREEN);
						st.giveItems(C_GEM, 97);
						st.addExpAndSp(504210, 5042);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						return "head_blacksmith_flutter_q10463_08.htm";
					}
					break;
				case 13:
					if(st.hasQuestItems(PRACTICE_STORMBRINGER_SA))
					{
						st.giveItem(SA_BLUE);
						st.giveItems(C_GEM, 97);
						st.addExpAndSp(504210, 5042);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						return "head_blacksmith_flutter_q10463_08.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(npc.getNpcId() == FLUTTER)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
				case CREATED:
					if(st.getPlayer().getLevel() >= 52 && st.getPlayer().getLevel() <= 58)
					{
						return "head_blacksmith_flutter_q10463_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "head_blacksmith_flutter_q10463_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return st.hasQuestItems(PRACTICE_STORMBRINGER_SA) ? "head_blacksmith_flutter_q10463_07.htm" : "head_blacksmith_flutter_q10463_06.htm";
					}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 52 && player.getLevel() <= 58;
	}
}