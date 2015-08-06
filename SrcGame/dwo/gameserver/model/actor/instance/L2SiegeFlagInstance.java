package dwo.gameserver.model.actor.instance;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.castle.CastleSiegeManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallSiegeManager;
import dwo.gameserver.instancemanager.fort.FortSiegeManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.status.SiegeFlagStatus;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2SiegeClan;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.residence.Siegable;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;

public class L2SiegeFlagInstance extends L2Npc
{
	private final boolean _isAdvanced;
	private L2Clan _clan;
	private L2PcInstance _player;
	private Siegable _siege;
	private boolean _canTalk;

	public L2SiegeFlagInstance(L2PcInstance player, int objectId, L2NpcTemplate template, boolean advanced, boolean outPost)
	{
		super(objectId, template);

		_clan = player.getClan();
		_player = player;
		_canTalk = true;
		_siege = CastleSiegeManager.getInstance().getSiege(_player.getX(), _player.getY(), _player.getZ());
		if(_siege == null)
		{
			_siege = FortSiegeManager.getInstance().getSiege(_player.getX(), _player.getY(), _player.getZ());
		}
		if(_siege == null)
		{
			_siege = ClanHallSiegeManager.getInstance().getSiege(player);
		}
		if(_clan == null || _siege == null)
		{
			throw new NullPointerException(getClass().getSimpleName() + ": Initialization failed.");
		}
		L2SiegeClan sc = _siege.getAttackerClan(_clan);
		if(sc == null)
		{
			throw new NullPointerException(getClass().getSimpleName() + ": Cannot find siege clan.");
		}
		sc.addFlag(this);
		_isAdvanced = advanced;
		getStatus();
		setIsInvul(false);
	}

	/**
	 * Use L2SiegeFlagInstance(L2PcInstance, int, L2NpcTemplate, boolean) instead
	 * @param player
	 * @param objectId
	 * @param template
	 */
	@Deprecated
	public L2SiegeFlagInstance(L2PcInstance player, int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_isAdvanced = false;
	}

	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, L2Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, skill);
		if(canTalk())
		{
			if(getCastle() != null && getCastle().getSiege().isInProgress() || getFort() != null && getFort().getSiege().isInProgress() || getConquerableHall() != null && getConquerableHall().isInSiege())
			{
				if(_clan != null)
				{
					// send warning to owners of headquarters that theirs base is under attack
					_clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.BASE_UNDER_ATTACK));
					_canTalk = false;
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTalkTask(), 20000);
				}
			}
		}
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if(player == null || !canTarget(player))
		{
			return;
		}

		// Check if the L2PcInstance already target the L2NpcInstance
		if(this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a ServerMode->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);

			// Send a ServerMode->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);

			// Send a ServerMode->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if(interact)
		{
			if(isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100)
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else
			{
				// Send a ServerMode->Client ActionFail to the L2PcInstance in order to avoid that the client wait another packet
				player.sendActionFailed();
			}
		}
	}

	@Override
	public boolean isAttackable()
	{
		return !isInvul();
	}

	public boolean isAdvancedHeadquarter()
	{
		return _isAdvanced;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return !isInvul();
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if(!super.doDie(killer))
		{
			return false;
		}
		if(_siege != null && _clan != null)
		{
			L2SiegeClan sc = _siege.getAttackerClan(_clan);
			if(sc != null)
			{
				sc.removeFlag(this);
			}
		}
		return true;
	}

	@Override
	public SiegeFlagStatus getStatus()
	{
		return (SiegeFlagStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		setStatus(new SiegeFlagStatus(this));
	}

	void setCanTalk(boolean val)
	{
		_canTalk = val;
	}

	private boolean canTalk()
	{
		return _canTalk;
	}

	private class ScheduleTalkTask implements Runnable
	{

		public ScheduleTalkTask()
		{
		}

		@Override
		public void run()
		{
			setCanTalk(true);
		}
	}
}
