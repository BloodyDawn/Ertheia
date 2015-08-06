package dwo.gameserver.model.actor.controller.character;

import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.restriction.IRestrictionChecker;
import dwo.gameserver.model.actor.restriction.RestrictionCheck;
import dwo.gameserver.model.actor.restriction.RestrictionCheckList;
import dwo.gameserver.model.world.zone.L2WorldRegion;
import dwo.gameserver.model.world.zone.Location;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author Yorie
 */
@RestrictionCheckList({RestrictionCheck.IN_NO_SUMMON_FRIEND_ZONE, RestrictionCheck.IN_BOSS_ZONE})
public class LocationController extends dwo.gameserver.model.actor.controller.object.LocationController implements IRestrictionChecker
{
	private final L2Character character;
	private Location lastKnownLocation;

	public LocationController(L2Character object)
	{
		super(object);
		character = object;
		character.getRestrictionController().addChecker(this);
	}

	@Override
	public boolean checkRestriction(RestrictionCheck check, Map<RestrictionCheck, Object> params)
	{
		switch(check)
		{
			case IN_NO_SUMMON_FRIEND_ZONE:
				return character.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND);
			case IN_BOSS_ZONE:
				return GrandBossManager.getInstance().getZone(character) != null;
		}
		return true;
	}

	@Override
	protected void onBadCoords()
	{
		character.getLocationController().decay();
	}

	/**
	 * L2Characters should be re-validated into world regions.
	 * @param value World region.
	 */
	@Override
	protected void setWorldRegion(L2WorldRegion value)
	{
		if(getWorldRegion() != null) // confirm revalidation of old region's zones
		{
			if(value != null)
			{
				getWorldRegion().revalidateZones(character);    // at world region change
			}
			else
			{
				getWorldRegion().removeFromZones(character);    // at world region change
			}
		}

		super.setWorldRegion(value);
	}

	/**
	 * Remembers current character location. Then this location can be used to teleport back.
	 */
	public void rememberLocation()
	{
		lastKnownLocation = new Location(getX(), getY(), getZ(), getHeading());
	}

	/**
	 * This method results into location that was reminded with using of rememberLocation method.
	 * @return Reminded location.
	 */
	@Nullable
	public Location getMemorizedLocation()
	{
		return lastKnownLocation;
	}

	/**
	 * Clears information about reminded location.
	 */
	public void forgetLocation()
	{
		lastKnownLocation = null;
	}

	/**
	 * Teleports object to reminded location if it exists.
	 */
	public void teleportToMemorizedLocation()
	{
		teleportToMemorizedLocation(false);
	}

	/**
	 * Allows using of random offset while teleporting to reminded location.
	 * @param allowRandomOffset If true, player will be teleported with small random offset.
	 */
	public void teleportToMemorizedLocation(boolean allowRandomOffset)
	{
		if(lastKnownLocation != null)
		{
			character.teleToLocation(lastKnownLocation.getX(), lastKnownLocation.getY(), lastKnownLocation.getZ(), allowRandomOffset);
		}
	}
}
