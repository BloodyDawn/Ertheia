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
 * Date: 24.04.12
 * Time: 14:49
 */

public class _00477_Awakened extends Quest
{
	// Квестовые персонажи
	private static final int Гид = 33463;
	private static final int Джастин = 31282;

	// Квестовые предметы
	private static final int КровавыеСлезы = 19496;

	// Квестовые монстры
	private static final int[] Монстры = {
		21294, 21295, 21296, 21297, 21298, 21299, 21300, 21301, 21302, 21303, 21304, 21305, 21307, 21312, 21313
	};

	public _00477_Awakened()
	{
		setMinMaxLevel(70, 74);
		addStartNpc(Гид);
		addTalkId(Гид, Джастин);
		addKillId(Монстры);
		questItemIds = new int[]{КровавыеСлезы};
	}

	public static void main(String[] args)
	{
		new _00477_Awakened();
	}

	@Override
	public int getQuestId()
	{
		return 477;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		switch(event)
		{
			case "33463-04.htm":
				st.startQuest();
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(st.getCond() == 1 && Rnd.getChance(50))
		{
			if(ArrayUtils.contains(Монстры, npc.getNpcId()))
			{
				st.giveItem(КровавыеСлезы);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if(st.getQuestItemsCount(КровавыеСлезы) >= 45)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
		}
		return super.onKill(npc, killer, isPet);
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
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.DAILY);
				case CREATED:
					return "33463-01.htm";
				case STARTED:
					return st.getCond() == 1 ? "33463-05.htm" : "33463-06.htm";
			}
		}
		else if(npc.getNpcId() == Джастин)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 1)
				{
					return "31282-02.htm";
				}
				else if(st.getCond() == 2)
				{
					st.addExpAndSp(8534700, 8523390);
					st.giveAdena(334560, true);
					st.exitQuest(QuestType.DAILY);
					return "31282-01.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "31282-03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 70 && player.getLevel() <= 74;

	}
}