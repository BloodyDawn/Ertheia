package dwo.gameserver.model.items.shapeshift;

import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import org.jdom2.Element;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 01.12.12
 * Time: 18:46
 */

public class ShapeShiftData
{
	private final CrystalGrade _grade;
	private final ShapeShiftingWindowType _shapeShiftingWindow;
	private final ShapeWindowType _shapeType;
	private final int _period;
	private final int _lookWeaponClassID;
	private final long _bodyPart;
	private final int _priceAdena;

	public ShapeShiftData(ShapeShiftingWindowType shapeShiftingWindow, Element element)
	{
		_grade = CrystalGrade.valueOf(element.getAttributeValue("grade"));
		_shapeType = ShapeWindowType.valueOf(element.getAttributeValue("shapeType"));
		_shapeShiftingWindow = shapeShiftingWindow;
		_period = Integer.parseInt(element.getAttributeValue("period"));
		_lookWeaponClassID = Integer.parseInt(element.getAttributeValue("lookWeaponClassID"));
		_bodyPart = ItemTable._slots.get(element.getAttributeValue("bodyPart", "none"));
		_priceAdena = Integer.parseInt(element.getAttributeValue("priceAdena"));
	}

	public CrystalGrade getGrade()
	{
		return _grade;
	}

	public ShapeShiftingWindowType getShapeShiftingWindow()
	{
		return _shapeShiftingWindow;
	}

	public ShapeWindowType getShapeType()
	{
		return _shapeType;
	}

	public int getPeriod()
	{
		return _period;
	}

	public int getLookWeaponClassID()
	{
		return _lookWeaponClassID;
	}

	public int getPriceAdena()
	{
		return _priceAdena;
	}

	public boolean checkBodyPart(int bodyPart)
	{
		return _bodyPart == 0 || _bodyPart != bodyPart;
	}
}