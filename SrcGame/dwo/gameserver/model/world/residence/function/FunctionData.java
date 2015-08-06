package dwo.gameserver.model.world.residence.function;

import org.jdom2.Element;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.01.13
 * Time: 20:11
 */

public class FunctionData
{
	private int _id;
	private String _name;
	private FunctionType _type;
	private int _level;
	private byte _depth;
	private double _funcValue;
	private FunctionCost _cost;

	public FunctionData(int id, Element element)
	{
		_id = id;
		_name = element.getAttributeValue("name");
		_type = FunctionType.values()[Integer.parseInt(element.getAttributeValue("type"))];
		_level = Integer.parseInt(element.getAttributeValue("level"));
		_depth = Byte.parseByte(element.getAttributeValue("depth"));

		_funcValue = element.getAttributeValue("funcValue") != null ? Double.parseDouble(element.getAttributeValue("funcValue")) : 0.0;

		_cost = new FunctionCost(Integer.parseInt(element.getAttributeValue("days")), Integer.parseInt(element.getAttributeValue("adena")));
	}

	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public FunctionType getType()
	{
		return _type;
	}

	public int getLevel()
	{
		return _level;
	}

	public byte getDepth()
	{
		return _depth;
	}

	public double getFuncValue()
	{
		return _funcValue;
	}

	public FunctionCost getFunctionCostData()
	{
		return _cost;
	}

	public int getPercent()
	{
		return (int) ((_funcValue < 1 ? _funcValue : _funcValue - 1) * 100);
	}
}