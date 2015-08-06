package dwo.gameserver.network.game.masktypes;

import dwo.gameserver.model.items.itemcontainer.Inventory;

/**
 * User: GenCloud
 * Date: 17.01.2015
 * Team: DWO
 */
public enum InventorySlot implements IUpdateTypeComponent
{
    UNDER(Inventory.PAPERDOLL_UNDER),
    REAR(Inventory.PAPERDOLL_REAR),
    LEAR(Inventory.PAPERDOLL_LEAR),
    NECK(Inventory.PAPERDOLL_NECK),
    RFINGER(Inventory.PAPERDOLL_RFINGER),
    LFINGER(Inventory.PAPERDOLL_LFINGER),
    HEAD(Inventory.PAPERDOLL_HEAD),
    RHAND(Inventory.PAPERDOLL_RHAND),
    LHAND(Inventory.PAPERDOLL_LHAND),
    GLOVES(Inventory.PAPERDOLL_GLOVES),
    CHEST(Inventory.PAPERDOLL_CHEST),
    LEGS(Inventory.PAPERDOLL_LEGS),
    FEET(Inventory.PAPERDOLL_FEET),
    CLOAK(Inventory.PAPERDOLL_CLOAK),
    LRHAND(Inventory.PAPERDOLL_RHAND),
    HAIR(Inventory.PAPERDOLL_HAIR),
    HAIR2(Inventory.PAPERDOLL_HAIR2),
    RBRACELET(Inventory.PAPERDOLL_RBRACELET),
    LBRACELET(Inventory.PAPERDOLL_LBRACELET),
    DECO1(Inventory.PAPERDOLL_DECO1),
    DECO2(Inventory.PAPERDOLL_DECO2),
    DECO3(Inventory.PAPERDOLL_DECO3),
    DECO4(Inventory.PAPERDOLL_DECO4),
    DECO5(Inventory.PAPERDOLL_DECO5),
    DECO6(Inventory.PAPERDOLL_DECO6),
    BELT(Inventory.PAPERDOLL_BELT),
    BROOCH(Inventory.PAPERDOLL_BROACH),
    BROOCH_JEWEL(Inventory.PAPERDOLL_STONE1),
    BROOCH_JEWEL2(Inventory.PAPERDOLL_STONE2),
    BROOCH_JEWEL3(Inventory.PAPERDOLL_STONE3),
    BROOCH_JEWEL4(Inventory.PAPERDOLL_STONE4),
    BROOCH_JEWEL5(Inventory.PAPERDOLL_STONE5),
    BROOCH_JEWEL6(Inventory.PAPERDOLL_STONE6);

    private final long _pDollSlot;

    private InventorySlot(long pDollSlot)
    {
        _pDollSlot = pDollSlot;
    }

    public long getSlot()
    {
        return _pDollSlot;
    }

    @Override
    public int getMask()
    {
        return ordinal();
    }
}
