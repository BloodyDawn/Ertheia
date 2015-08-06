package dwo.gameserver.model.actor.restriction;

/**
 * @author Yorie
 */
public class RestrictionResponse
{
	public static final RestrictionResponse DEFAULT = new RestrictionResponse(RestrictionCheck.SUCCESS);
	private final Class<?> cls;
	private final RestrictionCheck reason;

	public RestrictionResponse(RestrictionCheck reason)
	{
		cls = null;
		this.reason = reason;
	}

	public RestrictionResponse(Class<?> cls, RestrictionCheck reason)
	{
		this.cls = cls;
		this.reason = reason;
	}

	public boolean passed()
	{
		return reason == RestrictionCheck.SUCCESS;
	}

	public RestrictionCheck getReason()
	{
		return reason;
	}

	public Class<?> getRestrictedClass()
	{
		return cls;
	}
}
