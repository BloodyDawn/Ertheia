package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.util.StringUtil;

import java.util.List;

public class ShowBoard extends L2GameServerPacket
{
	public static final String TYPE_101 = "101";
	public static final String TYPE_102 = "102";
	public static final String TYPE_103 = "103";
	public static final String TYPE_104 = "104";
	private StringBuilder _htmlCode;

	public ShowBoard(String htmlCode, String id)
	{
		_htmlCode = StringUtil.startAppend(500, id, "\u0008", htmlCode);
	}

	public ShowBoard(List<String> arg)
	{
		_htmlCode = StringUtil.startAppend(500, "1002\u0008");
		for(String str : arg)
		{
			StringUtil.append(_htmlCode, str, " \u0008");
		}
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x01); //c4 1 to show community 00 to hide
		writeS("bypass _bbshome"); // top
		writeS("bypass _bbsgetfav"); // favorite
		writeS("bypass _bbsloc"); // region
		writeS("bypass _bbsclan"); // clan
		writeS("bypass _bbsmemo"); // memo
		writeS("bypass _bbsmail"); // mail
		writeS("bypass _bbsfriends"); // friends
		writeS("bypass bbs_add_fav"); // add fav.
		if(_htmlCode.length() < 8192)
		{
			writeS(_htmlCode.toString());
		}
		else
		{
			writeS("<html><body>Html is too long!</body></html>");
		}
	}
}