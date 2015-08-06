package dwo.scripts.ai.zone;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 ** @author Gnacik
 */
public class PavelArchaic extends Quest
{
	private static final int[] _mobs1 = {22801, 22804};
	private static final int[] _mobs2 = {18917};

	public PavelArchaic()
	{
		addKillId(_mobs1);
		addAttackId(_mobs2);
	}

	public static void main(String[] args)
	{
		new PavelArchaic();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(!npc.isDead() && ArrayUtils.contains(_mobs2, npc.getNpcId()))
		{
			npc.doDie(attacker);

			if(Rnd.getChance(40))
			{
				L2Attackable _golem1 = (L2Attackable) addSpawn(22801, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
				_golem1.attackCharacter(attacker);

				L2Attackable _golem2 = (L2Attackable) addSpawn(22804, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
				_golem2.attackCharacter(attacker);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(ArrayUtils.contains(_mobs1, npc.getNpcId()))
		{
			L2Attackable _golem = (L2Attackable) addSpawn(npc.getNpcId() + 1, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
			_golem.attackCharacter(killer);
		}
		return super.onKill(npc, killer, isPet);
	}
}