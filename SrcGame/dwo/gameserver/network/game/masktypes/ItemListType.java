package dwo.gameserver.network.game.masktypes;

/**
 * User: GenCloud
 * Date: 17.01.2015
 * Team: DWO
 */
public enum ItemListType implements IUpdateTypeComponent
{
    AUGMENT_BONUS(0x01),
    ELEMENTAL_ATTRIBUTE(0x02),
    ENCHANT_EFFECT(0x04),
    VISUAL_ID(0x08);

    private final int _mask;

    private ItemListType(int mask)
    {
        _mask = mask;
    }

    @Override
    public int getMask()
    {
        return _mask;
    }
}
