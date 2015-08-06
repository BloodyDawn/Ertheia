package dwo.gameserver.model.skills.base.proptypes;

/**
 * Documentation:<br>
 * This enum class holds the skill operative types:
 * <ul>
 * 	<li>A1 = Active Skill with "Instant Effect" (for example damage skills heal/pdam/mdam/cpdam skills).</li>
 * 	<li>A2 = Active Skill with "Continuous effect + Instant effect" (for example buff/debuff or damage/heal over time skills).</li>
 * 	<li>A3 = Active Skill with "Instant effect + Continuous effect"</li>
 * 	<li>A4 = Active Skill with "Instant effect + ?" used for special event herb.</li>
 * 	<li>CA1 = Continuous Active Skill with "instant effect" (instant effect casted by ticks).</li>
 * 	<li>CA5 = Continuous Active Skill with "continuous effect" (continuous effect casted by ticks).</li>
 * 	<li>DA1 = Directional Active Skill with "Charge/Rush instant effect"</li>
 * 	<li>DA2 = Directional Active Skill with "Charge/Rush Continuous effect"</li>
 * 	<li>P = Passive Skill</li>
 * 	<li>T = Toggle Skill</li>
 * </ul>
 * @author Zoey76
 */

public enum L2SkillOpType
{
	OP_ACTIVE,
	OP_PASSIVE,
	OP_TOGGLE,
	A1,
	A2,
	A3,
	A4,
	CA1,
	CA5,
	DA1,
	DA2,
	P,
	T,
	OP_ACTIVE_TOGGLE;

	/**
	 * @return {@code true} if the operative skill type is active, {@code false} otherwise.
	 */
	public boolean isActive()
	{
		switch(this)
		{
			case A1:
			case OP_ACTIVE_TOGGLE:
			case A3:
			case A4:
			case CA1:
			case CA5:
			case DA1:
			case DA2:
			case OP_ACTIVE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * @return {@code true} if the operative skill type is passive, {@code false} otherwise.
	 */
	public boolean isPassive()
	{
		return this == P || this == OP_PASSIVE;
	}

	/**
	 * @return {@code true} if the operative skill type is toggle, {@code false} otherwise.
	 */
	public boolean isToggle()
	{
		return this == OP_ACTIVE_TOGGLE || this == OP_TOGGLE || this == T;
	}
}
