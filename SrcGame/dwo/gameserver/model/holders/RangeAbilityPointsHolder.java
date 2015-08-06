package dwo.gameserver.model.holders;

/**
 * User: GenCloud
 * Date: 17.01.2015
 * Team: DWO
 */
public class RangeAbilityPointsHolder
{
    private final int _min;
    private final int _max;
    private final int _sp;

    public RangeAbilityPointsHolder(int min, int max, int sp)
    {
        _min = min;
        _max = max;
        _sp = sp;
    }

    /**
     * @return minimum value.
     */
    public int getMin()
    {
        return _min;
    }

    /**
     * @return maximum value.
     */
    public int getMax()
    {
        return _max;
    }

    /**
     * @return SP.
     */
    public int getSP()
    {
        return _sp;
    }
}
