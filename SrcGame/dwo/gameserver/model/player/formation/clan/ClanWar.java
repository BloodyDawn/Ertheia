package dwo.gameserver.model.player.formation.clan;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.concurrent.Future;

/**
 * Clan war entity.
 *
 * @author Yorie
 */
public class ClanWar
{
	public static final long PREPARATION_PERIOD_DURATION = 1000 * 60 * 60 * 24 * 3; // Three days
	public static final long INACTIVITY_TIME_DURATION = 1000 * 60 * 60 * 24 * 7; // One week
	public static final long PEACE_DURATION = 1000 * 60 * 60 * 24 * 5; // Five days
	private static final Logger log = LogManager.getLogger(ClanWar.class);
	private final int attackerClanId;
	private final int opposingClanId;
	private ClanWarPeriod period;
	private int currentPeriodStartTime;
	private int lastKillTime;
	private Future<?> mutualStartTask;
	private Future<?> inactivityCheckTask;
	private int attackersKillCounter;
	private int opposersKillCounter;

	/**
	 * Constructs new Clan War.
	 * @param attackerClanId Clan who requested CW.
	 * @param opposingClanId Clan who will be CW opponent.
	 * @param period Current period (state) of CW.
	 * @param currentPeriodStartTime Period start timestamp in seconds.
	 */
	public ClanWar(int attackerClanId, int opposingClanId, ClanWarPeriod period, int currentPeriodStartTime, int lastKillTime, int attackersKillCounter, int opposersKillCounter)
	{
		this.attackerClanId = attackerClanId;
		this.opposingClanId = opposingClanId;
		this.period = period;
		this.currentPeriodStartTime = currentPeriodStartTime;
		this.lastKillTime = lastKillTime;
		this.attackersKillCounter = attackersKillCounter;
		this.opposersKillCounter = opposersKillCounter;

		L2Clan attackerClan = ClanTable.getInstance().getClan(attackerClanId);
		L2Clan opposingClan = ClanTable.getInstance().getClan(opposingClanId);
		attackerClan.addClanWar(this);
		opposingClan.addClanWar(this);

		onChange();
	}

	/**
	 * Sets up last CW kill time in seconds.
	 * Invocation of this method will produce DB storage for this CW.
	 */
	public void onKill(L2PcInstance killer, L2PcInstance victim)
	{
		if(period != ClanWarPeriod.MUTUAL)
		{
			return;
		}

		// When your reputation score is 0 or below, the other clan cannot acquire any reputation points
		if(victim.getClan().getReputationScore() > 0)
		{
			killer.getClan().addReputationScore(Config.REPUTATION_SCORE_PER_KILL, false);
		}

		// When the opposing sides reputation score is 0 or below, your clans reputation score does not decrease
		if(killer.getClan().getReputationScore() > 0)
		{
			victim.getClan().takeReputationScore(Config.REPUTATION_SCORE_PER_KILL, false);
		}

		lastKillTime = (int) (System.currentTimeMillis() / 1000);

		if(killer.getClan().getClanId() == attackerClanId)
		{
			++attackersKillCounter;
		}
		else if(killer.getClan().getClanId() == opposingClanId)
		{
			++opposersKillCounter;
		}

		save();

		victim.getClan().broadcastToOnlineMembers(SystemMessage.getSystemMessage(3811).addPcName(victim).addPcName(killer));
		killer.getClan().broadcastToOnlineMembers(SystemMessage.getSystemMessage(3812).addPcName(killer).addPcName(victim));
	}

	public int getAttackersKillCounter()
	{
		return attackersKillCounter;
	}

	/**
	 * Calculates the point difference between specified clan and opponent clan.
	 * @param clan Target clan.
	 * @return Point difference.
	 */
	public int getPointDiff(L2Clan clan)
	{
		return attackerClanId == clan.getClanId() ? attackersKillCounter - opposersKillCounter : opposersKillCounter - attackersKillCounter;
	}

	/**
	 * Calculated L2 special clan war progress value.
	 * Value is based on point difference in clan war between clan who's point will be computed and opponent clan.
	 * @param pointDiff Point difference between target clan and it's opponent.
	 * @return Clan war state.
	 */
	public WarProgress calculateWarProgress(int pointDiff)
	{
		if(pointDiff <= -50)
		{
			return WarProgress.VERY_LOW;
		}
		else if(pointDiff > -50 && pointDiff <= -20)
		{
			return WarProgress.LOW;
		}
		else if(pointDiff > -20 && pointDiff <= 19)
		{
			return WarProgress.NORMAL;
		}
		else
		{
			return pointDiff > 19 && pointDiff <= 49 ? WarProgress.HIGH : WarProgress.VERY_HIGH;
		}
	}

