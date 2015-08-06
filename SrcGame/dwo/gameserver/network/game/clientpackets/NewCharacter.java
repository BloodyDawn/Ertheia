package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.ClassTemplateTable;
import dwo.gameserver.model.actor.templates.L2PcTemplate;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.network.game.serverpackets.packet.lobby.NewCharacterSuccess;
import org.apache.log4j.Level;

public class NewCharacter extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

    @Override
    protected void runImpl()
    {
        if(Config.DEBUG)
        {
            _log.log(Level.DEBUG, "CreateNewChar");
        }

        NewCharacterSuccess ct = new NewCharacterSuccess();

        L2PcTemplate template = ClassTemplateTable.getInstance().getTemplate(0);
        ct.addChar(template);

        template = ClassTemplateTable.getInstance().getTemplate(ClassId.fighter);    // human fighter
        ct.addChar(template);

        template = ClassTemplateTable.getInstance().getTemplate(ClassId.mage);    // human mage
        ct.addChar(template);

        template = ClassTemplateTable.getInstance().getTemplate(ClassId.elvenFighter);    // elf fighter
        ct.addChar(template);

        template = ClassTemplateTable.getInstance().getTemplate(ClassId.elvenMage);    // elf mage
        ct.addChar(template);

        template = ClassTemplateTable.getInstance().getTemplate(ClassId.darkFighter);    // dark elf fighter
        ct.addChar(template);

        template = ClassTemplateTable.getInstance().getTemplate(ClassId.darkMage);    // dark elf mage
        ct.addChar(template);

        template = ClassTemplateTable.getInstance().getTemplate(ClassId.orcFighter);    // orc fighter
        ct.addChar(template);

        template = ClassTemplateTable.getInstance().getTemplate(ClassId.orcMage);    // orc mage
        ct.addChar(template);

        template = ClassTemplateTable.getInstance().getTemplate(ClassId.dwarvenFighter);    // dwarf fighter
        ct.addChar(template);

        template = ClassTemplateTable.getInstance().getTemplate(ClassId.maleSoldier); //kamael male soldier
        ct.addChar(template);

        template = ClassTemplateTable.getInstance().getTemplate(ClassId.femaleSoldier); // kamael female soldier
        ct.addChar(template);

        template = ClassTemplateTable.getInstance().getTemplate(ClassId.Arteas_Fighter); // arteas female soldier
        ct.addChar(template);

        template = ClassTemplateTable.getInstance().getTemplate(ClassId.Arteas_Wizard); // arteas female soldier
        ct.addChar(template);

        sendPacket(ct);
    }

	@Override
	public String getType()
	{
		return "[C] 0E NewCharacter";
	}
}
