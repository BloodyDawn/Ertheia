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
 * Date: 03.11.12
 * Time: 19:37
 */

public class _00371_ShriekOfGhosts extends Quest
{
	// Квестовые персонажи
	private static final int PATRIN = 30929;
	private static final int REVA = 30867;

	// Квестовые предметы
	private static final int URN = 5903;
	private static final int PORCELAIN = 6002;

	private static final int EXCELLENT_PORCELAIN = 6003;
	private static final int GOOD_PORCELAIN = 6004;
	private static final int LOW_PORCELAIN = 6005;
	private static final int UGLE_PORCELAIN = 6006;

	// Квестовые мобы
	private static final int[] MOBs = {20818, 20820, 20824};

	public _00371_ShriekOfGhosts()
	{
		addStartNpc(REVA);
		addTalkId(REVA, PATRIN);
		addKillId(MOBs);
		questItemIds = new int[]{URN, PORCELAIN};
	}

	public static void main(String[] args)
	{
		new _00371_ShriekOfGhosts();
	}

	private void calculateQuestDrop(QuestState state, L2Npc npc)
	{
		int chance = Rnd.get(1000);
		switch(npc.getNpcId())
		{
			case 20818:
				if(chance < 350)
				{
					state.giveItem(URN);
					state.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				else if(chance < 400)
				{
					state.giveItem(PORCELAIN);
					state.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				break;
			case 20820:
				if(chance < 583)
				{
					state.giveItem(URN);
					state.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				else if(chance < 673)
				{
					state.giveItem(PORCELAIN);
					state.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				break;
			case 20824:
				if(chance < 458)
				{
					state.giveItem(URN);
					state.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				else if(chance < 538)
				{
					state.giveItem(PORCELAIN);
					state.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				break;
		}
	}

	@Override
	public int getQuestId()
	{
		return 371;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			qs.startQuest();
			return "seer_reva_q0371_03.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == REVA)
		{
			switch(reply)
			{
				case 1:
					if(!st.hasQuestItems(URN))
					{
						return "seer_reva_q0371_06.htm";
					}
					if(st.hasQuestItems(URN) && st.getQuestItemsCount(URN) < 100)
					{
						st.giveAdena(st.getQuestItemsCount(URN) * 1000 + 15000, true);
						st.takeItems(URN, -1);
						return "seer_reva_q0371_07.htm";
					}
					if(st.getQuestItemsCount(URN) >= 100)
					{
						st.giveAdena(st.getQuestItemsCount(URN) * 1000 + 37700, true);
						st.takeItems(URN, -1);
						return "seer_reva_q0371_08.htm";
					}
				case 2:
					return "seer_reva_q0371_09.htm";
				case 3:
					if(st.hasQuestItems(URN))
					{
						st.giveAdena(st.getQuestItemsCount(URN) * 1000, true);
						st.takeItems(URN, -1);
					}
					st.exitQuest(QuestType.REPEATABLE);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return "seer_reva_q0371_10.htm";
			}
		}
		else if(npc.getNpcId() == PATRIN)
		{
			if(reply == 1)
			{
				if(st.hasQuestItems(PORCELAIN))
				{
					int chance = Rnd.get(100);
					if(chance < 2)
					{
						st.giveItem(EXCELLENT_PORCELAIN);
						st.takeItems(PORCELAIN, 1);
						return "patrin_q0371_03.htm";
					}
					else if(chance < 32)
					{
						st.giveItem(GOOD_PORCELAIN);
						st.takeItems(PORCELAIN, 1);
						return "patrin_q0371_04.htm";
					}
					else if(chance < 62)
					{
						st.giveItem(LOW_PORCELAIN);
						st.takeItems(PORCELAIN, 1);
						return "patrin_q0371_05.htm";
					}
					else if(chance < 77)
					{
						st.giveItem(UGLE_PORCELAIN);
						st.takeItems(PORCELAIN, 1);
						return "patrin_q0371_06.htm";
					}
					else
					{
						st.takeItems(PORCELAIN, 1);
						return "patrin_q0371_07.htm";
					}
				}
				else
				{
					return "patrin_q0371_02.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(ArrayUtils.contains(MOBs, npc.getNpcId()))
		{
			if(killer.getParty() != null)
			{
				for(L2PcInstance pMember : killer.getParty().getMembersInRadius(killer, 1500))
				{
					st = pMember.getQuestState(getClass());
					if(st == null || !st.isStarted())
					{
					}
					else
					{
						calculateQuestDrop(st, npc);
					}
				}
			}
			else
			{
				calculateQuestDrop(st, npc);
			}
		}

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == REVA)
		{
			switch(st.getState())
			{
				case COMPLETED:
				case CREATED:
					if(player.getLevel() < 59)
					{
						return "seer_reva_q0371_01.htm";
					}
					if(player.getLevel() >= 52)
					{
						return "seer_reva_q0371_02.htm";
					}
				case STARTED:
					return !st.hasQuestItems(PORCELAIN) ? "seer_reva_q0371_04.htm" : "seer_reva_q0371_05.htm";
			}
		}
		else if(npc.getNpcId() == PATRIN)
		{
			if(st.isStarted())
			{
				return "patrin_q0371_01.htm";
			}
		}
		return getNoQuestMsg(player);
	}
}