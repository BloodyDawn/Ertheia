package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 02.09.12
 * Time: 19:53
 */

public class _00756_TopQualityPetra extends Quest
{
	// Квестовые персонажи
	private static final int Аку = 33671;

	// Квестовые предметы
	private static final int ПетраЗахака = 35702;
	private static final int ПетраВысшегоКачества = 35703;
	private static final int ЗнакАку = 34910;

	public _00756_TopQualityPetra()
	{
		addStartNpc(Аку);
		addTalkId(Аку);
		questItemIds = new int[]{ПетраВысшегоКачества};
	}

	public static void main(String[] args)
	{
		new _00756_TopQualityPetra();
	}

	@Override
	public int getQuestId()
	{
		return 756;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Аку)
		{
			if(reply == 1 && cond == 1)
			{
				st.giveItem(ЗнакАку);
				st.addExpAndSp(570676680, 261024840);
				st.exitQuest(QuestType.DAILY);
				return "sofa_aku_q0756_02.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		L2PcInstance player = st.getPlayer();

		if(npcId == Аку)
		{
			if(st.isStarted())
			{
				return "sofa_aku_q0756_01.htm";
			}
		}

		return getNoQuestMsg(player);
	}

	@Override
	public String onStartFromItem(L2PcInstance player)
	{
		if(player.getLevel() < 97)
		{
			return "petra_of_zahaq_q0756_02.htm";
		}

		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			st = newQuestState(player);
		}

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}
		else
		{
			return "petra_of_zahaq_q0756_01.htm";
		}

		st.startQuest();
		st.takeItems(ПетраЗахака, 1);
		st.giveItem(ПетраВысшегоКачества);
		return "petra_of_zahaq_q0756_03.htm";
	}
}