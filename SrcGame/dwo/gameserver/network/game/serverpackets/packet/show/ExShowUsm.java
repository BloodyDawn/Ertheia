package dwo.gameserver.network.game.serverpackets.packet.show;

/*
 * Проигрывает USM ролик по ID из usmmoviedata
 */

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExShowUsm extends L2GameServerPacket
{
	public static int GD1_INTRO = 0x02;
	public static int Q001 = 1;
	public static int Q002 = 3;
	public static int Q003 = 4;
	public static int Q004 = 5;
	public static int Q005 = 6;
	public static int Q006 = 7;
	public static int Q007 = 8;
	public static int Q009 = 9;
	public static int Q010 = 10;
	public static int Q011 = 11;
	public static int Q012 = 12;
    public static int ARTEAS_FIRST_QUEST = 14;
	public static int AWAKE_1 = 139;
	public static int AWAKE_2 = 140;
	public static int AWAKE_3 = 141;
	public static int AWAKE_4 = 142;
	public static int AWAKE_5 = 143;
	public static int AWAKE_6 = 144;
	public static int AWAKE_7 = 145;
	public static int AWAKE_8 = 146;


    public static int INTRO_ARTEAS = 147;
    public static int INTRO_OTHERS = 148;
	private final int _param1;
	private final int _param2;
	private final int _param3;
	private int _movieId;

	public ExShowUsm(int movieId)
	{
		_movieId = movieId;
		_param1 = 0;
		_param2 = 0;
		_param3 = 0;
	}

	public ExShowUsm(int movieId, int param1, int param2, int param3)
	{
		_movieId = movieId;
		_param1 = param1;
		_param2 = param2;
		_param3 = param3;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_movieId);
		writeD(_param1);
		writeD(_param2);
		writeD(_param3);
	}
}
