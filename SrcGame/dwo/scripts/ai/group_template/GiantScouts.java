package dwo.scripts.ai.group_template;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.Say2;

public class GiantScouts extends Quest
{
	private static final int[] _scouts = {22668, 22669};

	public GiantScouts()
	{
		addAggroRangeEnterId(_scouts);
	}

	public static void main(String[] args)
	{
		new GiantScouts();
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2Character target = isPet ? player.getPets().getFirst() : player;

		if(GeoEngine.getInstance().canSeeTarget(npc, target))
		{
			if(!npc.isInCombat() && npc.getTarget() == null)
			{
				npc.broadcastPacket(new Say2(npc.getObjectId(), ChatType.SHOUT, npc.getName(), "Oh Giants, an intruder has been discovered."));
			}

			npc.setTarget(target);
			npc.setRunning();
			((L2Attackable) npc).addDamageHate(target, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);

			// Notify clan
			npc.getKnownList().getKnownObjects().values().stream().filter(obj -> obj != null).forEach(obj -> {
				if(obj instanceof L2MonsterInstance)
				{
					L2MonsterInstance monster = (L2MonsterInstance) obj;
					if(npc.getClan() != null && monster.getClan() != null && monster.getClan().equals(npc.getClan()) && GeoEngine.getInstance().canSeeTarget(npc, monster))
					{
						monster.setTarget(target);
						monster.setRunning();
						monster.addDamageHate(target, 0, 999);
						monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					}
				}
			});
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
}