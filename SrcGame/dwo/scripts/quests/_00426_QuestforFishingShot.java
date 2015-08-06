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
 * Date: 16.08.12
 * Time: 5:15
 */

public class _00426_QuestforFishingShot extends Quest
{
	// Квестовые предметы
	private static final int SWEET_FLUID = 7586;

	// Квестовые монстры
	private static int[] MOBS = {
		20005, 20013, 20016, 20017, 20024, 20025, 20043, 20044, 20046, 20047, 20050, 20058, 20063, 20066, 20070, 20074,
		20077, 20078, 20079, 20080, 20081, 20082, 20083, 20084, 20085, 20088, 20089, 20100, 20106, 20115, 20120, 20131,
		20132, 20135, 20157, 20162, 20176, 20225, 20227, 20230, 20232, 20234, 20241, 20267, 20268, 20269, 20270, 20271,
		20308, 20312, 20317, 20324, 20333, 20341, 20345, 20346, 20349, 20350, 20356, 20357, 20363, 20368, 20386, 20389,
		20403, 20404, 20433, 20448, 20456, 20463, 20470, 20471, 20475, 20476, 20511, 20525, 20528, 20536, 20537, 20538,
		20539, 20544, 20547, 20550, 20551, 20552, 20553, 20554, 20555, 20557, 20559, 20560, 20562, 20573, 20575, 20576,
		20630, 20632, 20634, 20636, 20641, 20643, 20644, 20646, 20648, 20650, 20651, 20659, 20661, 20652, 20656, 20655,
		20657, 20658, 20663, 20665, 20667, 20781, 20772, 20783, 20784, 20786, 20788, 20790, 20791, 20792, 20794, 20796,
		20798, 20800, 20802, 20804, 20808, 20809, 20810, 20811, 20812, 20814, 20815, 20816, 20819, 20822, 20824, 20825,
		20828, 20829, 20830, 20833, 20834, 20836, 20837, 20839, 20841, 20843, 20845, 20847, 20849, 20936, 20938, 20939,
		20944, 20943, 20940, 20941, 20978, 20979, 20983, 20985, 20991, 20994, 21023, 21024, 21025, 21026, 21058, 21060,
		21061, 21066, 21067, 21070, 21072, 21075, 21078, 21081, 21100, 21101, 21102, 21103, 21104, 21105, 21106, 21107,
		21117, 21125, 21261, 21269, 21271, 21272, 21273, 21314, 21316, 21318, 21320, 21322, 22634, 22636, 22638, 22640,
		22641, 22644, 22646, 22649, 21508, 21510, 21511, 21514, 21516, 21518, 21520, 21523, 21526, 21529, 21530, 21531,
		21536, 21532, 21542, 21543, 21544
	};
	private static int[] HMOBS = {
		20651, 20652, 20655, 20656, 20657, 20658, 20772, 20809, 20810, 20811, 20812, 20814, 20815, 20816, 20819, 20822,
		20824, 20825, 20828, 20829, 20830, 20978, 20979, 20983, 20985, 21058, 21061, 21066, 21067, 21070, 21072, 21075,
		21078, 21081, 21314, 21316, 21318, 21320, 21322, 21376, 21378, 21380, 21382, 21384, 21387, 21390, 21393, 21395,
		21508, 21510, 21511, 21514, 21516, 21518, 21520, 21523, 21526, 21529, 21530, 21531, 21532, 21536, 21542, 21543,
		21544
	};

	public _00426_QuestforFishingShot()
	{
		for(int npcId = 31562; npcId <= 31579; npcId++)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
		addStartNpc(31696, 31697, 31989, 32007, 32348);
		addTalkId(31696, 31697, 31989, 32007, 32348);
		addKillId(MOBS);
	}

	public static void main(String[] args)
	{
		new _00426_QuestforFishingShot();
	}

	@Override
	public int getQuestId()
	{
		return 426;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			qs.startQuest();
			return "fisher_berix_q0426_03.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(st.isStarted())
		{
			if(reply == 1)
			{
				return "fisher_berix_q0426_06.htm";
			}
			else if(reply == 2)
			{
				return "fisher_berix_q0426_07.htm";
			}
			else if(reply == 3)
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "fisher_berix_q0426_08.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(Rnd.getChance(30))
		{
			if(ArrayUtils.contains(HMOBS, npcId))
			{
				st.giveItems(SWEET_FLUID, Rnd.get(5) + 1);
			}
			else
			{
				st.giveItems(SWEET_FLUID, Rnd.get(3) + 1);
			}
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		switch(st.getState())
		{
			case CREATED:
				return "fisher_berix_q0426_01.htm";
			case STARTED:
				return !st.hasQuestItems(SWEET_FLUID) ? "fisher_berix_q0426_04.htm" : "fisher_berix_q0426_05.htm";
		}
		return null;
	}
}