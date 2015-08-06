package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Util;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 05.09.12
 * Time: 19:06
 */

public class _00943_FillingtheEnergyofDestruction extends Quest
{
	// NPCs
	private static final int SEED_TALISMAN_MANAGER = 33715;

	// MOBs
	private static final int[] RAID_BOSSES = {
		29195, 29196, 29212, 29194, 25779, 25867, 29213, 29218, 25825, 29236, 29238
	};

	// ITEMs
	private static final int CORE_OF_TWISTED_MAGIC = 35668;
	private static final int ENERGY_OF_DESTRUCTION = 35562;

	public _00943_FillingtheEnergyofDestruction()
	{
		addStartNpc(SEED_TALISMAN_MANAGER);
		addTalkId(SEED_TALISMAN_MANAGER);
		addKillId(RAID_BOSSES);
		questItemIds = new int[]{CORE_OF_TWISTED_MAGIC};
	}

	public static void main(String[] args)
	{
		new _00943_FillingtheEnergyofDestruction();
	}

	@Override
	public int getQuestId()
	{
		return 943;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "seed_talisman_manager_q0943_03.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == SEED_TALISMAN_MANAGER)
		{
			if(reply == 1)
			{
				return "seed_talisman_manager_q0943_02.htm";
			}
			else if(reply == 2 && cond == 2)
			{
				st.giveItem(ENERGY_OF_DESTRUCTION);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.DAILY);
				return "seed_talisman_manager_q0943_08.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		executeForEachPlayer(killer, npc, isSummon, true, true);
		return super.onKill(npc, killer, isSummon);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == SEED_TALISMAN_MANAGER)
		{
			if(st.isNowAvailable() && st.isCompleted())
			{
				st.setState(CREATED);
			}

			switch(st.getState())
			{
				case COMPLETED:
					return "seed_talisman_manager_q0943_05.htm";
				case CREATED:
					return st.getPlayer().getLevel() < 90 ? "seed_talisman_manager_q0943_04.htm" : "seed_talisman_manager_q0943_01.htm";
				case STARTED:
					if(cond == 1)
					{
						return "seed_talisman_manager_q0943_06.htm";
					}
					else if(cond == 2)
					{
						return "seed_talisman_manager_q0943_07.htm";
					}
			}
		}

		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 90;
	}

	@Override
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.getCond() == 1 && Util.checkIfInRange(1500, npc, player, false))
		{
			st.giveItem(CORE_OF_TWISTED_MAGIC);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			st.setCond(2);
		}
	}
}