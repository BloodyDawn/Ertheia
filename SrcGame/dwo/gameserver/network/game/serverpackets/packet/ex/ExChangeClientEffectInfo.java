package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * Created with IntelliJ IDEA.
 * User: Keiichi
 * Date: 11.12.12
 * Time: 23:20
 * To change this template use File | Settings | File Templates.
 */
public class ExChangeClientEffectInfo extends L2GameServerPacket
{
	private int changeZoneState;
	private int setPostEffect;
	private int onSetFogEffect;

	public ExChangeClientEffectInfo(int czs, int spe, int sfe)
	{
		czs = changeZoneState;
		spe = setPostEffect;
		sfe = onSetFogEffect;
	}

	@Override
	protected void writeImpl()
	{
		writeD(changeZoneState); // ?
		writeD(setPostEffect); // ?
		writeD(onSetFogEffect); // ?
	}
}
