package dwo.gameserver.model.holders;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 30.01.13
 * Time: 19:48
 */

public class VitalityHolder
{
	private int _vitalityPoints;
	private int _vitalityItems;

	public VitalityHolder(int vitalityPoints, int vitalityItems)
	{
		_vitalityPoints = vitalityPoints;
		_vitalityItems = vitalityItems;
	}

	public int getVitalityPoints()
	{
		return _vitalityPoints;
	}

	public void setVitalityPoints(int vitalityPoints)
	{
		_vitalityPoints = vitalityPoints;
	}

	public int getVitalityItems()
	{
		return _vitalityItems;
	}

	public void setVitalityItems(int vitalityItems)
	{
		_vitalityItems = vitalityItems;
	}
}