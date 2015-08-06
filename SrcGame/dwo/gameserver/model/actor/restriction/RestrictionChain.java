package dwo.gameserver.model.actor.restriction;

import javolution.util.FastList;

import java.util.Collections;
import java.util.List;

/**
 * Restriction engine chains.
 * Chains is set of checks that will be checked by registered checkers. Checker is class that implements @IRestrictionChecker interface.
 * Chains consist of two types of check sets: negative checks and positive checks.
 * Negative checks is condition that should not be true to pass chain.
 * Positive checks is checks that should be true to pass chain.
 *
 * Example:
 *      FOO({MARRIED}, {PARTICIPATING_OLYMPIAD, PARTICIPATING_COMBAT, DEAD})
 *      This should equals next logical checks:
 *          - Player isn't olympiad member          [negative check PARTICIPATING_OLYMPIAD];
 *          - Player isn't in combat                [negative check PARTICIPATING_COMBAT];
 *          - Player isn't dead                     [negative check DEAD];
 *          - Player is married                     [positive check MARRIED].
 *      If one from this checks fails, then fails whole chain.
 *
 * Chains can be inherited, i.e. you could use one chain to set up new chain with checks from first chain.
 * Example:
 *      FOO({MARRIED}, {PARTICIPATING_OLYMPIAD}),
 *      BAR(FOO, {FLYING}, {DEAD})
 *      In this example, BAR chain consist of next checks:
 *          - Player should be married              [inherited from FOO chain];
 *          - Player should not participate in olly [inherited from FOO chain];
 *          - Player should fly                     [positive check FLYING];
 *          - Player shouln't be dead               [negative check DEAD].
 *
 * @author Yorie
 */
public enum RestrictionChain
{
	CUSTOM_SERVICE(new RestrictionCheck[]{}, new RestrictionCheck[]{
		RestrictionCheck.PARTICIPATING_OLYMPIAD, RestrictionCheck.PRISONER, RestrictionCheck.CASTING,
		RestrictionCheck.ATTACKING, RestrictionCheck.DEAD, RestrictionCheck.PARTICIPATING_SIEGE,
		RestrictionCheck.PARTICIPATING_COMBAT, RestrictionCheck.BAD_REPUTATION
	}),

	SUMMON_PLAYER(new RestrictionCheck[]{}, new RestrictionCheck[]{
		RestrictionCheck.FLYING, RestrictionCheck.IN_NO_SUMMON_FRIEND_ZONE, RestrictionCheck.PARTICIPATING_OLYMPIAD,
		RestrictionCheck.PARTICIPATING_COMBAT
	}),

	SUMMON_AGATHION(new RestrictionCheck[]{}, new RestrictionCheck[]{
		RestrictionCheck.PARTICIPATING_OLYMPIAD
	}),

	USE_SCROLL(new RestrictionCheck[]{}, new RestrictionCheck[]{
		RestrictionCheck.PRISONER, RestrictionCheck.IN_BOSS_ZONE, RestrictionCheck.CASTING, RestrictionCheck.CAN_MOVE,
		RestrictionCheck.CAN_CAST, RestrictionCheck.DEAD, RestrictionCheck.PARTICIPATING_OLYMPIAD,
		RestrictionCheck.OBSERVING, RestrictionCheck.COMBAT_FLAG_EQUPPIED
	}),

	FELL_IN_LOVE(new RestrictionCheck[]{
		RestrictionCheck.MARRIED, RestrictionCheck.PARTICIPATING_SAME_INSTANCE
	}, new RestrictionCheck[]{
		RestrictionCheck.IN_BOSS_ZONE, RestrictionCheck.COMBAT_FLAG_EQUPPIED, RestrictionCheck.ONLINE,
		RestrictionCheck.PRISONER, RestrictionCheck.PARTICIPATING_DUEL, RestrictionCheck.PARTICIPATING_OLYMPIAD,
		RestrictionCheck.OBSERVING, RestrictionCheck.PARTICIPATING_SIEGE, RestrictionCheck.IN_NO_SUMMON_FRIEND_ZONE,
		RestrictionCheck.PARTICIPATING_EVENT
	}),
	CAN_MAKE_SOCIAL_ACTIONS(new RestrictionCheck[]{}, new RestrictionCheck[]{
		RestrictionCheck.TRADING, RestrictionCheck.DEAD, RestrictionCheck.PARTICIPATING_DUEL,
		RestrictionCheck.IMMOBILIZED, RestrictionCheck.CASTING, RestrictionCheck.CASTING_SIMULTANEOUSLY,
		RestrictionCheck.PARTICIPATING_COMBAT, RestrictionCheck.PARTICIPATING_OLYMPIAD, RestrictionCheck.OBSERVING
	});

	private final List<RestrictionCheck> positiveChecks = new FastList<>();
	private final List<RestrictionCheck> negativeChecks = new FastList<>();

	RestrictionChain(RestrictionCheck[] positiveChecks)
	{
		Collections.addAll(this.positiveChecks, positiveChecks);
	}

	RestrictionChain(RestrictionCheck[] positiveChecks, RestrictionCheck[] negativeChecks)
	{
		Collections.addAll(this.positiveChecks, positiveChecks);
		Collections.addAll(this.negativeChecks, negativeChecks);
	}

	RestrictionChain(RestrictionChain parent, RestrictionCheck[] positiveChecks, RestrictionCheck[] negativeChecks)
	{
		this.positiveChecks.addAll(parent.positiveChecks);
		this.negativeChecks.addAll(parent.negativeChecks);
		Collections.addAll(this.negativeChecks, negativeChecks);
		Collections.addAll(this.positiveChecks, positiveChecks);
	}

	public List<RestrictionCheck> getPositiveChecks()
	{
		return positiveChecks;
	}

	public List<RestrictionCheck> getNegativeChecks()
	{
		return negativeChecks;
	}
}
