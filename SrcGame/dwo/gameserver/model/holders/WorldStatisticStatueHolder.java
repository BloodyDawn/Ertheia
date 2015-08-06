package dwo.gameserver.model.holders;

import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.worldstat.CategoryType;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.sql.ResultSet;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class WorldStatisticStatueHolder extends L2NpcTemplate
{
	private static final Logger _log = Logger.getLogger(WorldStatisticStatueHolder.class);
	private static final StatsSet stats = new StatsSet();

	static
	{
		stats.set("npcId", 0);
		stats.set("idTemplate", 0);
		stats.set("type", 0);
		stats.set("name", "");
		stats.set("server_side_name", false);
		stats.set("title", "");
		stats.set("server_side_title", false);
		stats.set("sex", "");
		stats.set("level", 0);
		stats.set("exp", 0);
		stats.set("sp", 0);
		stats.set("slot_rhand", 0);
		stats.set("slot_lhand", 0);
		stats.set("enchant", 0);
		stats.set("drop_herb_group", 0);
		stats.set("client_class", "");
	}

	private CategoryType _cat;
	private int _classId;
	private int _raceId;
	private int _hairStyle;
	private int _hairColor;
	private int _face;
	private int _socialId;
	private int _necklace;
	private int _head;
	private int _rhand;
	private int _lhand;
	private int _gloves;
	private int _chest;
	private int _legs;
	private int _feet;
	private int _accessory1;
	private int _accessory2;

	public WorldStatisticStatueHolder(ResultSet set)
	{
		super(stats);
		try
		{
			_cat = CategoryType.getCategoryById(set.getInt("cat_id"));
			_classId = set.getInt("class_id");
			_name = set.getString("char_name");
			_raceId = set.getInt("race_id");
			_sex = set.getString("sex");
			_hairStyle = set.getInt("hair_style");
			_hairColor = set.getInt("hair_color");
			_face = set.getInt("face");
			_socialId = set.getInt("social_id");
			_necklace = set.getInt("necklace");
			_head = set.getInt("head");
			_rhand = set.getInt("rhand");
			_lhand = set.getInt("lhand");
			_gloves = set.getInt("gloves");
			_chest = set.getInt("chest");
			_legs = set.getInt("legs");
			_feet = set.getInt("feet");
			_accessory1 = set.getInt("accessory1");
			_accessory2 = set.getInt("accessory2");
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "Cannot instantinate world statistic statue holder", e);
		}
	}

	public CategoryType getCategory()
	{
		return _cat;
	}

	public int getClassId()
	{
		return _classId;
	}

	public int getRaceId()
	{
		return _raceId;
	}

	public int getSocialId()
	{
		return _socialId;
	}

	public int getHairStyle()
	{
		return _hairStyle;
	}

	public int getFace()
	{
		return _face;
	}

	public int getHairColor()
	{
		return _hairColor;
	}

	public int getNecklace()
	{
		return _necklace;
	}

	public int getHead()
	{
		return _head;
	}

	public int getLegs()
	{
		return _legs;
	}

	public int getGloves()
	{
		return _gloves;
	}

	public int getArmor()
	{
		return _chest;
	}

	public int getFeet()
	{
		return _feet;
	}

	public int getAccessory(int type)
	{
		return type == 1 ? _accessory1 : _accessory2;
	}

	@Override
	public int getLeftHand()
	{
		return _lhand;
	}

	@Override
	public int getRightHand()
	{
		return _rhand;
	}
}
