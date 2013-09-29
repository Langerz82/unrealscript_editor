package com.patrick_vane.unrealscript.editor.class_hierarchy;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.ViewPart;
import com.patrick_vane.unrealscript.editor.UnrealScriptEditor;
import com.patrick_vane.unrealscript.editor.class_hierarchy.parser.UnrealScriptClass;
import com.patrick_vane.unrealscript.editor.class_hierarchy.parser.UnrealScriptClassHierarchyParser;
import com.patrick_vane.unrealscript.editor.class_hierarchy.parser.UnrealScriptRootClass;
import com.patrick_vane.unrealscript.editor.constants.ProjectConstant;
import com.patrick_vane.unrealscript.editor.constants.UnrealScriptID;


public class TypeHierarchyView extends ViewPart
{
	private static TypeHierarchyViewTree				classHierarchyViewer;
	
	private static UnrealScriptRootClass				lastRoot;
	private static HashMap<String,UnrealScriptClass>	lastClasses 		= new HashMap<String,UnrealScriptClass>();
	private static File									lastSourceFolder;
	private static boolean								firstRunAfterStart 	= false;
	private static boolean								fileChanged			= false;
	
	private static boolean 								initialized 		= false;
	
	
	private static Thread updateThread = new Thread()
	{
		@Override
		public void run()
		{
			while( true )
			{
				File sourceFolder = null;
				try
				{
					sourceFolder = new File( UnrealScriptEditor.getSelectedOrActiveProjectFile().getAbsolutePath()+"/"+ProjectConstant.subfolders.get("UnrealScript") );
				}
				catch( Exception e )
				{
				}
				
				if( (sourceFolder != null) && sourceFolder.exists() )
				{
					if( firstRunAfterStart || fileChanged || (lastRoot == null) || (lastRoot.getChilds().size() == 0) || (lastSourceFolder == null) || !lastSourceFolder.equals(sourceFolder) )
					{
						final UnrealScriptRootClass root = UnrealScriptClassHierarchyParser.parse( sourceFolder );
						
						lastSourceFolder = sourceFolder;
						fileChanged = false;
						
						if( firstRunAfterStart || (lastRoot == null) || !lastRoot.equalsCompletely(root) )
						{
							lastRoot = root;
							firstRunAfterStart = false;
							
							setClassesHashSet( root );
							
							Display.getDefault().syncExec
							(
								new Runnable()
								{
									@Override
									public void run()
									{
										if( classHierarchyViewer != null )
										{
											Tree control = null;
											int horizontal = 0;
											int vertical = 0;
											if( classHierarchyViewer.getControl() instanceof Tree )
											{
												control = (Tree) classHierarchyViewer.getControl();
												if( control != null )
												{
													if( control.getHorizontalBar() != null )
														horizontal = control.getHorizontalBar().getSelection();
													if( control.getVerticalBar() != null )
														vertical = control.getVerticalBar().getSelection();
												}
											}
											
											Object[] expanded = classHierarchyViewer.getExpandedElements();
											ISelection selection = classHierarchyViewer.getSelection();
											
											classHierarchyViewer.setInput( root );
											classHierarchyViewer.collapseAll();
											classHierarchyViewer.refresh( true );
											
											classHierarchyViewer.setExpandedElements( expanded );
											classHierarchyViewer.setSelection( selection );
											
											if( control != null )
											{
												if( control.getHorizontalBar() != null )
													control.getHorizontalBar().setSelection( horizontal );
												if( control.getVerticalBar() != null )
													control.getVerticalBar().setSelection( vertical );
											}
											
											initialized = true;
										}
									}
								}
							);
						}
					}
				}
				
				try
				{
					Thread.sleep( 1000 );
				}
				catch( Exception e )
				{
				}
			}
		}
	};
	
	
	public static void init() // starts static{} block
	{
		updateThread.setDaemon( true );
		updateThread.start();
	}
	
	
	private static void setClassesHashSet( UnrealScriptRootClass unrealscriptRootClass )
	{
		lastClasses.clear();
		for( UnrealScriptClass child : unrealscriptRootClass.getChilds() )
		{
			addToClassesHashSet( child );
		}
	}
	private static void addToClassesHashSet( UnrealScriptClass unrealscriptClass )
	{
		lastClasses.put( unrealscriptClass.getName().toLowerCase(), unrealscriptClass );
		for( UnrealScriptClass child : unrealscriptClass.getChilds() )
		{
			addToClassesHashSet( child );
		}
	}
	
	
	@Override
	public void createPartControl( Composite parent )
	{
		classHierarchyViewer = new TypeHierarchyViewTree( parent, SWT.MULTI|SWT.H_SCROLL|SWT.V_SCROLL );
		classHierarchyViewer.setContentProvider( new TypeHierarchyContentProvider() );
		classHierarchyViewer.setLabelProvider( new TypeHierarchyLabelProvider() );
		classHierarchyViewer.setSorter( new TypeHierarchySorter() );
		classHierarchyViewer.addDoubleClickListener( new TypeHierarchyDoubleClickListener() );
		classHierarchyViewer.setAutoExpandLevel( 0 );
		firstRunAfterStart = true;
	}
	
	
	@Override
	public void setFocus()
	{
	}
	
	
	public static void fileChanged()
	{
		fileChanged = true;
	}
	
	public static UnrealScriptRootClass getRoot()
	{
		return lastRoot;
	}
	public static HashMap<String,UnrealScriptClass> getClasses()
	{
		return lastClasses;
	}
	public static File getRootSourceFolder()
	{
		return lastSourceFolder;
	}
	public static boolean isRootFromThisProject()
	{
		try
		{
			File projectFolder = new File( UnrealScriptEditor.getSelectedOrActiveProjectFile().getAbsolutePath()+"/"+ProjectConstant.subfolders.get("UnrealScript") );
			return projectFolder.equals( lastSourceFolder );
		}
		catch( Exception e )
		{
			return false;
		}
	}
	
	public static void show()
	{
		UnrealScriptEditor.showView( UnrealScriptID.VIEW_TYPE_HIERARCHY );
	}
	public static void waitForInitialize()
	{
		while( !initialized )
		{
			try
			{
				Thread.sleep( 100 );
			}
			catch( Exception e )
			{
			}
		}
	}
	
	
	public static void expandToClass( final UnrealScriptClass unrealscriptClass )
	{
		new Thread()
		{
			@Override
			public void run()
			{
				if( unrealscriptClass != null )
				{
					show();
					waitForInitialize();
					
					String className = unrealscriptClass.getName().toLowerCase();
					
					HashSet<String> parents = new HashSet<String>();
					UnrealScriptClass parent = unrealscriptClass.getParent();
					while( parent != null )
					{
						parents.add( parent.getName().toLowerCase() );
						parent = parent.getParent();
					}
					
					expand( parents, className.toLowerCase() );
				}
				
			}
		}.start();
	}
	private static void expand( final HashSet<String> parents, final String className )
	{
		Display.getDefault().syncExec
		(
			new Runnable()
			{
				@Override
				public void run()
				{
					if( classHierarchyViewer != null )
					{
						expand( lastRoot, parents, className );
					}
				}
			}
		);
	}
	private static void expand( UnrealScriptClass rootClass, HashSet<String> parents, String className )
	{
		if( rootClass != null )
		{
			for( UnrealScriptClass child : rootClass.getChilds() )
			{
				String name = child.getName().toLowerCase();
				if( name.equals(className) )
				{
					classHierarchyViewer.setSelection( child );
					return;
				}
				else if( parents.contains(name) )
				{
					parents.remove( name );
					expand( child, parents, className );
					return;
				}
			}
		}
	}
}
