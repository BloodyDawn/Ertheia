package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 04.03.12
 * Time: 23:32
 */

public class _00135_TempleExecutor extends Quest
{
	// Квестовые персонажи
	private static final int SHEGFIELD = 30068;
	private static final int ALEX = 30291;
	private static final int SONIN = 31773;
	private static final int PANO = 30078;

	// Квестовые предметы
	private static final int CARGO = 10328;
	private static final int CRYSTAL = 10329;
	private static final int MAP = 10330;
	private static final int SONIN_CR = 10331;
	private static final int PANO_CR = 10332;
	private static final int ALEX_CR = 10333;
	private static final int BADGE = 10334;

	// Квестовые монстры
	private static final int[] mobs = {
		20781, 21104, 21105, 21106, 21107
	};

	public _00135_TempleExecutor()
	{
		addStartNpc(SHEGFIELD);
		addTalkId(SHEGFIELD, ALEX, SONIN, PANO);
		addKillId(mobs);
		questItemIds = new int[]{CARGO, CRYSTAL, MAP, SONIN_CR, ALEX_CR, PANO_CR};
	}

	public static void main(String[] args)
	{
		new _00135_TempleExecutor();
	}

	@Override
	public int getQuestId()
	{
		return 135;
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
			case "30068-02.htm":
				st.startQuest();
				break;
			case "30068-09.htm":
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.unset("talk");
				st.exitQuest(QuestType.ONE_TIME);
				st.giveAdena(16924, true);
				st.giveItems(BADGE, 1);
				if(st.getPlayer().getLevel() >= 35 && st.getPlayer().getLevel() <= 40)
				{
					st.addExpAndSp(30000, 2000);
				}
				break;
			case "30068-03.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30291-06.htm":
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
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

		if(ArrayUtils.contains(mobs, npc.getNpcId()))
		{
			if(st.getCond() == 3)
			{
				if(st.getQuestItemsCount(CARGO) < 10)
				{
					st.giveItems(CARGO, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				else if(st.getQuestItemsCount(CRYSTAL) < 10)
				{
					st.giveItems(CRYSTAL, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				else if(st.getQuestItemsCount(MAP) < 10)
				{
					st.giveItems(MAP, 1);
					if(st.getQuestItemsCount(MAP) >= 10 && st.getQuestItemsCount(CARGO) >= 10 && st.getQuestItemsCount(CRYSTAL) >= 10)
					{
						st.setCond(4);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					else
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
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

		if(npc.getNpcId() == SHEGFIELD)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
				case CREATED:
					if(player.getLevel() >= 35)
					{
						return "30068-01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "30068-00.htm";
					}
				case STARTED:
					switch(st.getCond())
					{
						case 1:
							return "30068-02.htm";
						case 2:
						case 3:
						case 4:
							return "30068-04.htm";
						case 5:
							if(st.hasQuestItems(SONIN_CR) && st.hasQuestItems(PANO_CR) && st.hasQuestItems(ALEX_CR))
							{
								st.takeItems(SONIN_CR, -1);
								st.takeItems(PANO_CR, -1);
								st.takeItems(ALEX_CR, -1);
								st.set("talk", "1");
								return "30068-05.htm";
							}
							if(st.getInt("talk") == 1)
							{
								return "30068-06.htm";
							}
							break;
					}
					break;
			}
		}
		else if(npc.getNpcId() == ALEX)
		{
			switch(st.getCond())
			{
				case 2:
					return "30291-01.htm";
				case 3:
					return "30291-07.htm";
				case 4:
					if(st.hasQuestItems(SONIN_CR) && st.hasQuestItems(PANO_CR))
					{
						st.takeItems(MAP, -1);
						st.giveItems(ALEX_CR, 1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						st.setCond(5);
						return "30291-09.htm";
					}
					else
					{
						return "30291-08.htm";
					}
				case 5:
					return "30291-10.htm";
			}
		}
		else if(npc.getNpcId() == SONIN)
		{
			if(st.getCond() == 4)
			{
				if(st.getQuestItemsCount(CARGO) >= 10)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.takeItems(CARGO, -1);
					st.giveItems(SONIN_CR, 1);
					return "31773-01.htm";
				}
				else
				{
					return "31773-02.htm";
				}
			}
		}
		else if(npc.getNpcId() == PANO)
		{
			if(st.getCond() == 4)
			{
				if(st.getQuestItemsCount(CRYSTAL) >= 10)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.takeItems(CRYSTAL, -1);
					st.giveItems(PANO_CR, 1);
					return "30078-01.htm";
				}
				else
				{
					return "30078-02.htm";
				}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 35;

	}
}