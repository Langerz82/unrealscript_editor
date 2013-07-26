package com.patrick_vane.unrealscript.editor.constants;

import java.util.HashMap;


public class ProjectConstant
{
	public static final HashMap<String,String>		subfolders			= new HashMap<String,String>();
	public static final HashMap<String,String[]>	hiddensubfolders	= new HashMap<String,String[]>();
	
	static
	{
		addFolder( "BaseConfig", "Engine/Config" );
		addFolder( "Config", "UDKGame/Config" );
		addFolder( "Content", "UDKGame/Content", new String[]{"Maps"} );
		addFolder( "DLLs", "Binaries/Win32/UserCode" );
		addFolder( "Logs", "UDKGame/Logs" );
		addFolder( "Flash", "UDKGame/Flash" );
		addFolder( "Maps", "UDKGame/Content/Maps" );
		addFolder( "Movies", "UDKGame/Movies" );
		addFolder( "UnrealScript", "Development/Src" );
	}
	
	private static void addFolder( String name, String path )
	{
		subfolders.put( name, path );
	}
	private static void addFolder( String name, String path, String[] hide )
	{
		subfolders.put( name, path );
		hiddensubfolders.put( path, hide );
	}
}
