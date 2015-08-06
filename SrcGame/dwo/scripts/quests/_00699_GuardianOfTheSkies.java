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
 * User: ANZO
 * Date: 24.08.11
 * Time: 13:40
 */

public class _00699_GuardianOfTheSkies extends Quest
{
	// Квестовые персонажи
	private static final int LEKON = 32557;

	// Квестовые предметы
	private static final int GOLDEN_FEATHER = 13871;

	// Квестовые монстры
	private static final int[] MOBS = {22614, 22615, 25623, 25633};

	public _00699_GuardianOfTheSkies()
	{
		addStartNpc(LEKON);
		addTalkId(LEKON);
		addKillId(MOBS);
		questItemIds = new int[]{GOLDEN_FEATHER};
	}

	public static void main(String[] args)
	{
		new _00699_GuardianOfTheSkies();
	}

	@Override
	public int getQuestId()
	{
		return 699;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("32557-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equals("32557-quit.htm"))
		{
			st.unset("cond");
			st.exitQuest(QuestType.REPEATABLE);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
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
		if(Rnd.getChance(80) && st.getCond() == 1 && ArrayUtils.contains(MOBS, npc.getNpcId()))
		{
			st.giveItems(GOLDEN_FEATHER, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		String htmltext = getNoQuestMsg(st.getPlayer());
		int cond = st.getCond();

		if(npc.getNpcId() == LEKON)
		{
			QuestState first = st.getPlayer().getQuestState(_10273_GoodDayToFly.class);
			if(first != null && first.getState() == COMPLETED && st.getState() == CREATED && st.getPlayer().getLevel() >= 75)
			{
				htmltext = "32557-01.htm";
			}
			else if(cond == 1)
			{
				long itemcount = st.getQuestItemsCount(GOLDEN_FEATHER);
				if(itemcount > 0)
				{
					st.takeItems(GOLDEN_FEATHER, -1);
					st.giveAdena(itemcount * 2300, true);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					htmltext = "32557-06.htm";
				}
				else
				{
					htmltext = "32557-04.htm";
				}
			}
		}
		return htmltext;
	}
}
