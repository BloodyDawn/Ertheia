package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.datatables.sql.PetNameTable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.network.game.components.SystemMessageId;

import java.util.List;

public class RequestChangePetName extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		// Находим пета.
		List<L2Summon> pets = activeChar.getPets();

		L2PetInstance pet = null;
		for(L2Summon summon : pets)
		{
			if(summon.isPet())
			{
				pet = summon.getPetInstance();
				break;
			}
		}

		if(pet == null)
		{
			activeChar.sendPacket(SystemMessageId.NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET);
			return;
		}

		if(pet.getName() != null)
		{
			activeChar.sendPacket(SystemMessageId.NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET);
			return;
		}
		if(PetNameTable.getInstance().doesPetNameExist(_name, pet.getTemplate().getNpcId()))
		{
			activeChar.sendPacket(SystemMessageId.NAMING_ALREADY_IN_USE_BY_ANOTHER_PET);
			return;
		}
		if(_name.length() < 3 || _name.length() > 16)
		{
			// activeChar.sendPacket(SystemMessage.NAMING_PETNAME_UP_TO_8CHARS);
			activeChar.sendMessage("Длина имени питомца не должно превышать 16 символов."); // TODO: retail
			return;
		}
		if(!PetNameTable.getInstance().isValidPetName(_name))
		{
			activeChar.sendPacket(SystemMessageId.NAMING_PETNAME_CONTAINS_INVALID_CHARS);
			return;
		}

		pet.setName(_name);
		pet.updateAndBroadcastStatus(1);
	}

	@Override
	public String getType()
	{
		return "[C] 89 RequestChangePetName";
	}
}