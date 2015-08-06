package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.stat.PcStat;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.01.13
 * Time: 16:52
 */

public class _00757_TriolsMovement extends Quest
{
	// Квестовые персонажи
	private static final int Razen = 33803;

	// Квестовые предметы
	private static final int Totem = 36230;
	private static final int Spirit = 36231;

	// Квестовые награды
	private static final int DivineBox = 36232;
	private static final int PaganBlood = 36278;

	// Квестовые монстры
	private static final int[] Mobs = {
		22139, 22140, 22141, 22147, 22149, 22145, 22154, 22161, 22169, 22172, 22190, 22195, 22144, 22143, 22148, 22150,
		22158, 22162, 22164, 22166, 22170, 22142, 22155, 22159, 22163, 22167, 22171, 19409, 19410
	};

	public _00757_TriolsMovement()
	{
		addStartNpc(Razen);
		addTalkId(Razen);
		addKillId(Mobs);
		questItemIds = new int[]{Totem, Spirit};
	}

	public static void main(String[] args)
	{
		new _00757_TriolsMovement();
	}

	@Override
	public int getQuestId()
	{
		return 757;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "razen_q0757_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int cond = st.getCond();

		switch(reply)
		{
			case 1:
				return "razen_q0757_04.htm";
			case 2:
				return "razen_q0757_05.htm";
			case 10:
				st.exitQuest(QuestType.DAILY);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return null;
			case 20:
				return cond == 2 ? "razen_q0757_10.htm" : null;
			case 11:
				if(cond == 2)
				{
					st.giveItem(DivineBox);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.DAILY);
					return "razen_q0757_11.htm";
				}
				else
				{
					return null;
				}
			case 12:
				if(cond == 2)
				{
					st.giveItems(PaganBlood, 10);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.DAILY);
					return "razen_q0757_12.htm";
				}
				else
				{
					return null;
				}
			case 13:
				return "razen_q0757_13.htm";
			case 31:
				if(cond == 3)
				{
					st.giveItem(DivineBox);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.DAILY);
					player.setVitalityPoints(PcStat.MAX_VITALITY_POINTS);
					return "razen_q0757_18.htm";
				}
				else
				{
					return null;
				}
			case 32:
				if(cond == 3)
				{
					st.giveItems(PaganBlood, 10);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.DAILY);
					player.setVitalityPoints(PcStat.MAX_VITALITY_POINTS);
					return "razen_q0757_19.htm";
				}
				else
				{
					return null;
				}
		}

		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if(ArrayUtils.contains(Mobs, npc.getNpcId()))
		{
			executeForEachPlayer(killer, npc, isSummon, true, true);
		}
		return super.onKill(npc, killer, isSummon);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npcId == Razen)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "razen_q0757_03.htm";
				case CREATED:
					return st.getPlayer().getLevel() < 95 ? "razen_q0757_02.htm" : "razen_q0757_01.htm";
				case STARTED:
					if(cond == 1)
					{
						return "razen_q0757_07.htm";
					}
					else if(cond == 2)
					{
						if(st.getQuestItemsCount(Totem) >= 50)
						{
							return st.getQuestItemsCount(Spirit) == 0 ? "razen_q0757_08.htm" : "razen_q0757_09.htm";
						}
					}
					else if(cond == 3)
					{
						return st.getQuestItemsCount(Totem) >= 50 && st.getQuestItemsCount(Spirit) >= 1200 ? "razen_q0757_17.htm" : "razen_q0757_09.htm";
					}
			}
		}

		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 97;
	}

	@Override
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && Util.checkIfInRange(1500, npc, player, false))
		{
			if(st.getCond() == 1)
			{
				if(Rnd.getChance(50))
				{
					if(st.getQuestItemsCount(Totem) < 50)
					{
						st.giveItem(Totem);
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
				else
				{
					if(st.getQuestItemsCount(Spirit) < 1)
					{
						st.giveItem(Spirit);
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
				if(st.getQuestItemsCount(Totem) >= 50 && st.getQuestItemsCount(Spirit) >= 1)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
			else if(st.getCond() == 2)
			{
				if(Rnd.getChance(75))
				{
					st.giveItem(Spirit);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					if(st.getQuestItemsCount(Spirit) >= 1200)
					{
						st.setCond(3);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
			}
		}
	}
}