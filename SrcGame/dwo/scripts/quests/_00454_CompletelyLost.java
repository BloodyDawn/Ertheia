package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

import java.util.Collection;

/**
 * L2-GodWorld Team
 * @author ANZO, Yukio
 * Date: 20.06.2013
 * Time: 22.22.22
 */
public class _00454_CompletelyLost extends Quest
{
	// Quest Npc
	private static final int INJURED_SOLDIER = 32738;
	private static final int ERMIAN = 32736;

	// Quest Rewards
	private static final int[][] REWARDS = {
		{15792, 1}, {15798, 1}, {15795, 1}, {15801, 1}, {15808, 1}, {15804, 1}, {15809, 1}, {15810, 1}, {15811, 1},
		{15660, 3}, {15666, 3}, {15663, 3}, {15667, 3}, {15669, 3}, {15668, 3}, {15769, 3}, {15770, 3}, {15771, 3},
		{15805, 1}, {15796, 1}, {15793, 1}, {15799, 1}, {15802, 1}, {15809, 1}, {15810, 1}, {15811, 1}, {15672, 3},
		{15664, 3}, {15661, 3}, {15670, 3}, {15671, 3}, {15769, 3}, {15770, 3}, {15771, 3}, {15800, 1}, {15803, 1},
		{15806, 1}, {15807, 1}, {15797, 1}, {15794, 1}, {15809, 1}, {15810, 1}, {15811, 1}, {15673, 3}, {15674, 3},
		{15675, 3}, {15691, 3}, {15665, 3}, {15662, 3}, {15769, 3}, {15770, 3}, {15771, 3}
	};

	public _00454_CompletelyLost()
	{
		addStartNpc(INJURED_SOLDIER);
		addTalkId(INJURED_SOLDIER, ERMIAN);
	}

	public static void main(String[] args)
	{
		new _00454_CompletelyLost();
	}

	@Override
	public int getQuestId()
	{
		return 454;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			qs.startQuest();
			qs.set("npcFollow", "0");
			return "wunded_gracia_soldier_q0454_04.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int cond = st.getCond();
		switch(npc.getNpcId())
		{
			case INJURED_SOLDIER:
				if(reply == 11 && cond == 1)
				{
					if(seeSoldier(npc, player) == null)
					{
						st.set("npcFollow", "1");
						npc.getAI().startFollow(player);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
						return "wunded_gracia_soldier_q0454_06.htm";
					}
				}
				if(reply == 2)
				{
					return "wunded_gracia_soldier_q0454_07.htm";
				}
				break;
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(st == null)
		{
			return null;
		}

		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == INJURED_SOLDIER)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 85)
					{
						return "wunded_gracia_soldier_q0454_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "wunded_gracia_soldier_q0454_03.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return st.getInt("npcFollow") == 0 ? "wunded_gracia_soldier_q0454_05.htm" : "wunded_gracia_soldier_q0454_08.htm";
					}
					break;
				case COMPLETED:
					if(st.isNowAvailable())
					{
						if(player.getLevel() >= 85)
						{
							st.setState(CREATED);
							return "wounded_gracia_soldier_q454_01.htm";
						}
						else
						{
							return "wunded_gracia_soldier_q0454_03.htm";
						}
					}
					else
					{
						return "wunded_gracia_soldier_q0454_02.htm";
					}
			}
		}
		else if(npc.getNpcId() == ERMIAN)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 1)
				{
					L2Npc soldier = seeSoldier(npc, player);
					if(soldier != null)
					{
						soldier.doDie(null);
						soldier.endDecayTask();
						giveReward(st);
						st.setState(COMPLETED);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.DAILY);
						return "ermian_q0454_03.htm";
					}
					else
					{
						return "ermian_q0454_01.htm";
					}
				}
			}
			else if(st.isCompleted())
			{
				return "ermian_q0454_04.htm";
			}
		}

		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 85;
	}

	private L2Npc seeSoldier(L2Npc npc, L2PcInstance player)
	{
		Collection<L2Npc> around = npc.getKnownList().getKnownNpcInRadius(200);
		if(around != null && !around.isEmpty())
		{
			for(L2Npc n : around)
			{
				if(n.getNpcId() == INJURED_SOLDIER && n.getAI().getFollowTarget() != null)
				{
					if(n.getAI().getFollowTarget().getObjectId() == player.getObjectId())
					{
						return n;
					}
				}
			}
		}
		return null;
	}

	private void giveReward(QuestState st)
	{
		int row = Rnd.get(0, REWARDS.length - 1);
		st.giveItems(REWARDS[row][0], REWARDS[row][1]);
	}
}
