package dwo.gameserver.util.fileio.filters;

import java.io.File;
import java.io.FileFilter;

public class XmlFilter implements FileFilter
{
	@Override
	public boolean accept(File pathname)
	{
		return pathname.getName().endsWith(".xml");
	}
}
