package dwo.scripts.ai.zone;

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import org.apache.log4j.Level;

public class CryptsOfDisgrace extends Quest
{
	public static final int[] MOBS = {
		22703, 22704, 22705, 22706, 22707
	};

	private static final int[][] MobSpawns = {
		{18464, -28681, 255110, -2160, 10}, {18464, -26114, 254708, -2139, 10}, {18463, -28457, 256584, -1926, 10},
		{18463, -26482, 257663, -1925, 10}, {18464, -26453, 256745, -1930, 10}, {18463, -27362, 256282, -1935, 10},
		{18464, -25441, 256441, -2147, 10}
	};

	public CryptsOfDisgrace()
	{

		addKillId(MOBS);

		for(int[] loc : MobSpawns)
		{
			addSpawn(loc[0], loc[1], loc[2], loc[3], loc[4]);
		}
	}

	public static void main(String[] args)
	{
		new CryptsOfDisgrace();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		return super.onKill(npc, player, isPet);
	}

	public void addSpawn(int mobId, int x, int y, int z, int respTime)
	{
		L2NpcTemplate template1;
		template1 = NpcTable.getInstance().getTemplate(mobId);
		L2Spawn spawn;
		try
		{
			spawn = new L2Spawn(template1);
			spawn.setLocx(x);
			spawn.setLocy(y);
			spawn.setLocz(z);
			spawn.setAmount(1);
			spawn.setHeading(-1);
			spawn.setRespawnDelay(respTime);
			spawn.setInstanceId(0);
			spawn.setOnKillDelay(0);
			SpawnTable.getInstance().addNewSpawn(spawn);
			spawn.init();
			spawn.startRespawn();
			if(respTime == 0)
			{
				spawn.stopRespawn();
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
	}
}
