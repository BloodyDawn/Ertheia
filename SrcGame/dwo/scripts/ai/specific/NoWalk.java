package dwo.scripts.ai.specific;

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Set;

public class NoWalk extends Quest
{
	// Не ходят, не сходят с точки даже если ударить
	int[] NoWalkEver = {
		18328, 18804, 18811, 18812, 18813, 18814, 18344, 18345, 18346, 18506, 18693, 18694, 18695, 18696, 18793, 18794,
		18795, 18796, 18797, 18798, 18864, 18865, 18866, 18867, 18868, 18912, 22548, 22549, 22593, 22598, 22599, 22600,
		27384, 29045, 29048, 29049, 32342, 32656, 29006,
		// SoD traps
		18720, 18721, 18722, 18723, 18724, 18725, 18729, 18730, 18731, 18732, 18737, 18739, 18740, 18741, 18742, 18743,
		18744, 18745, 18748, 18749, 18753, 18754, 18755, 18756, 18758, 18771,
		// Fortress
		35683, 35719, 35752, 35788, 35821, 35852, 35888, 35921, 35957, 35995, 36028, 36064, 36102, 36135, 36166, 36202,
		36240, 36278, 36311, 36347, 36385,
		// Гварды ТИ
		33007,
		// Яйца в инсте траджана
		18996, 18997, 19023,
		// Лучник в Крепости Гильотины
		23245,
		// Коконы в Саду Фей
		32919, 32920,
		// Мандрагора в Гильятине
		23240, 23241,
		// Для квеста 421_LittleWingBigAdventure
		27185, 27186, 27187, 27188
	};

	// Просто не ходят рандомом
	int[] onlyNoWalk = {
		// Sel Mahums Training Fields Privates
		22783, 22782, 22784, 22779, 22780, 22785,
		// Мобы в саду фей
		22887, 22871, 22895, 22863, 22879, 22897, 22865, 23041, 22881, 22905, 22888, 22872,// Дневной спаун
		22889, 22904, // TODO: Ночной спаун
		// Семя индустриализации

		23226, 23224, 23225, 23219, 23235,
		// Охраняют печи - 1й таж
		23233,
		// Охраняют печи - 2й этаж
		23234,
		// Seed Of Hellfire
		23220
	};

	public NoWalk()
	{
		for(int npcId : NoWalkEver)
		{
			Set<L2Spawn> spawns = SpawnTable.getInstance().getSpawns(npcId);
			spawns.stream().filter(spawn -> spawn.getLastSpawn() != null).forEach(spawn -> {
				spawn.getLastSpawn().setIsNoRndWalk(true);
				spawn.getLastSpawn().setIsImmobilized(true);
			});
		}
		for(int npcId : onlyNoWalk)
		{
			Set<L2Spawn> spawns = SpawnTable.getInstance().getSpawns(npcId);
			spawns.stream().filter(spawn -> spawn.getLastSpawn() != null).forEach(spawn -> spawn.getLastSpawn().setIsNoRndWalk(true));
		}
		addSpawnId(NoWalkEver);
		addSpawnId(onlyNoWalk);
	}

	public static void main(String[] args)
	{
		new NoWalk();
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(ArrayUtils.contains(NoWalkEver, npc.getNpcId()))
		{
			npc.setIsNoRndWalk(true);
			npc.setIsImmobilized(true);
		}
		else if(ArrayUtils.contains(onlyNoWalk, npc.getNpcId()))
		{
			npc.setIsNoRndWalk(true);
			npc.setIsNoAnimation(true);
		}
		return super.onSpawn(npc);
	}
}