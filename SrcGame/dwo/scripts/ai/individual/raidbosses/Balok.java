package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import dwo.scripts.instances.RB_Baylor;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class Balok extends Quest
{
	private static final int BALOK = 29218;

	private static final SkillHolder INVINCIBILITY_ACTIVATION = new SkillHolder(14190, 1);

	private static final int[][] PRISON_COORDS = {
		{153584, 140349, -12704, 16384}, {155084, 141214, -12704, 28672}, {155312, 142080, -12704, 32768},
		{152704, 143600, -12704, 53248}, {154448, 143584, -12704, 45056}, {154435, 140601, -12704, 20480},
		{151805, 142085, -12704, 0}, {152064, 141190, -12704, 7000},
	};

	public Balok()
	{
		addAttackId(BALOK);
	}

	public static void main(String[] args)
	{
		new Balok();
	}

	/**
	 * Включаем УД.
	 * @param npc
	 */
	private void ultimateDefense(L2Npc npc)
	{
		RB_Baylor.BaylorWorld world = InstanceManager.getInstance().getInstanceWorld(npc, RB_Baylor.BaylorWorld.class);

		// УД включается только у второго Байлора
		if(world != null && world.killedBailors == 1 && Rnd.get() <= 0.0001)
		{
			npc.doCast(INVINCIBILITY_ACTIVATION.getSkill());
		}
	}

	/**
	 * Сажаем игрока в тюрьму.
	 * @param npc
	 */
	private void imprison(L2Npc npc)
	{
		RB_Baylor.BaylorWorld world = null;
		InstanceManager.getInstance().getInstanceWorld(npc, null);

		if(world != null)
		{
			L2PcInstance unlucky = world.playersInside.get(Rnd.get(world.playersInside.size()));
			int[] coords = PRISON_COORDS[Rnd.get(PRISON_COORDS.length)];
			unlucky.teleToInstance(new Location(coords[0], coords[1], coords[2], coords[3]), world.instanceId);
		}
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		synchronized(this)
		{
			double percentHp = npc.getCurrentHp() / npc.getMaxHp();

			if(percentHp < 0.15)
			{
				ultimateDefense(npc);
			}
			else if(Rnd.get() <= 0.00005)
			{
				imprison(npc);
			}
			return null;
		}
	}
}