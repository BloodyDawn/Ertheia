package dwo.gameserver.model.actor.instance;

import dwo.gameserver.model.actor.templates.L2NpcTemplate;

public class L2EventMonsterInstance extends L2MonsterInstance
{
	// Block offensive skills usage on event mobs
	// mainly for AoE skills, disallow kill many event mobs
	// with one skill
	private boolean block_skill_attack;

	// Block melee usage on event mobs
	private boolean block_melee_attack;

	// Event mobs should drop items to ground
	private boolean drop_on_ground;

	// Some Event NPc's must have drop protected to spawner not killer
	private L2PcInstance spawner;

	public L2EventMonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public void eventSetBlockOffensiveSkills(boolean value)
	{
		block_skill_attack = value;
	}

	public void eventSetBlockMeleeAttack(boolean value)
	{
		block_melee_attack = value;
	}

	public void eventSetDropOnGround(boolean value)
	{
		drop_on_ground = value;
	}

	public boolean eventDropOnGround()
	{
		return drop_on_ground;
	}

	public boolean eventSkillAttackBlocked()
	{
		return block_skill_attack;
	}

	public boolean eventMeleeBlocked()
	{
		return block_melee_attack;
	}

	public void eventSetSpawner(L2PcInstance player)
	{
		spawner = player;
	}

	public L2PcInstance eventGetSpawner()
	{
		return spawner;
	}
}