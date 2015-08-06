package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 23.04.12
 * Time: 3:04
 */

public class _00470_DivinityProtector extends Quest
{
	// Квестовые персонажи
	private static final int Гид = 33463;
	private static final int Агрипель = 31348;

	// Квестовые предметы
	private static final int ПотерявшаяСветДуша = 19489;

	// Квестовые монстры
	private static final int[] Монстры = {
		21520, 21521, 21522, 21523, 21524, 21525, 21526, 21527, 21528, 21542, 21543, 21529, 21530, 21541, 21532, 21533,
		21534, 21535, 21536, 21545, 21546, 21537, 21538, 21539, 21540, 21544
	};

	public _00470_DivinityProtector()
	{
		setMinMaxLevel(60, 64);
		addStartNpc(Гид);
		addTalkId(Гид, Агрипель);
		addKillId(Монстры);
		questItemIds = new int[]{ПотерявшаяСветДуша};
	}

	public static void main(String[] args)
	{
		new _00470_DivinityProtector();
	}

	@Override
	public int getQuestId()
	{
		return 470;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(st.getCond() == 1)
		{
			if(Rnd.getChance(50) && ArrayUtils.contains(Монстры, npc.getNpcId()))
			{
				st.giveItem(ПотерявшаяСветДуша);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if(st.getQuestItemsCount(ПотерявшаяСветДуша) >= 20)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.setCond(2);
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == Гид)
		{
			switch(st.getState())
			{
				case CREATED:
					return "33463-01.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33463-05.htm";
					}
					if(st.getCond() == 2)
					{
						return "33463-06.htm";
					}
					break;
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.DAILY);
			}
		}
		else if(npc.getNpcId() == Агрипель)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 2)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.DAILY);
					return "31348-01.htm";
				}
				else
				{
					return "31348-02.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "31348-03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 60 && player.getLevel() <= 64;

	}
}