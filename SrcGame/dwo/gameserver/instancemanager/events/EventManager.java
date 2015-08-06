package dwo.gameserver.instancemanager.events;

import dwo.config.events.ConfigEventCTF;
import dwo.config.events.ConfigEventKOTH;
import dwo.gameserver.instancemanager.events.CTF.CTFEvent;
import dwo.gameserver.instancemanager.events.CTF.CTFEventTeam;
import dwo.gameserver.instancemanager.events.CTF.CTFManager;
import dwo.gameserver.instancemanager.events.KOTH.KOTHEvent;
import dwo.gameserver.instancemanager.events.KOTH.KOTHEventTeam;
import dwo.gameserver.instancemanager.events.KOTH.KOTHManager;
import dwo.gameserver.instancemanager.events.LastHero.LastHeroEvent;
import dwo.gameserver.instancemanager.events.TvT.TvTEvent;
import dwo.gameserver.instancemanager.events.TvT.TvTEventTeam;
import dwo.gameserver.instancemanager.events.TvT.TvTManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.components.SystemMessageId;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class EventManager
{
	protected static final Logger _log = LogManager.getLogger(EventManager.class);

	public static void onKill(L2Character killerCharacter, L2PcInstance killedPlayerInstance)
	{
		TvTEvent.onKill(killerCharacter, killedPlayerInstance);
		CTFEvent.onKill(killerCharacter, killedPlayerInstance);
		KOTHEvent.onKill(killerCharacter, killedPlayerInstance);
	}

	public static boolean onDieDropItem(L2PcInstance player)
	{
		if(TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(player.getObjectId()))
		{
			return true;
		}
		if(CTFEvent.isStarted() && CTFEvent.isPlayerParticipant(player.getObjectId()))
		{
			return true;
		}
		return KOTHEvent.isStarted() && KOTHEvent.isPlayerParticipant(player.getObjectId());

	}

	public static boolean isAutoAttackable(L2PcInstance player, L2Character attacker)
	{
		if(TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(player.getObjectId()) && TvTEvent.getParticipantTeamId(player.getObjectId()) != TvTEvent.getParticipantTeamId(attacker.getObjectId()))
		{
			return true;
		}
		if(CTFEvent.isStarted() && CTFEvent.isAutoAttackable(player, attacker))
		{
			return true;
		}
		return KOTHEvent.isStarted() && KOTHEvent.isAutoAttackable(player, attacker);

	}

	public static void onTeleported(L2PcInstance player)
	{
		TvTEvent.onTeleported(player);
		CTFEvent.onTeleported(player);
		KOTHEvent.onTeleported(player);
	}

	public static void onLogin(L2PcInstance player)
	{
		TvTEvent.onLogin(player);
		CTFEvent.onLogin(player);
		KOTHEvent.onLogin(player);
	}

	public static void onLogout(L2PcInstance player)
	{
		// TvT Event removal 
		try
		{
			TvTEvent.onLogout(player);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		// CTF Event removal 
		try
		{
			CTFEvent.onLogout(player);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		// KOTH Event removal 
		try
		{
			KOTHEvent.onLogout(player);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}
	}

	public static void onJail(L2PcInstance player)
	{
		if(!TvTEvent.isInactive() && TvTEvent.isPlayerParticipant(player.getObjectId()))
		{
			TvTEvent.removeParticipant(player.getObjectId());
		}
		else if(!CTFEvent.isInactive() && CTFEvent.isPlayerParticipant(player.getObjectId()))
		{
			CTFEvent.removeParticipant(player);
		}
		else if(!KOTHEvent.isInactive() && KOTHEvent.isPlayerParticipant(player.getObjectId()))
		{
			KOTHEvent.removeParticipant(player.getObjectId());
		}
	}

	public static boolean calculateDeathPenaltyBuff(L2PcInstance player)
	{
		if(TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(player.getObjectId()))
		{
			return true;
		}
		if(CTFEvent.isStarted() && CTFEvent.isPlayerParticipant(player.getObjectId()))
		{
			return true;
		}
		return KOTHEvent.isStarted() && KOTHEvent.isPlayerParticipant(player.getObjectId());

	}

	public static boolean onEscapeUse(L2PcInstance player)
	{
		if(!TvTEvent.onEscapeUse(player.getObjectId()))
		{
			player.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		if(!CTFEvent.onEscapeUse(player.getObjectId()))
		{
			player.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		if(!KOTHEvent.onEscapeUse(player.getObjectId()))
		{
			player.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}

		return true;
	}

	public static boolean checkForEventSkill(L2PcInstance player, L2PcInstance targetPlayer, L2Skill skill)
	{
		if(!TvTEvent.checkForTvTSkill(player, targetPlayer, skill))
		{
			return false;
		}
		if(!CTFEvent.checkForSkill(player, targetPlayer, skill))
		{
			return false;
		}
		return KOTHEvent.checkForSkill(player, targetPlayer, skill);

	}

	public static boolean eventTarget(L2PcInstance _owner, L2Character _target)
	{
		L2Object ownerTarget = _owner.getTarget();
		if(ownerTarget == null)
		{
			return true;
		}

		// TvT event targeting
		if(TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(_owner.getObjectId()))
		{
			TvTEventTeam enemyTeam = TvTEvent.getParticipantEnemyTeam(_owner.getObjectId());

			if(ownerTarget.getActingPlayer() != null)
			{
				L2PcInstance target = ownerTarget.getActingPlayer();
				if(enemyTeam.containsPlayer(target.getObjectId()) && !target.isDead())
				{
					_target = (L2Character) ownerTarget;
				}
			}
			return true;
		}

		// CTF event targeting
		if(CTFEvent.isStarted() && CTFEvent.isPlayerParticipant(_owner.getObjectId()))
		{
			CTFEventTeam enemyTeam = CTFEvent.getParticipantEnemyTeam(_owner.getObjectId());

			if(ownerTarget.getActingPlayer() != null)
			{
				L2PcInstance target = ownerTarget.getActingPlayer();
				if(enemyTeam.containsRegisteredPlayer(target.getObjectId()) && !target.isDead())
				{
					_target = (L2Character) ownerTarget;
				}
			}
			return true;
		}

		// KOTH event targeting
		if(KOTHEvent.isStarted() && KOTHEvent.isPlayerParticipant(_owner.getObjectId()))
		{
			KOTHEventTeam enemyTeam = KOTHEvent.getParticipantEnemyTeam(_owner.getObjectId());

			if(ownerTarget.getActingPlayer() != null)
			{
				L2PcInstance target = ownerTarget.getActingPlayer();
				if(enemyTeam.containsRegisteredPlayer(target.getObjectId()) && !target.isDead())
				{
					_target = (L2Character) ownerTarget;
				}
			}
			return true;
		}
		return false;
	}

	public static boolean isPlayerParticipant(L2PcInstance player)
	{
		if(TvTEvent.isPlayerParticipant(player.getObjectId()))
		{
			return true;
		}
		if(CTFEvent.isPlayerParticipant(player.getObjectId()))
		{
			return true;
		}
		return KOTHEvent.isPlayerParticipant(player.getObjectId());

	}

	public static boolean isStarted()
	{
		if(TvTEvent.isStarted())
		{
			return true;
		}
		if(CTFEvent.isStarted())
		{
			return true;
		}
		return KOTHEvent.isStarted();
	}

	public static boolean onItemSummon(L2PcInstance player)
	{
		if(!TvTEvent.onItemSummon(player.getObjectId()))
		{
			return false;
		}
		if(!CTFEvent.onItemSummon(player.getObjectId()))
		{
			return false;
		}
		return KOTHEvent.onItemSummon(player.getObjectId());
	}

	public static boolean onScrollUse(L2PcInstance player)
	{
		if(!TvTEvent.onScrollUse(player.getObjectId()))
		{
			return false;
		}
		if(!CTFEvent.onScrollUse(player.getObjectId()))
		{
			return false;
		}
		return KOTHEvent.onScrollUse(player.getObjectId());
	}

	public static boolean onSitForced(L2PcInstance player)
	{
		return false;
	}

	public static void getEventsInstances()
	{
		TvTManager.getInstance();
		CTFManager.getInstance();
		KOTHManager.getInstance();
		LastHeroEvent.getInstance();
	}

	/**
	 * @return true = calls return on the place where this method is called
	 */
	public static boolean onToVillage(L2PcInstance player, boolean havePressedToVillage)
	{
		if(CTFEvent.isStarting() || CTFEvent.isStarted() && CTFEvent.isPlayerParticipant(player.getObjectId()))
		{
			if(havePressedToVillage)
			{
				CTFEvent.onToVillage(player);
				return true;
			}
			else
			{
				CTFEvent.removeParticipant(player);
			}
		}
		else if(KOTHEvent.isStarting() || KOTHEvent.isStarted() && KOTHEvent.isPlayerParticipant(player.getObjectId()))
		{
			if(havePressedToVillage)
			{
				KOTHEvent.onToVillage(player);
				return true;
			}
			else
			{
				KOTHEvent.removeParticipant(player.getObjectId());
			}
		}
		return false;
	}

	/**
	 * @return true = calls return on the place where this method is called
	 */
	public static boolean onRequestRestartPointRunImpl(L2PcInstance player)
	{
		if(ConfigEventKOTH.KOTH_EVENT_RESPAWN_TYPE == ConfigEventKOTH.KOTHEventRespawnType.CLASSIC && KOTHEvent.isStarted() && KOTHEvent.isPlayerParticipant(player.getObjectId()))
		{
			return true;
		}
		if(ConfigEventCTF.CTF_EVENT_RESPAWN_TYPE == ConfigEventCTF.CTFEventRespawnType.CLASSIC && CTFEvent.isStarted() && CTFEvent.isPlayerParticipant(player.getObjectId()))
		{
			return true;
		}
		return TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(player.getObjectId());

	}

	/**
	 * @return true = calls return on the place where this method is called
	 */
	public static boolean onRequestUnEquipItem(L2PcInstance player)
	{
		return !(CTFEvent.isStarted() && CTFEvent.isPlayerParticipant(player.getObjectId()) && CTFEvent.isFlagOwner(player));
	}

	/**
	 * @return true = calls return on the place where this method is called
	 */
	public static boolean onUseItem(L2PcInstance player)
	{
		// Dont allow weapon/shielf equipment if a ctf flag is equipped
		return !(CTFEvent.isStarted() && CTFEvent.isPlayerParticipant(player.getObjectId()) && CTFEvent.isFlagOwner(player));
	}

	public static boolean onDie(L2PcInstance player)
	{
		boolean _canTeleport = true;

		if(CTFEvent.isStarted() && ConfigEventCTF.CTF_EVENT_RESPAWN_TYPE == ConfigEventCTF.CTFEventRespawnType.CLASSIC && CTFEvent.isPlayerParticipant(player.getObjectId()))
		{
			_canTeleport = false;
		}
		else if(KOTHEvent.isStarted() && ConfigEventKOTH.KOTH_EVENT_RESPAWN_TYPE == ConfigEventKOTH.KOTHEventRespawnType.CLASSIC && KOTHEvent.isPlayerParticipant(player.getObjectId()))
		{
			_canTeleport = false;
		}
		else if(TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(player.getObjectId()))
		{
			_canTeleport = false;
		}
		return _canTeleport;
	}

	/**
	 * @param activeChar
	 * @param objectId
	 * @return
	 */
	public static boolean onAction(L2PcInstance activeChar, int objectId)
	{
		if(!TvTEvent.onAction(activeChar, objectId))
		{
			return false;
		}
		if(!CTFEvent.onAction(activeChar, objectId))
		{
			return false;
		}
		return KOTHEvent.onAction(activeChar, objectId);
	}

	public static boolean onHandleCommandBBS( L2PcInstance player )
	{
		return isStarted() && isPlayerParticipant( player );
	}
}
