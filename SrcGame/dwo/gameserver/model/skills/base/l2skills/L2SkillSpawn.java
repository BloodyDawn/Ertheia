package dwo.gameserver.model.skills.base.l2skills;

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2TotemInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class L2SkillSpawn extends L2Skill
{
	private static final Logger _log = LogManager.getLogger(L2SkillSpawn.class);

	private final int _npcId;
	private final int _despawnDelay;
	private final boolean _summonSpawn;
	private final boolean _randomOffset;
	private final int[] _skillToCast;
	private final int[] _skillToCastLevel;

	public L2SkillSpawn(StatsSet set)
	{
		super(set);
		_npcId = set.getInteger("npcId", 0);
		_despawnDelay = set.getInteger("despawnDelay", 0);
		_summonSpawn = set.getBool("isSummonSpawn", false);
		_randomOffset = set.getBool("randomOffset", true);
		String[] stc = set.getString("skillToCast", "0").split(",");
		int[] stc2 = new int[stc.length];

		for(int i = 0; i < stc.length; i++)
		{
			stc2[i] = Integer.parseInt(stc[i]);
		}

		_skillToCast = stc2;

		stc = set.getString("skillToCastLevel", "1").split(",");
		stc2 = new int[_skillToCast.length];

		for(int i = 0; i < _skillToCast.length; i++)
		{
			stc2[i] = 1;
		}

		for(int i = 0; i < stc.length; i++)
		{
			stc2[i] = Integer.parseInt(stc[i]);
		}

		_skillToCastLevel = stc2;
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if(caster.isAlikeDead() || !caster.isPlayer())
		{
			return;
		}

		if(_npcId == 0)
		{
			_log.log(Level.WARN, "NPC ID not defined for skill ID:" + getId());
			return;
		}

		L2NpcTemplate template = NpcTable.getInstance().getTemplate(_npcId);
		if(template == null)
		{
			_log.log(Level.WARN, "Spawn of the nonexisting NPC ID:" + _npcId + ", skill ID:" + getId());
			return;
		}

		L2Spawn spawn;
		try
		{
			spawn = new L2Spawn(template);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception in L2SkillSpawn: " + e.getMessage(), e);
			return;
		}

		int x = caster.getX();
		int y = caster.getY();
		if(_randomOffset)
		{
			x += Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20);
			y += Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20);
		}

		spawn.setLocx(x);
		spawn.setLocy(y);
		spawn.setLocz(caster.getZ());
		spawn.setHeading(caster.getHeading());
		spawn.stopRespawn();

		L2Npc npc = spawn.doSpawn(_summonSpawn);
		npc.setName(template.getName());
		npc.setTitle(caster.getName());
		npc.setOwner(caster);
		if(_despawnDelay > 0)
		{
			npc.scheduleDespawn(_despawnDelay);
		}
		npc.setIsRunning(false); // Broadcast info

		if(npc instanceof L2TotemInstance && _skillToCast != null)
		{
			for(int i = 0; i < _skillToCast.length; i++)
			{
				((L2TotemInstance) npc).startSkillCastingTask(_skillToCast[i], _skillToCastLevel[i]);
			}
		}
	}
}