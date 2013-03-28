package com.patrick_vane.unrealscript.editor.outline;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import com.patrick_vane.unrealscript.editor.UnrealScriptEditor;
import com.patrick_vane.unrealscript.editor.UnrealScriptID;
import com.patrick_vane.unrealscript.editor.parser.CodeAttribute;


public class OutlineSorter extends ViewerSorter
{
	@Override
	public int compare( Viewer viewer, Object e1, Object e2 )
	{
		if( isSortingOff() )
			return 0;
		
		if( (e1 instanceof CodeAttribute) && (e2 instanceof CodeAttribute) )
		{
			CodeAttribute a1 = (CodeAttribute) e1;
			CodeAttribute a2 = (CodeAttribute) e2;
			if( !a1.isFunction() && a2.isFunction() )
				return -1;
			if( a1.isFunction() && !a2.isFunction() )
				return 1;
			return a1.getName().compareTo( a2.getName() );
		}
		
		if( e1 == null )
			return 1;
		if( e2 == null )
			return -1;
		
		return e1.toString().compareTo( e2.toString() );
	}
	
	
	private static boolean isSortingOff()
	{
		try
		{
			return Boolean.valueOf( UnrealScriptEditor.getRoot().getPersistentProperty(UnrealScriptID.PROPERTY_SORT_ALPHABETIC) );
		}
		catch( CoreException e )
		{
		}
		return false;
	}
}
