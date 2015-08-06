package dwo.gameserver.handler.skills;

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.FlyToLocation;
import dwo.gameserver.network.game.serverpackets.FlyToLocation.FlyType;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.01.12
 * Time: 21:49
 */

public class ReplaceWithPet implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {L2SkillType.REPLACE_WITH_PET};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(!(activeChar instanceof L2PcInstance))
		{
			return;
		}

		L2PcInstance _actor = activeChar.getActingPlayer();

		L2Object targetPet = targets[0]; // Должен работать для одного пета, TARGET_ONE

		if(!(targetPet instanceof L2Summon))
		{
			return; // TODO: SysMessage
		}

		if(!_actor.getPets().isEmpty() && _actor.getPets().contains(targetPet))
		{
			Location locPet = targetPet.getLoc();
			Location locChar = _actor.getLoc();

			replace(_actor, locPet);
			replace((L2Summon) targetPet, locChar);
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	private void replace(L2Character _actor, Location loc)
	{
		int x = loc.getX();
		int y = loc.getY();
		int z = loc.getZ();

		if(Config.GEODATA_ENABLED)
		{
			Location destiny = GeoEngine.getInstance().moveCheck(_actor.getX(), _actor.getY(), _actor.getZ(), x, y, z, _actor.getInstanceId());
			x = destiny.getX();
			y = destiny.getY();
			z = destiny.getZ();
		}

		// TODO: check if this AI intention is retail-like. This stops player's
		// previous movement
		_actor.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		_actor.broadcastPacket(new FlyToLocation(_actor, x, y, z, FlyType.DUMMY, 0, 0, 0));
		_actor.abortAttack();
		_actor.abortCast();

		_actor.setXYZ(x, y, z);
		_actor.broadcastPacket(new ValidateLocation(_actor));
	}
}
