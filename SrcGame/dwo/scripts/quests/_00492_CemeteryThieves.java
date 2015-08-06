package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 01.03.12
 * Time: 8:22
 */

public class _00492_CemeteryThieves extends Quest
{
	// Квестовые персонажи
	private static final int Зения = 32140;

	// Квестовые монстры
	private static final int[] Монстры = {23193, 23194, 23195, 23196};

	// Квестовые предметы
	private static final int РеликвииДревнейИмперии = 34769;

	public _00492_CemeteryThieves()
	{
		addStartNpc(Зения);
		addTalkId(Зения);
		addKillId(Монстры);
		questItemIds = new int[]{РеликвииДревнейИмперии};
	}

	public static void main(String[] args)
	{
		new _00492_CemeteryThieves();
	}

	@Override
	public int getQuestId()
	{
		return 492;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(event.equals("32140-06.htm"))
		{
			st.startQuest();
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

		if(st.getCond() == 1)
		{
			if(Rnd.getChance(50) && ArrayUtils.contains(Монстры, npc.getNpcId()))
			{
				if(st.getQuestItemsCount(РеликвииДревнейИмперии) < 50)
				{
					st.giveItem(РеликвииДревнейИмперии);
					if(st.getQuestItemsCount(РеликвииДревнейИмперии) >= 50)
					{
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
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

		if(npc.getNpcId() == Зения)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "32140-05.htm";
				case CREATED:
					if(player.getLevel() >= 80)
					{
						if(!player.isAwakened() && player.getClassId().level() == ClassLevel.THIRD.ordinal())
						{
							return "32140-01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "32140-02.htm";
						}
					}
					else
					{
						return "32140-03.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "32140-07.htm";
					}
					if(st.getCond() == 2 && st.getQuestItemsCount(РеликвииДревнейИмперии) >= 50)
					{
						st.addExpAndSp(25000000, 28500000);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						return "32140-08.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 80 && !player.isAwakened() && player.getClassId().level() == ClassLevel.THIRD.ordinal();

	}
}