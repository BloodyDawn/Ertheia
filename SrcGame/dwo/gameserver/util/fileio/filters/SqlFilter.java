package dwo.gameserver.util.fileio.filters;

import java.io.File;
import java.io.FileFilter;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 24.03.12
 * Time: 15:34
 */

public class SqlFilter implements FileFilter
{
	@Override
	public boolean accept(File pathname)
	{
		return pathname.getName().endsWith(".sql");
	}
}
