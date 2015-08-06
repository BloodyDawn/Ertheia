package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

import java.util.List;

public class ExShowTrace extends L2GameServerPacket
{
	private final List<Trace> _traces = new FastList<>();

	public void addTrace(int x, int y, int z, int time)
	{
		_traces.add(new Trace(x, y, z, time));
	}

	public void addTrace(L2Object obj, int time)
	{
		addTrace(obj.getX(), obj.getY(), obj.getZ(), time);
	}

	@Override
	protected void writeImpl()
	{
		writeH(_traces.size());
		for(Trace t : _traces)
		{
			writeD(t._x);
			writeD(t._y);
			writeD(t._z);
			writeH(t._time);
		}
	}

	static class Trace
	{
		public final int _x;
		public final int _y;
		public final int _z;
		public final int _time;

		public Trace(int x, int y, int z, int time)
		{
			_x = x;
			_y = y;
			_z = z;
			_time = time;
		}
	}
}
