package dwo.scripts.ai.group_template;

import dwo.gameserver.instancemanager.HellboundManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.taskmanager.manager.DecayTaskManager;

import java.util.List;

/**
 * @author DS
 */
public class Slaves extends Quest
{
	private static final int[] MASTERS = {
		22320, 22321
	};
	private static final Location MOVE_TO = new Location(-25451, 252291, -3252, 3500);
	private static final int TRUST_REWARD = 10;

	public Slaves()
	{
		addSpawnId(MASTERS);
		addKillId(MASTERS);
	}

	public static void main(String[] args)
	{
		new Slaves();
	}

	// Let's count trust points for killing in Engine
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(((L2MonsterInstance) npc).getMinionList() != null)
		{
			List<L2MonsterInstance> slaves = ((L2MonsterInstance) npc).getMinionList().getSpawnedMinions();
			if(slaves != null && !slaves.isEmpty())
			{
				for(L2MonsterInstance slave : slaves)
				{
					if(slave == null || slave.isDead())
					{
						continue;
					}

					slave.clearAggroList();
					slave.abortAttack();
					slave.abortCast();
					slave.broadcastPacket(new NS(slave.getObjectId(), ChatType.ALL, slave.getNpcId(), NpcStringId.THANK_YOU_FOR_SAVING_ME_FROM_THE_CLUTCHES_OF_EVIL));

					if(HellboundManager.getInstance().getLevel() >= 1 && HellboundManager.getInstance().getLevel() <= 2)
					{
						HellboundManager.getInstance().updateTrust(TRUST_REWARD, false);
					}

					slave.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO);
					DecayTaskManager.getInstance().addDecayTask(slave);
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		((L2MonsterInstance) npc).enableMinions(HellboundManager.getInstance().getLevel() < 5);
		((L2MonsterInstance) npc).setOnKillDelay(1000);

		return super.onSpawn(npc);
	}
}
