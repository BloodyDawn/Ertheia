package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 07.06.13
 * Time: 13:07
 */

public class _00901_HowLavasaurusesAreMade extends Quest
{
	// Квестовые персонажи
	private static final int Rooney = 32049;

	// Квестовые монстры
	private static final int Newborn = 18799;
	private static final int Fledgling = 18800;
	private static final int Adult = 18801;
	private static final int ElderlyLavasauruses = 18802;

	// Квестовые предметы
	private static final int LavasaurusStoneFragment = 21909;
	private static final int LavasaurusHeadFragment = 21910;
	private static final int LavasaurusBodyFragment = 21911;
	private static final int LavasaurusHornFragment = 21912;

	// Квестовые награды
	private static final int TotemOfBody = 21899;
	private static final int TotemOfSpirit = 21900;
	private static final int TotemOfCourage = 21901;
	private static final int TotemOfFortitude = 21902;

	public _00901_HowLavasaurusesAreMade()
	{
		addStartNpc(Rooney);
		addTalkId(Rooney);
		addKillId(Newborn, Fledgling, Adult, ElderlyLavasauruses);
		questItemIds = new int[]{
			LavasaurusStoneFragment, LavasaurusHeadFragment, LavasaurusBodyFragment, LavasaurusHornFragment
		};
	}

	public static void main(String[] args)
	{
		new _00901_HowLavasaurusesAreMade();
	}

	private void exitQuest(QuestState st)
	{
		st.takeItems(LavasaurusStoneFragment, -1);
		st.takeItems(LavasaurusHeadFragment, -1);
		st.takeItems(LavasaurusBodyFragment, -1);
		st.takeItems(LavasaurusHornFragment, -1);
		st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
		st.exitQuest(QuestType.DAILY);
	}

	@Override
	public int getQuestId()
	{
		return 901;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "warsmith_rooney_q0901_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == Rooney)
		{
			switch(reply)
			{
				case 1:
					return "warsmith_rooney_q0901_04.htm";
				case 2:
					st.giveItem(TotemOfBody);
					exitQuest(st);
					return "warsmith_rooney_q0901_13.htm";
				case 3:
					st.giveItem(TotemOfSpirit);
					exitQuest(st);
					return "warsmith_rooney_q0901_14.htm";
				case 4:
					st.giveItem(TotemOfFortitude);
					exitQuest(st);
					return "warsmith_rooney_q0901_15.htm";
				case 5:
					st.giveItem(TotemOfCourage);
					exitQuest(st);
					return "warsmith_rooney_q0901_16.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(st != null && st.getCond() == 1 && Rnd.getChance(10))
		{
			switch(npc.getNpcId())
			{
				case Newborn:
					if(st.getQuestItemsCount(LavasaurusStoneFragment) < 10)
					{
						st.giveItem(LavasaurusStoneFragment);
					}
					break;
				case Fledgling:
					if(st.getQuestItemsCount(LavasaurusHeadFragment) < 10)
					{
						st.giveItem(LavasaurusHeadFragment);
					}
					break;
				case Adult:
					if(st.getQuestItemsCount(LavasaurusBodyFragment) < 10)
					{
						st.giveItem(LavasaurusBodyFragment);
					}
					break;
				case ElderlyLavasauruses:
					if(st.getQuestItemsCount(LavasaurusHornFragment) < 10)
					{
						st.giveItem(LavasaurusHornFragment);
					}
					break;
			}

			if(st.getQuestItemsCount(LavasaurusStoneFragment) == 10 && st.getQuestItemsCount(LavasaurusHeadFragment) == 10 && st.getQuestItemsCount(LavasaurusBodyFragment) == 10 && st.getQuestItemsCount(LavasaurusHornFragment) == 10)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == Rooney)
		{
			if(st.isNowAvailable() && st.isCompleted())
			{
				st.setState(CREATED);
			}

			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() < 76 ? "warsmith_rooney_q0901_03.htm" : "warsmith_rooney_q0901_01.htm";
				case COMPLETED:
					return "warsmith_rooney_q0901_02.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "warsmith_rooney_q0901_06.htm";
					}
					if(st.getCond() == 2)
					{
						if(st.get("talked") == null)
						{
							st.set("talked", "true");
							return "warsmith_rooney_q0901_07.htm";
						}
						else
						{
							return "warsmith_rooney_q0901_08.htm";
						}
					}
					break;
			}
		}
		return null;
	}
}