package dwo.gameserver.util;

import dwo.config.Config;

/**
 * User: GenCloud
 * Date: 06.01.2015
 * Team: DWO
 */
public class Colors
{
    private static final int COLORS[] = new int[766];

    public static int loadColors()
    {
        int pos = 0;

        for(int i = 255; i > 0; i--)
            COLORS[pos++] = ((i & 0xFF) << 0) + ((255 & 0xFF) << 8) + ((i & 0xFF) << 16);

        for(int i = 0; i < 255; i++)
            COLORS[pos++] = ((i & 0xFF) << 0) + ((255 & 0xFF) << 8) + ((0 & 0xFF) << 16);

        for(int i = 255; i >= 0; i--)
            COLORS[pos++] = ((255 & 0xFF) << 0) + ((i & 0xFF) << 8) + ((0 & 0xFF) << 16);

        return COLORS.length;
    }

    public static int getColor(int pvpKils)
    {
        pvpKils = (int) Math.ceil(pvpKils / Config.TITLE_PVP_MODE_RATE);
        if(pvpKils >= COLORS.length)
            return COLORS[COLORS.length - 1];
        return COLORS[pvpKils];
    }
}
