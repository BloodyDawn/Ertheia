package dwo.gameserver.model.skills.base.l2skills;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2DecoyInstance;
import dwo.gameserver.model.actor.instance.L2IncarnationInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.FlyToLocation;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;

import java.util.List;

public class L2SkillIncarnation extends L2SkillDecoy
{
	private short _incarnationsCount;

	public L2SkillIncarnation(StatsSet set)
	{
		super(set);
		_incarnationsCount = set.getShort("incarnationsCount", (short) 1);
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		super.useSkill(caster, targets);
		// Телепортируем игрока
		teleport(caster, caster.getTarget());
        caster.getActingPlayer().getAppearance().setInvisible();
	}

	@Override
	protected void teleport(L2Character character, L2Object target)
	{
		if(!(target instanceof L2Character))
		{
			return;
		}

		Location loc = target.getLoc();
		loc.setX(target.getX() + Rnd.get(-100, 100));
		loc.setY(target.getY() + Rnd.get(-100, 100));

		if(Config.GEODATA_ENABLED)
		{
			loc = GeoEngine.getInstance().moveCheck(character.getX(), character.getY(), character.getZ(), loc.getX(), loc.getY(), loc.getZ(), character.getInstanceId());
		}

		character.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		character.broadcastPacket(new FlyToLocation(character, loc.getX(), loc.getY(), loc.getZ(), FlyToLocation.FlyType.DUMMY, 0, 0, 0));
		character.abortAttack();
		character.abortCast();
		character.setXYZ(loc.getX(), loc.getY(), loc.getZ());
		character.broadcastPacket(new ValidateLocation(character));

		if(character instanceof L2IncarnationInstance)
		{
			if(!target.equals(((L2IncarnationInstance) character).getOwner()))
			{
				character.setTarget(target);
				((L2IncarnationInstance) character).addDamageHate((L2Character) target, 0, 999);
				character.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
	}

	@Override
	protected List<L2DecoyInstance> createNpc(L2PcInstance activeChar)
	{
		List<L2DecoyInstance> decoys = new FastList<>();
		L2NpcTemplate decoyTemplate = NpcTable.getInstance().getTemplate(_npcId);


		for(short i = 0; i < _incarnationsCount; ++i)
		{
			L2DecoyInstance decoy = new L2IncarnationInstance(IdFactory.getInstance().getNextId(), decoyTemplate, activeChar, this);

			// Передаем эффекты от кастера
			for(L2Effect effect : activeChar.getAllEffects())
			{
				if(effect != null && effect.canBeStolen())
				{
					decoy.addEffect(effect);
				}
			}

			decoys.add(decoy);
		}

		return decoys;
	}
}