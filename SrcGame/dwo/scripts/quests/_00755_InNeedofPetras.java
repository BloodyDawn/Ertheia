package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 03.09.12
 * Time: 13:30
 */

public class _00755_InNeedofPetras extends Quest
{
	// Квестовые персонажи
	private static final int Аку = 33671;

	// Квестовые предметы
	private static final int ПрипасыАку = 35550;
	private static final int ЭнергияРазрушения = 35562;
	private static final int Петра = 34959;

	// Квестовые монстры
	private static final int[] Монстры = {
		23213, 23214, 23227, 23228, 23229, 23230, 23215, 23216, 23217, 23218, 23231, 23232, 23233, 23234, 23237, 23219
	};

	public _00755_InNeedofPetras()
	{
		addStartNpc(Аку);
		addTalkId(Аку);
		addKillId(Монстры);
		questItemIds = new int[]{Петра};
	}

	public static void main(String[] args)
	{
		new _00755_InNeedofPetras();
	}

	@Override
	public int getQuestId()
	{
		return 755;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "sofa_aku_q0755_04.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();

		if(npcId == Аку)
		{
			if(reply == 1)
			{
				return "sofa_aku_q0755_03.htm";
			}
		}

		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if(ArrayUtils.contains(Монстры, npc.getNpcId()))
		{
			executeForEachPlayer(killer, npc, isSummon, true, true);
		}
		return super.onKill(npc, killer, isSummon);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npcId == Аку)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() < 97 ? "sofa_aku_q0755_05.htm" : "sofa_aku_q0755_01.htm";
				case STARTED:
					if(cond == 1)
					{
						return "sofa_aku_q0755_07.htm";
					}
					if(cond == 2)
					{
						st.takeItems(Петра, -1);
						st.giveItem(ПрипасыАку);
						st.giveItem(ЭнергияРазрушения);
						st.exitQuest(QuestType.DAILY);
						return "sofa_aku_q0755_08.htm";
					}
					break;
				case COMPLETED:
					return "sofa_sizraku_q0754_06.htm";
			}
		}

		return getNoQuestMsg(player);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 97;
	}

	@Override
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		QuestState st = player.getQuestState(getClass());
		if(Rnd.getChance(75) && st != null && st.getCond() == 1 && Util.checkIfInRange(1500, npc, player, false))
		{
			if(st.getQuestItemsCount(Петра) >= 50)
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
}