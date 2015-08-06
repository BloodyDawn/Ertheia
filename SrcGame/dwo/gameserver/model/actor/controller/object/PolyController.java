package dwo.gameserver.model.actor.controller.object;

import dwo.gameserver.model.actor.L2Object;

/**
 * Object polymorphism controller.
 * That controller represents polymorphic states of any L2Object.
 * Allows create polymorphism on objects with different types.
 *
 * @author GODWORLD
 * @author Yorie
 */
public class PolyController extends L2ObjectController
{
	private int id;
	private PolyType type;

	public PolyController(L2Object object)
	{
		super(object);
	}

	/**
	 * Sets up poly type & ID.
	 * The type is basically type of polymorphic object like a NPC or item.
	 * The ID is primary attribute for polymorphism: NPC ID, item ID and etc.
	 *
	 * @param polyType Poly type.
	 * @param polyId Poly ID.
	 */
	public void setInfo(PolyType polyType, int polyId)
	{
		id = polyId;
		type = polyType;
	}

	/**
	 * Removes polymorphism info from controller.
	 */
	public void clearInfo()
	{
		type = PolyType.NONE;
		id = 1;
	}

	/**
	 * @return True if current object is in polymorphism.
	 */
	public boolean isMorphed()
	{
		return type != null;
	}

	/**
	 * @return Poly ID.
	 */
	public int getPolyId()
	{
		return id;
	}

	/**
	 * Sets up poly ID.
	 * @param value Poly ID.
	 * @see @PolyController#setInfo
	 */
	public void setPolyId(int value)
	{
		id = value;
	}

	/**
	 * @return Polymorphism type.
	 */
	public PolyType getPolyType()
	{
		return type;
	}

	/**
	 * Sets up polymorphism type.
	 * @param type Poly type.
	 */
	public void setPolyType(PolyType type)
	{
		this.type = type;
	}

	/**
	 * Type of polymorphism.
	 * @author Yorie
	 */
	public static enum PolyType
	{
		NONE,
		NPC,
		ITEM
	}
}
