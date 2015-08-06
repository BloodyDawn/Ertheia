package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;

public class TimakOrcTroopLeader extends Quest
{
	public TimakOrcTroopLeader()
	{
		addAttackId(20767);
	}

	public static void main(String[] args)
	{
		new TimakOrcTroopLeader();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		int objId = npc.getObjectId();
		if(npc.getAiVar("firstAttacked") != null)
		{
			if(Rnd.getChance(40))
			{
				npc.broadcastPacket(new NS(objId, ChatType.ALL, npc.getNpcId(), NpcStringId.DESTROY_THE_ENEMY_MY_BROTHERS));
			}
		}
		else
		{
			npc.setAiVar("firstAttacked", 1);
		}
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}
}