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
 * Date: 18.06.13
 * Time: 20:28
 */
public class _10431_TheSealofPunishmentDenofEvil extends Quest
{
	// Квестовые персонажи
	private static final int JOKEL = 33868;
	private static final int CHEIREN = 32655;

	// Квестовые предметы
	private static final int EVIL_SOUL = 36715;

	// Квестовые монстры
	private static final int[] MOBS = {22029, 22032, 22038, 22039, 22695, 22696, 22697, 22698, 22699, 22700};

	// Квестовая награда
	private static final int IRON_GATE_COIN = 37045;

	private static final int[] CLASS_LIMITS = {88, 90, 91, 93, 99, 100, 101, 106, 107, 108, 114, 131, 132, 133, 136};

	public _10431_TheSealofPunishmentDenofEvil()
	{
		addStartNpc(JOKEL);
		addTalkId(JOKEL, CHEIREN);
		addKillId(MOBS);
		questItemIds = new int[]{EVIL_SOUL};
	}

	public static void main(String[] args)
	{
		new _10431_TheSealofPunishmentDenofEvil();
	}

	@Override
	public int getQuestId()
	{
		return 10431;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "yokel_q10431_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == JOKEL)
		{
			if(reply == 1)
			{
				return "yokel_q10431_03.htm";
			}
			else if(reply == 2)
			{
				return "yokel_q10431_04.htm";
			}
		}
		else if(npc.getNpcId() == CHEIREN)
		{
			if(reply == 1 && st.getCond() == 1)
			{
				return "cheiren_q10431_02.htm";
			}
			else if(reply == 2 && st.getCond() == 1)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "cheiren_q10431_03.htm";
			}
			else if(reply == 3 && st.getCond() == 3)
			{
				st.addExpAndSp(28240800, 282408);
				st.giveItems(IRON_GATE_COIN, 60);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "cheiren_q10431_06.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());
		if(st != null && st.getCond() == 2 && Rnd.getChance(50))
		{
			st.giveItem(EVIL_SOUL);
			if(st.getQuestItemsCount(EVIL_SOUL) >= 50)
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == JOKEL)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
				case CREATED:
					if(player.getLevel() >= 81 && player.getLevel() <= 84 && ArrayUtils.contains(CLASS_LIMITS, player.getActiveClassId()))
					{
						return "yokel_q10431_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "yokel_q10431_02.htm";
					}
				case STARTED:
					return "yokel_q10431_06.htm";
			}
		}
		else if(npc.getNpcId() == CHEIREN)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 1)
				{
					return "cheiren_q10431_01.htm";
				}
				else if(st.getCond() == 2)
				{
					return "cheiren_q10431_04.htm";
				}
				else if(st.getCond() == 3)
				{
					return "cheiren_q10431_05.htm";
				}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 81 && player.getLevel() <= 84 && ArrayUtils.contains(CLASS_LIMITS, player.getActiveClassId());

	}
}