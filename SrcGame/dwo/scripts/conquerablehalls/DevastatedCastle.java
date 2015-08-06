package dwo.scripts.conquerablehalls;

import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.world.residence.clanhall.ClanHallSiegeEngine;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * @author BiggBoss
 * Devastated Castle clan hall siege script
 */
public class DevastatedCastle extends ClanHallSiegeEngine
{
	private static final String qn = "DevastatedCastle";

	private static final int GUSTAV = 35410;
	private static final int MIKHAIL = 35409;
	private static final int DIETRICH = 35408;
	private static final double GUSTAV_TRIGGER_HP = NpcTable.getInstance().getTemplate(GUSTAV).getBaseHpMax() / 12;

	private static TIntIntHashMap _damageToGustav = new TIntIntHashMap();

	public DevastatedCastle(int questId, String name, String descr, int hallId)
	{
		super(questId, name, descr, hallId);
		addKillId(GUSTAV);

		addSpawnId(MIKHAIL);
		addSpawnId(DIETRICH);

		addAttackId(GUSTAV);
	}

	public static void main(String[] args)
	{
		new DevastatedCastle(-1, qn, "conquerablehalls", DEVASTATED_CASTLE);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(!_hall.isInSiege())
		{
			return null;
		}

		synchronized(this)
		{
			L2Clan clan = attacker.getClan();

			if(clan != null && checkIsAttacker(clan))
			{
				int id = clan.getClanId();
				if(_damageToGustav.containsKey(id))
				{
					int newDamage = _damageToGustav.get(id);
					newDamage += damage;
					_damageToGustav.put(id, newDamage);
				}
				else
				{
					_damageToGustav.put(id, damage);
				}
			}

			if(npc.getCurrentHp() < GUSTAV_TRIGGER_HP && npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST)
			{
				broadcastNpcSay(npc, ChatType.ALL, 1000278);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, SkillTable.getInstance().getInfo(4235, 1), npc);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(!_hall.isInSiege())
		{
			return null;
		}

		_missionAccomplished = true;

		if(npc.getNpcId() == GUSTAV)
		{
			synchronized(this)
			{
				cancelSiegeTask();
				endSiege();
			}
		}

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(npc.getNpcId() == MIKHAIL)
		{
			broadcastNpcSay(npc, ChatType.SHOUT, 1000276);
		}
		else if(npc.getNpcId() == DIETRICH)
		{
			broadcastNpcSay(npc, ChatType.SHOUT, 1000277);
		}
		return null;
	}

	@Override
	public L2Clan getWinner()
	{
		double counter = 0;
		int damagest = 0;
		for(int clan : _damageToGustav.keys())
		{
			double damage = _damageToGustav.get(clan);
			if(damage > counter)
			{
				counter = damage;
				damagest = clan;
			}
		}
		return ClanTable.getInstance().getClan(damagest);
	}
}