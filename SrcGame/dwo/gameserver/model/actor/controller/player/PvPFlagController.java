package dwo.gameserver.model.actor.controller.player;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.RelationChanged;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.Future;

/**
 * @author Yorie
 */
public class PvPFlagController extends PlayerController
{
	/**
	 * Duration before the end of flag, when character name should be blinking.
	 */
	public static final long PVP_FLAG_BLINKING_PERIOD = 20000;
	private static final Logger log = LogManager.getLogger(PvPFlagController.class);
	private FlagState state = FlagState.NO_FLAG;
	private long expirationTime;
	private Future<?> flagTask;

	public PvPFlagController(L2PcInstance player)
	{
		super(player);
	}

	/**
	 * Returns current PvP flag expiration time.
	 * @return Expiration time.
	 */
	public long getExpirationTime()
	{
		return expirationTime;
	}

	/**
	 * Sets the PvP flag expiration time.
	 * @param time Expiration time.
	 */
	public void setExpirationTime(long time)
	{
		expirationTime = time;
	}

	/**
	 * Starts player PvP mode.
	 */
	public void startFlag()
	{
		updateFlag(FlagState.FLAGGED);
		if(flagTask == null)
		{
			flagTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PvPFlag(), 1000, 1000);
		}
	}

	/**
	 * Stops PvP flag.
	 */
	public void stopFlag()
	{
		if(flagTask != null)
		{
			flagTask.cancel(true);
			flagTask = null;
		}
	}

	/**
	 * Returns current PvP flag state.
	 * @return PvP flag state.
	 */
	public FlagState getState()
	{
		return state;
	}

	/**
	 * Sets PvP flag state for player.
	 * @param state PvP flag state.
	 */
	protected void setState(FlagState state)
	{
		this.state = state;
	}

	/**
	 * @return True, if character is in PvP mode (no matter, blinking or not).
	 */
	public boolean isFlagged()
	{
		return state != FlagState.NO_FLAG;
	}

	/**
	 * Returns integer representation of current flag state.
	 * @return State ordinal value.
	 */
	public int getStateValue()
	{
		return state.ordinal();
	}

	/**
	 * Updates PvP flag status for player.
	 */
	public void updateStatus()
	{
		if(player.isInsideZone(L2Character.ZONE_PVP))
		{
			return;
		}

		expirationTime = System.currentTimeMillis() + Config.PVP_NORMAL_TIME;

		if(!isFlagged())
		{
			startFlag();
		}
	}

	/**
	 * Updates PvP flag status depending on attacked target.
	 * @param target Attacked target.
	 */
	public void updateStatus(L2Character target)
	{
		L2PcInstance playerTarget = target.getActingPlayer();

		if(playerTarget == null || player.isInDuel() && playerTarget.getDuelId() == player.getDuelId())
		{
			return;
		}

		// If characters not in PvP zone and target isn't PK
		if((!player.isInsideZone(L2Character.ZONE_PVP) || !playerTarget.isInsideZone(L2Character.ZONE_PVP)) && playerTarget.getReputation() >= 0)
		{
			expirationTime = System.currentTimeMillis() + (player.checkIfPvP(playerTarget) ? Config.PVP_PVP_TIME : Config.PVP_NORMAL_TIME);

			if(state == FlagState.NO_FLAG)
			{
				startFlag();
			}
		}
	}

	/**
	 * System method to update flag state.
	 * @param state Flag state.
	 */
	private void updateFlag(FlagState state)
	{
		if(this.state == state)
		{
			return;
		}

		this.state = state;

		player.broadcastUserInfo();

		updatePetsFlag(player);

		player.getKnownList().getKnownPlayers().values().stream().filter(target -> target != null).forEach(target -> {
			if(!target.equals(player))
			{
				target.sendPacket(new RelationChanged(player, player.getRelation(target), player.isAutoAttackable(target)));
			}

			updatePetsFlag(target);
		});
	}

	/**
	 * Sends flag update packet to @target.
	 */
	private void updatePetsFlag(L2PcInstance target)
	{
		if(!player.getPets().isEmpty())
		{
			// Applied on summons too
			for(L2Summon pet : player.getPets())
			{
				target.sendPacket(new RelationChanged(pet, player.getRelation(target), player.isAutoAttackable(target)));
			}
		}
	}

	/**
	 * PvP flag state.
	 * @author Yorie.
	 */
	public static enum FlagState
	{
		/**
		 * Player not in PvP mode.
		 */
		NO_FLAG,
		/**
		 * Player in PvP mode.
		 */
		FLAGGED,
		/**
		 * Player still in PvP mode, but his name is blinking, what means he'll be not in PvP mode soon.
		 */
		BLINKING;

		public static FlagState valueOf(int value)
		{
			if(value < 0)
			{
				return NO_FLAG;
			}
			else
			{
				return value > BLINKING.ordinal() ? BLINKING : FlagState.values()[value];
			}
		}
	}

	private class PvPFlag implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if(System.currentTimeMillis() > getExpirationTime())
				{
					stopFlag();
					updateFlag(FlagState.NO_FLAG);
				}
				else if(System.currentTimeMillis() > getExpirationTime() - PVP_FLAG_BLINKING_PERIOD)
				{
					updateFlag(FlagState.BLINKING);
				}
				else
				{
					updateFlag(FlagState.FLAGGED);
				}
			}
			catch(Exception e)
			{
				log.log(Level.ERROR, "Error in PvP Flag task: ", e);
			}
		}
	}
}
