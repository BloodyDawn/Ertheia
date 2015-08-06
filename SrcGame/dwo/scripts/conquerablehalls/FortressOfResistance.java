/**
 *
 */
package dwo.scripts.conquerablehalls;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.residence.clanhall.ClanHallSiegeEngine;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.util.StringUtil;
import gnu.trove.map.hash.TIntLongHashMap;
import org.apache.log4j.Level;

/**
 * @author BiggBoss
 * Fortress of Resistance clan hall siege Script
 */
public class FortressOfResistance extends ClanHallSiegeEngine
{
	private static final String qn = "FortressOfResistance";

	private static final int MESSENGER = 35382;
	private static final int BLOODY_LORD_NURKA = 35375;

	private final Location[] NURKA_COORDS = {
		new Location(45109, 112124, -1900),    // 30%
		new Location(47653, 110816, -2110),    // 40%
		new Location(47247, 109396, -2000)    // 30%
	};

	private L2Spawn _nurka;
	private TIntLongHashMap _damageToNurka = new TIntLongHashMap();
	private NpcHtmlMessage _messengerMsg;

	/**
	 * @param questId
	 * @param name
	 * @param descr
	 */
	public FortressOfResistance(int questId, String name, String descr, int hallId)
	{
		super(questId, name, descr, hallId);
		addFirstTalkId(MESSENGER);
		addKillId(BLOODY_LORD_NURKA);
		addAttackId(BLOODY_LORD_NURKA);
		buildMessengerMessage();

		try
		{
			_nurka = new L2Spawn(NpcTable.getInstance().getTemplate(BLOODY_LORD_NURKA));
			_nurka.setAmount(1);
			_nurka.setRespawnDelay(10800);

			_nurka.setLocation(NURKA_COORDS[0]);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, getName() + ": Couldnt set the Bloody Lord Nurka spawn", e);
		}
	}

	public static void main(String[] args)
	{
		new FortressOfResistance(-1, qn, "conquerablehalls", FORTRESS_RESSISTANCE);
	}

	private void buildMessengerMessage()
	{
		String html = HtmCache.getInstance().getHtmQuest(null, "conquerablehalls/FortressOfResistance/partisan_ordery_brakel001.htm");
		if(html != null)
		{
			_messengerMsg = new NpcHtmlMessage(5);
			_messengerMsg.setHtml(html);
			_messengerMsg.replace("%nextSiege%", StringUtil.formatDate(_hall.getSiegeDate().getTime(), "yyyy-MM-dd HH:mm:ss"));
		}
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if(!_hall.isInSiege())
		{
			return null;
		}

		int clanId = player.getClanId();
		if(clanId > 0)
		{
			long clanDmg = _damageToNurka.containsKey(clanId) ? _damageToNurka.get(clanId) + damage : damage;
			_damageToNurka.put(clanId, clanDmg);
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(!_hall.isInSiege())
		{
			return null;
		}

		_missionAccomplished = true;

		synchronized(this)
		{
			npc.getSpawn().stopRespawn();
			npc.getLocationController().delete();
			cancelSiegeTask();
			endSiege();
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		player.sendPacket(_messengerMsg);
		return null;
	}

	@Override
	public void onSiegeStarts()
	{
		_nurka.init();
	}

	@Override
	public void onSiegeEnds()
	{
		buildMessengerMessage();
	}

	@Override
	public L2Clan getWinner()
	{
		int winnerId = 0;
		long counter = 0;
		for(int i : _damageToNurka.keys())
		{
			long dam = _damageToNurka.get(i);
			if(dam > counter)
			{
				winnerId = i;
				counter = dam;
			}
		}
		return ClanTable.getInstance().getClan(winnerId);
	}
}
