package dwo.scripts.ai.zone;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;

/**
 * Frozen Labyrinth AI
 * @author malyelfik
 */
public class FrozenLabyrinth extends Quest
{
	// Monsters
	private static final int PRONGHORN_SPIRIT = 22087;
	private static final int PRONGHORN = 22088;
	private static final int LOST_BUFFALO = 22093;
	private static final int FROST_BUFFALO = 22094;

	private FrozenLabyrinth()
	{
		addAttackId(PRONGHORN, FROST_BUFFALO);
	}

	public static void main(String[] args)
	{
		new FrozenLabyrinth();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		if(skill != null && !skill.isMagic())
		{
			int spawnId = LOST_BUFFALO;
			if(npc.getNpcId() == PRONGHORN)
			{
				spawnId = PRONGHORN_SPIRIT;
			}

			int diff = 0;
			for(int i = 0; i < 6; i++)
			{
				int x = diff < 60 ? npc.getX() + diff : npc.getX();
				int y = diff >= 60 ? npc.getY() + diff - 40 : npc.getY();

				L2Attackable monster = (L2Attackable) addSpawn(spawnId, x, y, npc.getZ(), npc.getHeading(), false, 0);
				monster.attackCharacter(attacker);
				diff += 20;
			}
			npc.getLocationController().delete();
		}
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}
}