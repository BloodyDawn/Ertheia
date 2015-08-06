package dwo.gameserver.instancemanager.vehicle;

import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.instance.L2ShuttleInstance;
import dwo.gameserver.model.actor.templates.L2CharTemplate;
import dwo.gameserver.model.skills.stats.StatsSet;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * L2GOD Team
 * User: ANZO, Bacek
 * Date: 16.09.11
 * Time: 22:40
 */

public class ShuttleManager
{
	private TIntObjectHashMap<L2ShuttleInstance> _shuttles = new TIntObjectHashMap<>();

	private ShuttleManager()
	{
	}

	public static ShuttleManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public L2ShuttleInstance getNewShuttle(int x, int y, int z, int heading, int shuttleId, int[] type)
	{
		StatsSet npcDat = new StatsSet();
		npcDat.set("npcId", shuttleId);
		npcDat.set("level", 0);
		npcDat.set("jClass", "shuttle");

		npcDat.set("str", 0);
		npcDat.set("con", 0);
		npcDat.set("dex", 0);
		npcDat.set("int", 0);
		npcDat.set("wit", 0);
		npcDat.set("men", 0);

		npcDat.set("baseShldDef", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseAccCombat", 38);
		npcDat.set("baseEvasRate", 38);
		npcDat.set("base_critical", 38);

		npcDat.set("collision_radius", 0);
		npcDat.set("collision_height", 0);
		npcDat.set("sex", "male");
		npcDat.set("type", "");
		npcDat.set("base_attack_range", 0);
		npcDat.set("org_mp", 0);
		npcDat.set("baseCpMax", 0);
		npcDat.set("exp", 0);
		npcDat.set("sp", 0);
		npcDat.set("base_physical_attack", 0);
		npcDat.set("base_magic_attack", 0);
		npcDat.set("base_attack_speed", 0);
		npcDat.set("agro_range", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("slot_rhand", 0);
		npcDat.set("slot_lhand", 0);
		npcDat.set("armor", 0);
		npcDat.set("ground_high", 0);
		npcDat.set("ground_low", 0);
		npcDat.set("name", "Shuttle");
		npcDat.set("org_hp", 50000);
		npcDat.set("org_hp_regen", 3.0e-3f);
		npcDat.set("org_mp_regen", 3.0e-3f);
		npcDat.set("base_defend", 100);
		npcDat.set("base_magic_defend", 100);
		L2CharTemplate template = new L2CharTemplate(npcDat);
		L2ShuttleInstance shuttle = new L2ShuttleInstance(IdFactory.getInstance().getNextId(), template, shuttleId, type);
		shuttle.getStat().setMoveSpeed(500);
		shuttle.setHeading(heading);
		shuttle.setXYZ(x, y, z);
		shuttle.getLocationController().spawn();
		_shuttles.put(shuttleId, shuttle);
		return shuttle;
	}

	public L2ShuttleInstance getShuttle(int id)
	{
		return _shuttles.get(id);
	}

	private static class SingletonHolder
	{
		protected static final ShuttleManager _instance = new ShuttleManager();
	}
}
