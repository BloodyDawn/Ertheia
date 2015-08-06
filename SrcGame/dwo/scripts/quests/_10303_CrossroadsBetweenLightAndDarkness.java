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
 * @author ANZO
 * Date: 22.04.12
 * Time: 15:56
 */

public class _10303_CrossroadsBetweenLightAndDarkness extends Quest
{
	// Квестовые персонажи
	private static final int Йона = 32909;
	private static final int Жрец = 33343;

	// Квестовые предметы
	private static final int СледыРастлевающейТьмы = 17747;
	private static final int СледыРастлевающейТьмыКвест = 17820;

	// Квестовые награды
	private static final int[] НаградыЙоны = {13505, 16108, 16102, 16105};
	private static final int[] НаградыЖреца = {16101, 16100, 16099, 16098};

	public _10303_CrossroadsBetweenLightAndDarkness()
	{
		addTalkId(Йона, Жрец);
		questItemIds = new int[]{СледыРастлевающейТьмыКвест};
	}

	public static void main(String[] args)
	{
		new _10303_CrossroadsBetweenLightAndDarkness();
	}

	@Override
	public int getQuestId()
	{
		return 10303;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Йона)
		{
			if(reply == 1 && cond == 1)
			{
				return "yona_cat_q10303_04.htm";
			}
			else if(reply == 2 && cond == 1)
			{
				st.giveItem(НаградыЙоны[Rnd.get(НаградыЙоны.length)]);
				st.addExpAndSp(6730155, 2847330);
				st.giveAdena(465855, true);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "yona_cat_q10303_05.htm";
			}
			else if(reply == 3 && cond == 1)
			{
				return "yona_cat_q10303_06.htm";
			}
		}
		else if(npcId == Жрец)
		{
			if(reply == 1 && cond == 1)
			{
				return "magmeld_silen_priest_q10303_04.htm";
			}
			else if(reply == 2 && cond == 1)
			{
				st.giveItem(НаградыЖреца[Rnd.get(НаградыЖреца.length)]);
				st.addExpAndSp(6730155, 2847330);
				st.giveAdena(465855, true);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "magmeld_silen_priest_q10303_05.htm";
			}
			else if(reply == 3 && cond == 1)
			{
				return "magmeld_silen_priest_q10303_06.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		L2PcInstance player = st.getPlayer();

		if(npcId == Йона)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "yona_cat_q10303_02.htm";
				case STARTED:
					if(cond == 1)
					{
						return player.getLevel() < 90 ? "yona_cat_q10303_03.htm" : "yona_cat_q10303_01.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == Жрец)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "magmeld_silen_priest_q10303_02.htm";
				case STARTED:
					if(cond == 1)
					{
						return player.getLevel() < 90 ? "magmeld_silen_priest_q10303_03.htm" : "magmeld_silen_priest_q10303_01.htm";
					}
					break;
			}
		}
		return getNoQuestMsg(player);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 90;

	}

	@Override
	public String onStartFromItem(L2PcInstance player)
	{
		if(player.getLevel() < 90)
		{
			return "q10303_signs_of_rotting_darkness_q10303_04.htm";
		}

		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			st = newQuestState(player);
		}
		else if(st.isCompleted())
		{
			return "q10303_signs_of_rotting_darkness_q10303_03.htm";
		}

		st.startQuest();
		st.takeItems(СледыРастлевающейТьмы, 1);
		st.giveItem(СледыРастлевающейТьмыКвест);
		return "q10303_signs_of_rotting_darkness_q10303_02.htm";
	}
}