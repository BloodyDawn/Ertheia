package dwo.gameserver.model.player.formation.group;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 30.07.12
 * Time: 20:48
 */

public enum PartyLootType
{
	ITEM_LOOTER(487),
	ITEM_RANDOM(488),
	ITEM_RANDOM_SPOIL(798),
	ITEM_ORDER(799),
	ITEM_ORDER_SPOIL(800);

	private final int _sysMsgId;

	private PartyLootType(int sysMsgId)
	{
		_sysMsgId = sysMsgId;
	}

	public int getSysMsgId()
	{
		return _sysMsgId;
	}
}