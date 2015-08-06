package dwo.scripts.quests;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * @author ANZO
 */

public class _00270_TheOneWhoEndsSilence extends Quest
{
	private static final TIntObjectHashMap<Integer> Drops = new TIntObjectHashMap<>();

	// Квестовые предметы
	private static final int SpScrollLow = 5593;
	private static final int SpScrollMedium = 5594;
	private static final int SpScrollHigh = 5595;
	private static final int SpScrollHighest = 9898;
	private static final int Clothes = 15526;
	private static final int[] Recepies = {10373, 10374, 10375, 10376, 10377, 10378, 10379, 10380, 10381};
	private static final int[] Pieces = {10397, 10398, 10399, 10400, 10401, 10402, 10403, 10404, 10405,};

	public _00270_TheOneWhoEndsSilence()
	{
		addStartNpc(32757);
		addTalkId(32757);
		Drops.put(22789, 10);
		Drops.put(22790, 10);
		Drops.put(22791, 10);
		Drops.put(22792, 10);
		Drops.put(22793, 10);
		Drops.put(18909, 15);
		Drops.put(18910, 15);
		Drops.put(22794, 75);
		Drops.put(22795, 75);
		Drops.put(22796, 75);
		Drops.put(22797, 75);
		Drops.put(22798, 150);
		Drops.put(22799, 150);
		Drops.put(22800, 150);
		addKillId(Drops.keys());
		questItemIds = new int[]{Clothes};
	}

	public static void main(String[] args)
	{
		new _00270_TheOneWhoEndsSilence();
	}

	@Override
	public int getQuestId()
	{
		return 270;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		if(event.equals("32757-02.htm"))
		{
			st.startQuest();
		}
		else if(event.equals("32757-07.htm"))
		{
			st.unset("cond");
			st.takeItems(Clothes, -1);
			st.exitQuest(QuestType.REPEATABLE);
		}
		else if(NumberUtils.isDigits(event))
		{
			int r;
			int count = Integer.parseInt(event);
			if(st.getQuestItemsCount(Clothes) < count)
			{
				return "32757-05a.htm";
			}
			switch(count)
			{
				case 100:
					r = Rnd.get(100);
					if(r < 20) // 20%
					{
						st.giveItems(SpScrollHighest, 1);
					}
					else if(r < 40) // 20%
					{
						st.giveItems(SpScrollHigh, 1);
					}
					else if(r < 55) // 15%
					{
						st.giveItems(SpScrollMedium, 1);
					}
					else // 45%
					{
						st.giveItems(Recepies[Rnd.get(Recepies.length)], 1);
					}
					st.takeItems(Clothes, 100);
					break;
				case 200:
					st.giveItems(Recepies[Rnd.get(Recepies.length)], 1);
					r = Rnd.get(100);
					if(r < 17) // 17%
					{
						st.giveItems(SpScrollLow, 1);
					}
					else if(r < 34) // 17%
					{
						st.giveItems(SpScrollMedium, 1);
					}
					else if(r < 67) // 33%
					{
						st.giveItems(SpScrollHigh, 1);
					}
					else // 33%
					{
						st.giveItems(SpScrollHighest, 1);
					}
					st.takeItems(Clothes, 200);
					break;
				case 300:
					st.giveItems(Recepies[Rnd.get(Recepies.length)], 1);
					st.giveItems(Pieces[Rnd.get(Pieces.length)], 1);
					r = Rnd.get(100);
					if(r < 25) // 25%
					{
						st.giveItems(SpScrollLow, 1);
					}
					else if(r < 50) // 25%
					{
						st.giveItems(SpScrollMedium, 1);
					}
					else if(r < 75) // 25%
					{
						st.giveItems(SpScrollHigh, 1);
					}
					else // 25%
					{
						st.giveItems(SpScrollHighest, 1);
					}
					st.takeItems(Clothes, 300);
					break;
				case 400:
					st.giveItems(Recepies[Rnd.get(Recepies.length)], 1);
					st.giveItems(Pieces[Rnd.get(Pieces.length)], 1);
					r = Rnd.get(100);
					if(r < 30) // 30%
					{
						st.giveItems(SpScrollMedium, 1);
					}
					else if(r < 60) // 30%
					{
						st.giveItems(SpScrollHigh, 1);
					}
					else // 40%
					{
						st.giveItems(SpScrollHighest, 1);
					}
					r = Rnd.get(100);
					if(r < 45) // 45%
					{
						st.giveItems(Recepies[Rnd.get(Recepies.length)], 1);
					}
					else if(r < 60) // 15%
					{
						st.giveItems(SpScrollLow, 1);
					}
					else if(r < 75) // 15
					{
						st.giveItems(SpScrollMedium, 1);
					}
					else if(r < 90) // 15%
					{
						st.giveItems(SpScrollHigh, 1);
					}
					else // 10%
					{
						st.giveItems(SpScrollHighest, 1);
					}
					st.takeItems(Clothes, 400);
					break;
				case 500:
					st.giveItems(Recepies[Rnd.get(Recepies.length)], 1);
					st.giveItems(Recepies[Rnd.get(Recepies.length)], 1);
					st.giveItems(Pieces[Rnd.get(Pieces.length)], 1);
					if(Rnd.getChance(10))
					{
						st.giveItems(SpScrollLow, 1);
					}
					if(Rnd.getChance(20))
					{
						st.giveItems(SpScrollMedium, 1);
					}
					if(Rnd.get(100) < 35)
					{
						st.giveItems(SpScrollHigh, 1);
					}
					if(Rnd.get(100) < 35)
					{
						st.giveItems(SpScrollHighest, 1);
					}
					r = Rnd.get(100);
					if(r < 25) // 25%
					{
						st.giveItems(SpScrollLow, 1);
					}
					else if(r < 50) // 25%
					{
						st.giveItems(SpScrollMedium, 1);
					}
					else if(r < 75) // 25%
					{
						st.giveItems(SpScrollHigh, 1);
					}
					else // 25%
					{
						st.giveItems(SpScrollHighest, 1);
					}
					st.takeItems(Clothes, 500);
					break;
				default:
					return "32757-05a.htm";
			}
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			return "32757-05.htm";
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return super.onKill(npc, player, isPet);
		}
		if(st.getCond() == 1)
		{
			int chance = (int) (Drops.get(npc.getNpcId()) * Config.RATE_QUEST_DROP);
			int count = chance / 100;
			chance %= 100;
			if(count > 0)
			{
				if(Rnd.get(100) < chance)
				{
					count++;
				}
				st.giveItems(Clothes, count);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else if(Rnd.get(100) < chance)
			{
				st.giveItems(Clothes, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return htmltext;
		}
		int cond = st.getCond();
		switch(cond)
		{
			case 0:
				QuestState st1 = player.getQuestState(_10288_SecretMission.class);
				htmltext = st1 != null && st1.isCompleted() && player.getLevel() >= 82 ? "32757-00.htm" : "32757-00a.htm";
				break;
			case 1:
				htmltext = "32757-03.htm";
				break;
		}
		return htmltext;
	}
}