	/**
	 * Calculates clan war state for specified clan.
	 * @param clan Target clan.
	 * @return State of clan war.
	 */
	public ClanWarState getClanWarState(L2Clan clan)
	{
		if(period == ClanWarPeriod.NEW || period == ClanWarPeriod.PREPARATION)
		{
			return ClanWarState.PREPARATION;
		}
		else if(period == ClanWarPeriod.MUTUAL)
		{
			return ClanWarState.MUTUAL;
		}
		else if(period == ClanWarPeriod.PEACE)
		{
			int points = getPointDiff(clan);
			if(points == 0)
			{
				return ClanWarState.TIE;
			}
			else
			{
				return points < 0 ? ClanWarState.LOSS : ClanWarState.WIN;
			}
		}
		else
		{
			return ClanWarState.REJECTED;
		}
	}

	/**
	 * Determines whether specified clan is attacker or not.
	 * @param clan Target clan.
	 * @return True, if clan is attacker.
	 */
	public boolean isAttacker(L2Clan clan)
	{
		return attackerClanId == clan.getClanId();
	}

	/**
	 * Determines whether specified clan is opposition or not.
	 * @param clan Target clan.
	 * @return True, if clan is opposing.
	 */
	public boolean isOpposing(L2Clan clan)
	{
		return opposingClanId == clan.getClanId();
	}

	@Nullable
	public L2Clan getAttackersClan()
	{
		return ClanTable.getInstance().getClan(attackerClanId);
	}

	@Nullable
	public L2Clan getOpposingClan()
	{
		return ClanTable.getInstance().getClan(opposingClanId);
	}

	public int getOpposersKillCounter()
	{
		return opposersKillCounter;
	}

	public int getLastKillTime()
	{
		return lastKillTime;
	}

	public int getAttackerClanId()
	{
		return attackerClanId;
	}

	public int getOpposingClanId()
	{
		return opposingClanId;
	}

	public ClanWarPeriod getPeriod()
	{
		return period;
	}

	public void setPeriod(ClanWarPeriod period)
	{
		if(this.period == period)
		{
			return;
		}

		if(this.period == ClanWarPeriod.MUTUAL && period == ClanWarPeriod.PREPARATION)
		{
			log.warn("Cannot change clan war period from mutual (when both sides fighting) to preparation.");
		}

		this.period = period;
		currentPeriodStartTime = (int) (System.currentTimeMillis() / 1000);

		// When clan war really started, we need update CW status in clans.
		if(period == ClanWarPeriod.MUTUAL)
		{
			L2Clan attackingClan = ClanTable.getInstance().getClan(attackerClanId);
			L2Clan opposingClan = ClanTable.getInstance().getClan(opposingClanId);

			if(attackingClan != null && opposingClan != null)
			{
				attackingClan.updateClanWarStatus(this);
				attackingClan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(215).addString(opposingClan.getName()));

				opposingClan.updateClanWarStatus(this);
				opposingClan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(215).addString(attackingClan.getName()));
			}

