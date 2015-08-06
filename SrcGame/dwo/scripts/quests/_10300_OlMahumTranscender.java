package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import org.apache.commons.lang3.ArrayUtils;

public class _10300_OlMahumTranscender extends Quest
{
	private static final int[] Adventureguide = {
		31775, 31776, 31777, 31778, 31779, 31780, 31781, 31782, 31783, 31784, 31785, 31786, 31787, 31788, 31789, 31790,
		31791, 31792, 31793, 31794, 31795, 31796, 31797, 31798, 31799, 31824, 31805, 31800, 31801, 31802, 31803, 31804,
		31805, 31806, 31807, 31808, 31809, 31810, 31811, 31812, 31813, 31814, 31815, 31816, 31817, 31818, 31819, 31820,
		31821, 31822, 31823, 31824, 31825, 31826, 31827, 31828, 31829, 31830, 31831, 31832, 31833, 31834, 31835, 31836,
		31837, 31838, 31839, 31840, 31841, 31991, 31992, 31993, 31994, 31995, 32337, 32337, 32338, 32339, 32340, 33127,
		33385, 33386, 33387, 33388, 33389, 33390, 33391, 33392
	};

	private static final int mouen = 30196;

	private static final int[] Basilisk = {20573, 20574};
	private static final int[] gnols = {21261, 21262, 21263, 21264, 20241};
	private static final int[] OelMahum = {20575, 35428, 20576, 20161};

	private static final int markofbandit = 19484;
	private static final int markofshaman = 19485;
	private static final int proofmonstr = 19486;

	public _10300_OlMahumTranscender()
	{
		addStartNpc(Adventureguide);
		addTalkId(Adventureguide);
		addTalkId(mouen);
		addKillId(Basilisk);
		addKillId(gnols);
		addKillId(OelMahum);
		questItemIds = new int[]{markofbandit, markofshaman, proofmonstr};
	}

	public static void main(String[] args)
	{
		new _10300_OlMahumTranscender();
	}

	@Override
	public int getQuestId()
	{
		return 10300;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(event.equalsIgnoreCase("0-3.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("30196-01.htm"))
		{
			st.setCond(1);
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

		int npcId = npc.getNpcId();

		if(st.getCond() == 1)
		{
			if(ArrayUtils.contains(Basilisk, npcId) && st.getQuestItemsCount(markofbandit) < 30)
			{
				st.giveItems(markofbandit, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else if(ArrayUtils.contains(gnols, npcId) && st.getQuestItemsCount(markofshaman) < 30)
			{
				st.giveItems(markofshaman, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else if(ArrayUtils.contains(OelMahum, npcId) && st.getQuestItemsCount(proofmonstr) < 30)
			{
				st.giveItems(proofmonstr, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			if(st.getQuestItemsCount(markofbandit) >= 30 && st.getQuestItemsCount(markofshaman) >= 30 && st.getQuestItemsCount(proofmonstr) >= 30)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		int cond = st.getCond();
		int npcId = npc.getNpcId();

		if(ArrayUtils.contains(Adventureguide, npcId))
		{
			switch(st.getState())
			{
				case CREATED:
					if(st.getPlayer().getLevel() >= 50 && st.getPlayer().getLevel() <= 54)
					{
						return "start.htm";
					}
				case STARTED:
					if(cond == 1)
					{
						return "0-4.htm";
					}
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
			}
		}
		else if(npcId == mouen)
		{
			if(cond == 2)
			{
				st.takeItems(markofbandit, -1);
				st.takeItems(markofshaman, -1);
				st.takeItems(proofmonstr, -1);
				st.addExpAndSp(2046093, 1618470);
				st.giveAdena(329556, true);
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "30196-00.htm";
			}
			else
			{
				return "30196-01.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 50 && player.getLevel() < 54;

	}
}