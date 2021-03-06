package com.patrick_vane.unrealscript.editor.class_hierarchy;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.patrick_vane.unrealscript.editor.class_hierarchy.parser.UnrealScriptClass;


public class TypeHierarchyContentProvider implements ITreeContentProvider
{
	@Override
	public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
	{
	}
	
	
	@Override
	public Object[] getElements( Object inputElement )
	{
		Object[] children = getChildren( inputElement );
		if( children == null )
			return new Object[0];
		return children;
	}
	
	@Override
	public boolean hasChildren( Object element )
	{
		if( element instanceof UnrealScriptClass )
		{
			return (((UnrealScriptClass)element).getChilds().size() > 0);
		}
		return false;
	}
	
	@Override
	public Object[] getChildren( Object element )
	{
		if( element instanceof UnrealScriptClass )
		{
			return ((UnrealScriptClass)element).getChilds().toArray( new UnrealScriptClass[0] );
		}
		return new Object[0];
	}
	
	@Override
	public Object getParent( Object element )
	{
		if( element instanceof UnrealScriptClass )
		{
			return ((UnrealScriptClass)element).getParent();
		}
		return null;
	}
	
	
	@Override
	public void dispose()
	{
	}
}