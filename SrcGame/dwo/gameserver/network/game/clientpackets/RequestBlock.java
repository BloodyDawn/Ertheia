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

import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.BlockList;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.List;

public class RequestBlock extends L2GameClientPacket
{
	private static final int BLOCK = 0;
	private static final int UNBLOCK = 1;
	private static final int BLOCKLIST = 2;
	private static final int ALLBLOCK = 3;
	private static final int ALLUNBLOCK = 4;

	private String _name;
	private Integer _type;

	@Override
	protected void readImpl()
	{
		_type = readD(); //0x00 - block, 0x01 - unblock, 0x03 - allblock, 0x04 - allunblock

		if(_type == BLOCK || _type == UNBLOCK)
		{
			_name = readS();
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		int targetId = CharNameTable.getInstance().getIdByName(_name);
		int targetAL = CharNameTable.getInstance().getAccessLevelById(targetId);

		if(activeChar == null)
		{
			return;
		}

		List<String> blockCharNames;
		List<String> memos;

		switch(_type)
		{
			case BLOCK:
			case UNBLOCK:
				// can't use block/unblock for locating invisible characters
				if(targetId <= 0)
				{
					// Incorrect player name.
					activeChar.sendPacket(SystemMessageId.FAILED_TO_REGISTER_TO_IGNORE_LIST);
					return;
				}

				if(targetAL > 0)
				{
					// Cannot block a GM character.
					activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_GM);
					return;
				}

				if(activeChar.getObjectId() == targetId)
				{
					return;
				}

				if(_type == BLOCK)
				{
					RelationListManager.getInstance().addToBlockList(activeChar, targetId);
				}
				else
				{
					RelationListManager.getInstance().removeFromBlockList(activeChar, targetId);
				}

				blockCharNames = new FastList();
				memos = new FastList();
				for(int blockedId : RelationListManager.getInstance().getBlockList(activeChar.getObjectId()))
				{
					blockCharNames.add(CharNameTable.getInstance().getNameById(blockedId));
					memos.add(RelationListManager.getInstance().getRelationNote(activeChar.getObjectId(), blockedId));
				}
				activeChar.sendPacket(new BlockList(blockCharNames, memos));

				break;
			case BLOCKLIST:
				blockCharNames = new FastList();
				memos = new FastList();
				for(int blockedId : RelationListManager.getInstance().getBlockList(activeChar.getObjectId()))
				{
					blockCharNames.add(CharNameTable.getInstance().getNameById(blockedId));
					memos.add(RelationListManager.getInstance().getRelationNote(activeChar.getObjectId(), blockedId));
				}
				activeChar.sendPacket(new BlockList(blockCharNames, memos));
				break;
			case ALLBLOCK:
				activeChar.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);//Update by rocknow
				activeChar.setMessageRefusal(true);
				break;
			case ALLUNBLOCK:
				activeChar.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);//Update by rocknow
				activeChar.setMessageRefusal(false);
				break;
			default:
				_log.log(Level.INFO, "Unknown 0x0a block type: " + _type);
		}
	}

	@Override
	public String getType()
	{
		return "[C] A0 RequestBlock";
	}
}
