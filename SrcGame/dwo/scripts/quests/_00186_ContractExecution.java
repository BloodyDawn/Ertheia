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
 * Date: 17.08.12
 * Time: 0:08
 */

public class _00186_ContractExecution extends Quest
{
	// Квестовые персонажи
	private static final int Lorain = 30673;
	private static final int Nikola = 30621;
	private static final int Luka = 31437;

	// Квестовые монстры
	private static final int[] LetoLizardmans = {20577, 20578, 20579, 20580, 20581, 20582};

	// Квестовые предметы
	private static final short Certificate = 10362;
	private static final short MetalReport = 10366;
	private static final short Accessory = 10367;

	public _00186_ContractExecution()
	{
		addStartNpc(Lorain);
		addTalkId(Lorain, Nikola, Luka);
		addKillId(LetoLizardmans);
		questItemIds = new int[]{MetalReport, Accessory};
	}

	public static void main(String[] args)
	{
		new _00186_ContractExecution();
	}

	@Override
	public int getQuestId()
	{
		return 186;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && !st.isCompleted())
		{
			st.startQuest();
			st.takeItems(Certificate, -1);
			st.giveItems(MetalReport, 1);
			return "researcher_lorain_q0186_03.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Nikola)
		{
			if(reply == 1)
			{
				return "maestro_nikola_q0186_02.htm";
			}
			else if(reply == 2 && cond == 1)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "maestro_nikola_q0186_03.htm";
			}
		}
		else if(npcId == Luka)
		{
			if(reply == 1)
			{
				return "blueprint_seller_luka_q0186_03.htm";
			}
			else if(reply == 2)
			{
				return "blueprint_seller_luka_q0186_04.htm";
			}
			else if(reply == 3 && cond == 2)
			{
				st.giveAdena(105083, true);
				if(player.getLevel() < 50)
				{
					st.addExpAndSp(285935, 18711);
				}
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "blueprint_seller_luka_q0186_06.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		if(st.isStarted() && !st.hasQuestItems(Accessory) && st.getCond() == 2 && Rnd.getChance(20))
		{
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			st.giveItems(Accessory, 1);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Lorain)
		{
			switch(st.getState())
			{
				case CREATED:
					QuestState qs184 = st.getPlayer().getQuestState(_00184_NikolasCooperationContract.class);
					if(st.getPlayer().getLevel() < 41)
					{
						return "researcher_lorain_q0186_02.htm";
					}
					if(qs184 != null && qs184.isCompleted())
					{
						if(st.getQuestItemsCount(Certificate) > 0)
						{
							return "researcher_lorain_q0186_01.htm";
						}
					}
					st.exitQuest(QuestType.REPEATABLE);
					break;
				case STARTED:
					return "researcher_lorain_q0186_04.htm";
				case COMPLETED:
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
			}
		}
		else if(npcId == Nikola)
		{
			if(st.isStarted())
			{
				if(cond == 1)
				{
					return "maestro_nikola_q0186_01.htm";
				}
				else if(cond == 2)
				{
					return "maestro_nikola_q0186_04.htm";
				}
			}
		}
		else if(npcId == Luka)
		{
			if(st.isStarted())
			{
				return !st.hasQuestItems(Accessory) ? "blueprint_seller_luka_q0186_01.htm" : "blueprint_seller_luka_q0186_02.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pqs = player.getQuestState(_00184_NikolasCooperationContract.class);
		return player.getLevel() >= 41 && pqs != null && pqs.isCompleted();
	}
}