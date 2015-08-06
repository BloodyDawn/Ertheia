package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.L2AttackableAI;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.effects.L2Effect;
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

public class Baylor extends Quest
{
	private static final int BAYLOR = 29213;
	private static final int PRISON_GATE_KEY = 10015;
	private static final SkillHolder IMPRESION = new SkillHolder(5225, 1);
	private static final SkillHolder EXPOSE_WIKNESS1 = new SkillHolder(14511, 1);
	private static final SkillHolder EXPOSE_WIKNESS2 = new SkillHolder(14512, 1);
	private static final int BAYLOR_ATTACKER = 29215;
	private static final int[][] PRISON_COORDS = {
		{153584, 140349, -12704, 16384}, {155084, 141214, -12704, 28672}, {155312, 142080, -12704, 32768},
		{152704, 143600, -12704, 53248}, {154448, 143584, -12704, 45056}, {154435, 140601, -12704, 20480},
		{151805, 142085, -12704, 0}, {152064, 141190, -12704, 7000},
	};

	public Baylor()
	{
		addSpawnId(BAYLOR);
		addAttackId(BAYLOR);
	}

	public static void main(String[] args)
	{
		new Baylor();
	}

	/**
	 * Включаем УД.
	 * @param npc
	 */
	private void ultimateDefense(L2Npc npc)
	{
		RB_Baylor.BaylorWorld world = InstanceManager.getInstance().getInstanceWorld(npc, RB_Baylor.BaylorWorld.class);

		// УД включается только у второго Байлора
		if(world != null && world.killedBailors == 1 && Rnd.get() <= 0.01)
		{
			npc.doCast(IMPRESION.getSkill());
		}
	}

	/**
	 * Сажаем игрока в тюрьму.
	 * @param npc
	 */
	private void imprison(L2Npc npc)
	{
		RB_Baylor.BaylorWorld world = InstanceManager.getInstance().getInstanceWorld(npc, RB_Baylor.BaylorWorld.class);

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

				if(Rnd.get() <= 0.001)
				{
					L2ItemInstance item = ItemTable.getInstance().createItem(ProcessType.QUEST, PRISON_GATE_KEY, 1, attacker);
					item.dropMe(npc, npc.getX(), npc.getY(), npc.getZ());
				}
			}
			else if(attacker.isBehindTarget() && Rnd.get() <= 0.001)
			{
				if(Rnd.get() <= 0.5)
				{
					EXPOSE_WIKNESS1.getSkill().getEffects(npc, npc);
				}
				else
				{
					EXPOSE_WIKNESS2.getSkill().getEffects(npc, npc);
				}
			}
			else if(Rnd.get() <= 0.000005)
			{
				imprison(npc);
			}

			return null;
		}
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(npc.getNpcId() == BAYLOR)
		{
			npc.setAI(new BaylorAI(npc.getAI().getAccessor()));
		}
		return null;
	}

	public class BaylorAI extends L2AttackableAI
	{
		public BaylorAI(L2Character.AIAccessor accessor)
		{
			super(accessor);
		}

		@Override
		protected void onEvtAttacked(L2Character attacker)
		{
			if(attacker != null && attacker.isNpc() && attacker.getNpcInstance().getNpcId() == BAYLOR_ATTACKER && Rnd.get() <= 0.5)
			{
				for(L2Effect effect : getActor().getAllEffects())
				{
					if(effect != null && effect.getSkill().getId() == IMPRESION.getSkillId())
					{
						effect.exit();
					}
				}
			}

			super.onEvtAttacked(attacker);
		}
	}
}