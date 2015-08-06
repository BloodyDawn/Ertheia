package dwo.scripts.quests;

import dwo.gameserver.instancemanager.MentorManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.L2Mentee;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collection;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 12.04.12
 * Time: 6:46
 */

public class _00245_ComeToMe extends Quest
{
	// Квестовые персонажи
	private static final int Феррис = 30847;

	// Квестовые предметы
	private static final int ПепелКостра = 30322;
	private static final int КристаллОпыта = 30323;
	private static final int КристаллА = 1461;
	private static final int КольцоНаставника = 30383; // TODO: предмет

	// Квестовые монстры
	private static final int[] МонстрыТопи = {21110, 21111};
	private static final int[] МонстрыТопи2 = {21112, 21113, 21115, 21116};

	public _00245_ComeToMe()
	{
		addStartNpc(Феррис);
		addTalkId(Феррис);
		addKillId(МонстрыТопи);
		addKillId(МонстрыТопи2);
		addFirstTalkId(Феррис);
		questItemIds = new int[]{ПепелКостра, КристаллОпыта};
	}

	public static void main(String[] args)
	{
		new _00245_ComeToMe();
	}

	@Override
	public int getQuestId()
	{
		return 245;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(player.isMentor() && event.equals("30847-13.htm"))
		{
			L2PcInstance mentee = getCurrentMentee(player);
			if(mentee != null)
			{
				if(player.destroyItemByItemId(ProcessType.QUEST, КристаллА, 100, npc, true))
				{
					mentee.getQuestState(getClass()).setCond(3);
					return event;
				}
				else
				{
					return "30847-14.htm";
				}
			}
			else
			{
				return "30847-12.htm";
			}
		}

		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(event.equals("30847-04.htm"))
		{
			st.startQuest();
		}
		else if(event.equals("30847-07.htm"))
		{
			st.set("talk", "1");
			st.takeItems(ПепелКостра, -1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return super.onKill(npc, killer, isPet);
		}

		if(st.getCond() == 1)
		{
			if(ArrayUtils.contains(МонстрыТопи, npc.getNpcId()) && Rnd.getChance(50))
			{
				st.giveItem(ПепелКостра);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if(st.getQuestItemsCount(ПепелКостра) >= 15)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
		}
		else if(st.getCond() == 4)
		{
			if(ArrayUtils.contains(МонстрыТопи2, npc.getNpcId()))
			{
				if(killer.isMentee())
				{
					L2PcInstance mentor = MentorManager.getInstance().getMentor(killer.getObjectId()).getPlayerInstance();
					if(mentor != null && Util.checkIfInRange(400, killer, mentor, false))
					{
						st.giveItem(КристаллОпыта);
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						if(st.getQuestItemsCount(КристаллОпыта) >= 8)
						{
							st.setCond(5);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
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

		if(npc.getNpcId() == Феррис)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "30847-03.htm";
				case CREATED:
					if(player.getLevel() >= 70 && player.getLevel() <= 75 && player.isMentee() && player.isAcademyMember())
					{
						return "30847-01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "30847-02.htm";
					}
				case STARTED:
					switch(st.getCond())
					{
						case 1:
							return "30847-05.htm";
						case 2:
							if(st.getBool("talk"))
							{
								if(player.isMentee())
								{
									L2PcInstance mentor = MentorManager.getInstance().getMentor(player.getObjectId()).getPlayerInstance();
									return mentor != null && mentor.isOnline() && Util.checkIfInRange(200, npc, mentor, true) ? "30847-10.htm" : "30847-08.htm";
								}
								else
								{
									return "30847-09.htm";
								}
							}
							else
							{
								return "30847-06.htm";
							}
						case 3:
							st.setCond(4);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "30847-17.htm";
						case 4:
							return "30847-18.htm";
						case 5:
							st.takeItems(КристаллОпыта, -1);
							st.addExpAndSp(2018733, 200158);
							st.giveItem(КольцоНаставника);
							if(player.isAcademyMember())
							{
								player.getClan().addReputationScore(1000, true);
							}
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.exitQuest(QuestType.ONE_TIME);
							return "30847-19.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(player.isMentor() && npc.getNpcId() == Феррис)
		{
			L2PcInstance mentee = getCurrentMentee(player);

			if(mentee != null)
			{
				return "30847-11.htm";
			}
		}
		npc.showChatWindow(player);
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 70 && player.getLevel() <= 75 && player.isMentee() && player.isAcademyMember();

	}

	/**
	 * @param mentor персонаж наставника
	 * @return ближайшего к наставнику ученика с взятым квестом
	 */
	private L2PcInstance getCurrentMentee(L2PcInstance mentor)
	{
		L2PcInstance mentee = null;
		Collection<L2Mentee> mentees = MentorManager.getInstance().getMentees(mentor.getObjectId());
		for(L2Mentee pl : mentees)
		{
			if(pl.isOnline() && Util.checkIfInRange(200, mentor, pl.getPlayerInstance(), false))
			{
				QuestState st = pl.getPlayerInstance().getQuestState(getClass());
				if(st != null && st.getCond() == 2)
				{
					mentee = pl.getPlayerInstance();
				}
			}
		}
		return mentee;
	}
}