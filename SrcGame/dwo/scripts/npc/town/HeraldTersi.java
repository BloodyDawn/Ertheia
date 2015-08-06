package dwo.scripts.npc.town;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.instancemanager.GlobalVariablesManager;
import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.11.11
 * Time: 18:23
 *
 * Служит для спауна Геральда Терси в городах после убийства
 * Валакаса. Нпц остается стоять в городах не более дня.
 */

public class HeraldTersi extends Quest
{
	private static final int GeroldTersi = 4326;
	private static final int ValakasBoss = 29028;

	private static final List<Location> GeroldLocations = new ArrayList<>();
	private static final List<L2Spawn> GeroldSpawns = new ArrayList<>();

	private static final SkillHolder blessSkill = new SkillHolder(23312, 1);

	public HeraldTersi()
	{

		// TODO: Пока только Аден
		GeroldLocations.add(new Location(147934, 26648, -2200));

		addAskId(GeroldTersi, -77);
		addKillId(ValakasBoss);
		spawnGerold();
	}

	public static void main(String[] args)
	{
		new HeraldTersi();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -77)
		{
			if(reply == 1)
			{
				if(player.getFirstEffect(23312) != null)
				{
					blessSkill.getSkill().getEffects(npc, player);
					return null;
				}
				else
				{
					return "g_herald_of_navit002.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		GlobalVariablesManager.getInstance().storeVariable("valakasKillTime", String.valueOf(System.currentTimeMillis()));
		spawnGerold();
		return super.onKill(npc, killer, isPet);
	}

	private void spawnGerold()
	{
		if(GlobalVariablesManager.getInstance().getStoredVariable("valakasKillTime") != null)
		{
			long killTime = Long.parseLong(GlobalVariablesManager.getInstance().getStoredVariable("valakasKillTime"));
			long nowTime = System.currentTimeMillis();
			long dayInMillis = TimeUnit.DAYS.toMillis(1);
			long elapsedTime = nowTime - killTime;

			if(GrandBossManager.getInstance().getBossStatus(ValakasBoss) == 3 && elapsedTime < dayInMillis)
			{
				for(Location loc : GeroldLocations)
				{
					L2NpcTemplate template = NpcTable.getInstance().getTemplate(GeroldTersi);
					try
					{
						L2Spawn spawn = new L2Spawn(template);
						spawn.setLocation(loc);
						spawn.setAmount(1);
						spawn.setHeading(-1);
						SpawnTable.getInstance().addNewSpawn(spawn);
						spawn.init();
						spawn.startRespawn();
						GeroldSpawns.add(spawn);
					}
					catch(ClassNotFoundException | NoSuchMethodException e)
					{
						_log.log(Level.ERROR, "HeraldTersi: Problem with spawn Herald Tersi!");
					}
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new unSpawn(), killTime + dayInMillis - nowTime);
			}
			else
			{
				GlobalVariablesManager.getInstance().deleteVariableAndStore("valakasKillTime");
			}
		}
	}

	private static class unSpawn implements Runnable
	{
		@Override
		public void run()
		{
			for(L2Spawn spawn : GeroldSpawns)
			{
				SpawnTable.getInstance().deleteSpawn(spawn);
			}
			GlobalVariablesManager.getInstance().deleteVariableAndStore("valakasKillTime");
		}
	}
}
