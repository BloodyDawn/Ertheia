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
package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.model.player.L2SecondaryAuth;
import dwo.gameserver.network.game.serverpackets.packet.lobby.Ex2NDPasswordAck;

/**
 * (ch)cS{S}
 * c: change pass?
 * S: current password
 * S: new password
 *
 * @author mrTJO
 */
public class RequestEx2ndPasswordReq extends L2GameClientPacket
{
	int _changePass;
	String _password;
	String _newPassword;

	@Override
	protected void readImpl()
	{
		_changePass = readC();
		_password = readS();
		if(_changePass == 2)
		{
			_newPassword = readS();
		}
	}

	@Override
	protected void runImpl()
	{
		if(!Config.SECOND_AUTH_ENABLED)
		{
			return;
		}

		L2SecondaryAuth spa = getClient().getSecondaryAuth();
		boolean exVal = false;

		if(_changePass == 0 && !spa.isPasswordExist())
		{
			exVal = spa.savePassword(_password);
		}
		else if(_changePass == 2 && spa.isPasswordExist())
		{
			exVal = spa.changePassword(_password, _newPassword);
		}

		if(exVal)
		{
			getClient().sendPacket(new Ex2NDPasswordAck(Ex2NDPasswordAck.SUCCESS));
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:AF RequestEx2ndPasswordReq";
	}
}
