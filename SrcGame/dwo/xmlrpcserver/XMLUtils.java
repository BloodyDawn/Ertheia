package dwo.xmlrpcserver;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;

public class XMLUtils
{
	public static String serializePlayer(L2PcInstance pc, boolean full)
	{
		String result = "<char>";
		result += "<id>" + pc.getObjectId() + "</id>";
		result += "<name>" + pc.getName() + "</name>";
		result += "<level>" + pc.getLevel() + "</level>";
		result += "<class>" + pc.getActiveClassId() + "</class>";
		result += "<pvpkills>" + pc.getPvpKills() + "</pvpkills>";
		result += "<pkkills>" + pc.getPkKills() + "</pkkills>";
		result += "<noble>" + pc.isNoble() + "</noble>";
		result += "<hero>" + pc.getOlympiadController().isHero() + "</hero>";
		result += "<sex>" + (pc.getAppearance().getSex() ? "1" : "0") + "</sex>";
		result += "<race>" + pc.getRace().ordinal() + "</race>";
		result += "<face>" + pc.getAppearance().getFace() + "</face>";
		result += "<hairColor>" + pc.getAppearance().getHairColor() + "</hairColor>";
		result += "<hairStyle>" + pc.getAppearance().getHairStyle() + "</hairStyle>";
		result += "<clan_name>" + (pc.getClan() != null ? pc.getClan().getName() : "") + "</clan_name>";
		result += "<loc x=\"" + pc.getX() + "\" y=\"" + pc.getY() + "\" z=\"" + pc.getZ() + "\" />";
		if(full)
		{
			result += "<hp>" + pc.getCurrentHp() + "</hp>";
			result += "<cp>" + pc.getCurrentCp() + "</cp>";
			result += "<mp>" + pc.getCurrentMp() + "</mp>";
			result += "<sp>" + pc.getSp() + "</sp>";
			result += "<exp>" + pc.getExp() + "</exp>";
			result += "<items>";
			for(L2ItemInstance item : pc.getInventory().getItems())
			{
				result += "<item>";
				result += "<id>" + item.getItemId() + "</id>";
				result += "<object_id>" + item.getObjectId() + "</object_id>";
				result += "<name>" + item.getName() + "</name>";
				result += "<count>" + item.getCount() + "</count>";
				result += "<enchant>" + item.getEnchantLevel() + "</enchant>";
				result += "<slot>" + (item.isEquipped() ? item.getItem().getBodyPart() : -1) + "</slot>";
				result += "</item>";
			}
			result += "</items>";
		}
		result += "</char>";
		return result;
	}
}
