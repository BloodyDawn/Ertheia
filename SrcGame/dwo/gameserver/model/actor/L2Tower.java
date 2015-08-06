package dwo.gameserver.model.actor;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;

public abstract class L2Tower extends L2Npc
{
	protected L2Tower(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setIsInvul(false);
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Attackable during siege by attacker only
		return attacker != null && attacker.isPlayer() && getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().isInProgress() && getCastle().getSiege().checkIsAttacker(((L2PcInstance) attacker).getClan());
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if(!canTarget(player))
		{
			return;
		}

		// Check if the L2PcInstance already target the L2NpcInstance
		if(!equals(player.getTarget()))
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);

			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if(interact)
		{
			if(isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100 && GeoEngine.getInstance().canSeeTarget(player, this))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendActionFailed();
	}

	@Override
	public boolean isAttackable()
	{
		// Attackable during siege by attacker only
		return getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().isInProgress();
	}

	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}
}
