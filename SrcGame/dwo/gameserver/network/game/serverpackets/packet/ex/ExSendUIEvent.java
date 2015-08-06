/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import org.apache.commons.lang3.StringUtils;

public class ExSendUIEvent extends L2GameServerPacket
{
	private L2Object _player;
	private int _isHide;
	private int _isIncrease;
	private int _startTime;
	private int _endTime;
	private String _text;
	private int _npcStringId;
	private String[] _parameters;
	private int _points;

	private boolean isPointUI;

    public ExSendUIEvent(L2Object player, int isHide, int isIncrease, int startTime, int endTime, String text)
	{
		_player = player;
		_isHide = isHide;
		_isIncrease = isIncrease;
		_startTime = startTime;
		_endTime = endTime;
		_text = text;
		_npcStringId = -1;
	}

	public ExSendUIEvent(L2Object player, int isHide, int isIncrease, int startTime, int endTime, int npcStringId, String[] parameters)
	{
		_player = player;
		_isHide = isHide;
		_isIncrease = isIncrease;
		_startTime = startTime;
		_endTime = endTime;
		_text = null;
		_npcStringId = npcStringId;
		_parameters = parameters;
	}

	public ExSendUIEvent(L2Object player, int isHide, int isIncrease, int startTime, int endTime, NpcStringId npcString, String[] parameters)
	{
		_player = player;
		_isHide = isHide;
		_isIncrease = isIncrease;
		_startTime = startTime;
		_endTime = endTime;
		_text = null;
		_npcStringId = npcString.getId();
		_parameters = parameters;
	}

	public ExSendUIEvent(L2Object player, int status, int timeLeft, int points, int endTime, int npcStringId)
	{
		_player = player;
		_isHide = status;
		_isIncrease = timeLeft;
		_points = points;
		_endTime = endTime;
		_npcStringId = npcStringId;
		isPointUI = true;
	}

	@Override
	protected void writeImpl()
	{
		if(isPointUI)
		{
			writeD(_player.getObjectId());
			writeD(_isHide);
			writeD(0x00);
			writeD(0x00);
			writeS(String.valueOf(_isIncrease));
			writeS(String.valueOf(_points));
			writeS(String.valueOf(_endTime));
			writeS(StringUtils.EMPTY);
			writeS(StringUtils.EMPTY);
			writeD(_npcStringId);
		}
		else
		{
			writeD(_player.getObjectId());
			writeD(_isHide); // 0: show timer, 1: hide timer
			writeD(0x00); // unknown
			writeD(0x00); // unknown
			writeS(String.valueOf(_isIncrease)); // "0": count negative, "1": count positive
			writeS(String.valueOf(_startTime / 60)); // timer starting minute(s)
			writeS(String.valueOf(_startTime % 60)); // timer starting second(s)
			writeS(String.valueOf(_endTime / 60)); // timer length minute(s) (timer will disappear 10 seconds before it ends)
			writeS(String.valueOf(_endTime % 60)); // timer length second(s) (timer will disappear 10 seconds before it ends)
			writeD(_npcStringId);
			if(_npcStringId == -1)
			{
				writeS(_text); // text above timer
			}
			else if(_parameters != null)
			{
				for(String s : _parameters)
				{
					writeS(s);
				}
			}
		}
	}
}