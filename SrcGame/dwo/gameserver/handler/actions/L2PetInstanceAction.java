package dwo.gameserver.handler.actions;

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.engine.hookengine.IHookContainer;
import dwo.gameserver.engine.hookengine.container.CharacterHookContainer;
import dwo.gameserver.handler.IActionHandler;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.packet.pet.PetStatusShow;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;
import org.apache.log4j.Level;

public class L2PetInstanceAction implements IActionHandler
{
	private IHookContainer _hookContainer = new CharacterHookContainer();

	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		// Aggression target lock effect
		if(activeChar.isLockedTarget() && activeChar.getLockedTarget() != target)
		{
			activeChar.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
			return false;
		}

		boolean isOwner = activeChar.getObjectId() == ((L2PetInstance) target).getOwner().getObjectId();

		activeChar.sendPacket(new ValidateLocation((L2Character) target));
		if(isOwner && activeChar != ((L2PetInstance) target).getOwner())
		{
			((L2PetInstance) target).updateRefOwner(activeChar);
		}

		if(activeChar.getTarget() != target)
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "new target selected:" + target.getObjectId());
			}

			// Send a ServerMode->Client packet StatusUpdate of the L2PetInstance to
			// the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(target);
			su.addAttribute(StatusUpdate.CUR_HP, (int) ((L2Character) target).getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, ((L2Character) target).getMaxHp());
			activeChar.sendPacket(su);

			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), activeChar.getLevel() - ((L2Character) target).getLevel()));

			// Set the target of the L2PcInstance activeChar
			activeChar.setTarget(target);
		}
		else if(interact)
		{
			// Check if the pet is attackable (without a forced attack) and
			// isn't dead
			if(target.isAutoAttackable(activeChar) && !isOwner)
			{
				if(Config.GEODATA_ENABLED)
				{
					if(GeoEngine.getInstance().canSeeTarget(activeChar, target ) )
					{
						// Set the L2PcInstance Intention to AI_INTENTION_ATTACK
						activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
						activeChar.onActionRequest();
					}
				}
				else
				{
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					activeChar.onActionRequest();
				}
			}
			else if(!((L2Character) target).isInsideRadius(activeChar, 150, false, false))
			{
				if(Config.GEODATA_ENABLED)
				{
					if(GeoEngine.getInstance().canSeeTarget(activeChar, target))
					{
						activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
						activeChar.onActionRequest();
					}
				}
				else
				{
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
					activeChar.onActionRequest();
				}
			}
			else
			{
				if(isOwner)
				{
					activeChar.sendPacket(new PetStatusShow((L2PetInstance) target));
					HookManager.getInstance().notifyEvent(HookType.ON_SUMMON_ACTION, _hookContainer, target, activeChar);
				}
			}
		}
		return true;
	}

	@Override
	public Class<? extends L2Object> getInstanceType()
	{
		return L2PetInstance.class;
	}

	/**
	 * @return
	 */
	private IHookContainer getHookContainer()
	{
		return _hookContainer;
	}
}