			onChange();
		}
		else if(period == ClanWarPeriod.PEACE)
		{
			L2Clan attackingClan = ClanTable.getInstance().getClan(attackerClanId);
			L2Clan opposingClan = ClanTable.getInstance().getClan(opposingClanId);
			if(attackingClan != null && opposingClan != null)
			{
				attackingClan.updateClanWarStatus(this);
				attackingClan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(216).addString(opposingClan.getName()));

				opposingClan.updateClanWarStatus(this);
				opposingClan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(216).addString(attackingClan.getName()));

				ClanWar ths = this;
				ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					attackingClan.removeClanWar(ths);
					opposingClan.removeClanWar(ths);

					ClanTable.getInstance().deleteClanWar(ths);
				}, PEACE_DURATION - (System.currentTimeMillis() - (long) currentPeriodStartTime * 1000));

				Calendar deletionTime = Calendar.getInstance();
				deletionTime.setTimeInMillis(System.currentTimeMillis() + PEACE_DURATION - (System.currentTimeMillis() - (long) currentPeriodStartTime * 1000));
				log.info("Clan war between clans " + attackingClan.getName() + " and " + opposingClan.getName() +
					" has end. CW scheduled for deletion at " + deletionTime.getTime() + '.');
			}

			onChange();
		}
	}

	/**
	 * @return Current period duration in ms.
	 */
	public long getPeriodDuration()
	{
		switch(period)
		{
			case NEW:
			case PREPARATION:
				return System.currentTimeMillis() - (long) currentPeriodStartTime * 1000;
			case MUTUAL:
				return 0;
			case PEACE:
				return System.currentTimeMillis() - (long) currentPeriodStartTime * 1000;
		}
		return 0;
	}

	public int getCurrentPeriodStartTime()
	{
		return currentPeriodStartTime;
	}

	/**
	 * Accepts clan war. Mutual period will start immediately.
	 * War can be accepted only by opposing clan (verification sign).
	 */
	public void accept(L2Clan requestor)
	{
		if(requestor.getClanId() == opposingClanId)
		{
			setPeriod(ClanWarPeriod.MUTUAL);
		}
	}

	/**
	 * Cancels clan war in cost of loosing 5000 clan reputation points by requester.
	 * @param requester Those clan who wants to stop CW.
	 */
	public void cancel(L2Clan requester)
	{
		requester.takeReputationScore(5000, true);
		requester.broadcastToOnlineMembers(SystemMessage.getSystemMessage(218));

		L2Clan winnerClan = requester.getClanId() == attackerClanId ? ClanTable.getInstance().getClan(opposingClanId) : ClanTable.getInstance().getClan(attackerClanId);
		winnerClan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(217));

		setPeriod(ClanWarPeriod.PEACE);
	}

	public void onChange()
	{
		if(period == ClanWarPeriod.NEW)
		{
			period = ClanWarPeriod.PREPARATION;

			L2Clan attackersClan = ClanTable.getInstance().getClan(attackerClanId);
			L2Clan opposingClan = ClanTable.getInstance().getClan(opposingClanId);
			if(attackersClan != null && opposingClan != null)
			{
				attackersClan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(1562).addString(opposingClan.getName()));
				opposingClan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(1561).addString(attackersClan.getName()));
			}

			save();
		}

		if(period == ClanWarPeriod.PREPARATION)
		{
			// Three days preparation period
			long mutualPeriodStartTime = (long) currentPeriodStartTime * 1000 + PREPARATION_PERIOD_DURATION - System.currentTimeMillis();

			// Mutual period start timer
			mutualStartTask = ThreadPoolManager.getInstance().scheduleGeneral(() -> {
				if(period != ClanWarPeriod.PREPARATION)
				{
					return;
				}

				setPeriod(ClanWarPeriod.MUTUAL);
			}, mutualPeriodStartTime);

			Calendar scheduleTime = Calendar.getInstance();
			scheduleTime.setTimeInMillis(System.currentTimeMillis() + mutualPeriodStartTime);
			log.info("Clan war between clans with ID " + attackerClanId + " and " + opposingClanId + " in preparation mode. Scheduled for mutual period at " + scheduleTime.getTime());
		}
		else if(period == ClanWarPeriod.MUTUAL)
		{
			// Inactivity check each hour
			inactivityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> {
				if(period == ClanWarPeriod.MUTUAL)
				{
					long lastKillTimeDuration = System.currentTimeMillis() - (long) lastKillTime * 1000;
					// Oops, CW cancel
					if(lastKillTimeDuration > INACTIVITY_TIME_DURATION)
					{
						setPeriod(ClanWarPeriod.PEACE);
					}
				}
				else
				{
					if(inactivityCheckTask != null)
					{
						inactivityCheckTask.cancel(false);
						inactivityCheckTask = null;
					}
				}
			}, 1000 * 60 * 60, 1000 * 60 * 60);

			if(lastKillTime > 0)
			{
				Calendar killTime = Calendar.getInstance();
				killTime.setTimeInMillis((long) lastKillTime * 1000);
				log.info("Last kill in clan war between clans with ID " + attackerClanId + " and " + opposingClanId + " wat at " + killTime.getTime() + ". Scheduled inactivity check per each hour.");
			}
			else
			{
				log.info("Last kill in clan war between clans with ID " + attackerClanId + " and " + opposingClanId + " has never happened. Scheduled inactivity check per each hour.");
			}
		}

		if(period != ClanWarPeriod.PREPARATION && mutualStartTask != null)
		{
			mutualStartTask.cancel(true);
			mutualStartTask = null;
		}
		else if(period != ClanWarPeriod.MUTUAL && inactivityCheckTask != null)
		{
			inactivityCheckTask.cancel(true);
			inactivityCheckTask = null;
		}

		// Do not need to update status if CW not started yet.
		if(period == ClanWarPeriod.PREPARATION)
		{
			return;
		}

		L2Clan clan = ClanTable.getInstance().getClan(attackerClanId);
		if(clan != null)
		{
			for(L2PcInstance member : clan.getOnlineMembers(-1))
			{
				member.broadcastUserInfo();
			}
		}

		clan = ClanTable.getInstance().getClan(opposingClanId);

		if(clan != null)
		{
			for(L2PcInstance member : clan.getOnlineMembers(-1))
			{
				member.broadcastUserInfo();
			}
		}
	}

	public void save()
	{
		ClanTable.getInstance().storeClanWar(this);
	}

	public static enum ClanWarPeriod
	{
		NEW,
		PREPARATION,
		MUTUAL,
		PEACE
	}

	public static enum WarProgress
	{
		VERY_LOW,
		LOW,
		NORMAL,
		HIGH,
		VERY_HIGH
	}

	public static enum ClanWarState
	{
		PREPARATION,
		REJECTED,
		MUTUAL,
		WIN,
		LOSS,
		TIE
	}
}
