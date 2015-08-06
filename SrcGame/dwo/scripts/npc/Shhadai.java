package dwo.scripts.npc;

import dwo.gameserver.GameTimeController;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 25.11.12
 * Time: 10:47
 */

public class Shhadai extends Quest
{
	private static final int NPC = 32347;

	private static final int[] DAY_COORDS = {
		16882, 238952, 9776
	};
	private static final int[] NIGHT_COORDS = {
		9064, 253037, -1928
	};

	public Shhadai()
	{
		addAskId(NPC, -1006);
		addSpawnId(NPC);
	}

	private static void validatePosition(L2Npc npc)
	{
		int[] coords = DAY_COORDS;
		boolean mustRevalidate = false;
		if(npc.getX() != NIGHT_COORDS[0] && GameTimeController.getInstance().isNight())
		{
			coords = NIGHT_COORDS;
			mustRevalidate = true;
		}
		else if(npc.getX() != DAY_COORDS[0] && !GameTimeController.getInstance().isNight())
		{
			mustRevalidate = true;
		}

		if(mustRevalidate)
		{
			npc.getSpawn().setLocx(coords[0]);
			npc.getSpawn().setLocy(coords[1]);
			npc.getSpawn().setLocz(coords[2]);
			npc.teleToLocation(coords[0], coords[1], coords[2]);
		}
	}

	public static void main(String[] args)
	{
		new Shhadai();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == NPC)
		{
			if(ask == -1006)
			{
				if(reply == 1)
				{
					return "shhadai002.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(!npc.isTeleporting())
		{
			ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ValidatePosition(npc), 60000, 60000);
		}

		return super.onSpawn(npc);
	}

	private static class ValidatePosition implements Runnable
	{
		private final L2Npc _npc;

		public ValidatePosition(L2Npc npc)
		{
			_npc = npc;
		}

		@Override
		public void run()
		{
			validatePosition(_npc);
		}
	}
}
