package dwo.scripts.ai.group_template;

import dwo.config.scripts.ConfigChaosFestival;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;
import dwo.scripts.instances.ChaosFestival;

/**
 * Monsters that are spawned during Chaos Festival match.
 *
 * @author Yorie
 */
public class ChaosFestivalMobs extends Quest
{
	public static final int HERB_BOX = 19292;
	public static final int MYST_BOX = 19267;
	public static final int HERB_OF_HEALTH = 35983;
	public static final int HERB_OF_POWER = 35984;
	public static final int MYST_SIGN = 34900;

	public ChaosFestivalMobs()
	{
		addAttackId(HERB_BOX, MYST_BOX);
		addSpawnId(HERB_BOX, MYST_BOX);
	}

	public static void main(String[] args)
	{
		new ChaosFestivalMobs();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		ChaosFestival.ChaosFestivalWorld world = InstanceManager.getInstance().getInstanceWorld(npc, ChaosFestival.ChaosFestivalWorld.class);

		if(world == null)
		{
			return null;
		}

		switch(npc.getNpcId())
		{
			case HERB_BOX:
				npc.doDie(attacker);

				ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					// Настойка Восстановления HP
					((L2MonsterInstance) npc).dropItem(attacker, new ItemHolder(HERB_OF_HEALTH, 1));

					// Настойка силы, +100% к атаке, +400% к магической атаке
					if(Rnd.get() <= 0.05 || attacker.isGM())
					{
						((L2MonsterInstance) npc).dropItem(attacker, new ItemHolder(HERB_OF_POWER, 1));
					}
				}, 3000);
				break;
			case MYST_BOX:
				npc.doDie(attacker);

				ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					// Таинственные знаки
					int signsCount = Rnd.get(ConfigChaosFestival.CHAOS_FESTIVAL_MYST_BOX_SIGNS_MIN_COUNT, ConfigChaosFestival.CHAOS_FESTIVAL_MYST_BOX_SIGNS_MAX_COUNT);
					((L2MonsterInstance) npc).dropItem(attacker, new ItemHolder(MYST_SIGN, signsCount));

					// Случайная награда
					if(ConfigChaosFestival.CHAOS_FESTIVAL_MYST_BOX_RANDOM_REWARDS != null && !ConfigChaosFestival.CHAOS_FESTIVAL_MYST_BOX_RANDOM_REWARDS.isEmpty())
					{
						double chance = Rnd.get();
						int selectedItemId = 0;
						double max = 1.0;
						for(int[] itemInfo : ConfigChaosFestival.CHAOS_FESTIVAL_MYST_BOX_RANDOM_REWARDS)
						{
							int itemId = itemInfo[0];
							double itemChance = itemInfo[1] / 100000.0;
							if(max - itemChance <= chance)
							{
								selectedItemId = itemId;
								break;
							}
							max -= itemChance;
						}

						((L2MonsterInstance) npc).dropItem(attacker, new ItemHolder(selectedItemId, 1));
					}
				}, 3000);
				break;
		}

		return null;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		return null;
	}
}
