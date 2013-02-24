package com.patrick_vane.unrealscript.editor;

import java.io.File;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import com.patrick_vane.unrealscript.editor.class_hierarchy.ClassHierarchyContentProvider;
import com.patrick_vane.unrealscript.editor.class_hierarchy.ClassHierarchyLabelProvider;
import com.patrick_vane.unrealscript.editor.class_hierarchy.parser.UnrealScriptClassHierarchyParser;
import com.patrick_vane.unrealscript.editor.constants.TagConstant;
import com.patrick_vane.unrealscript.editor.default_classes.DoubleClickStrategy;
import com.patrick_vane.unrealscript.editor.executable.SaveOnResourceChangesListener;
import com.patrick_vane.unrealscript.editor.extra.AutoEditStrategy;
import com.patrick_vane.unrealscript.editor.extra.ContentAssistant;
import com.patrick_vane.unrealscript.editor.extra.TextHover;
import com.patrick_vane.unrealscript.editor.syntaxcolor.UnrealScriptSyntaxColor;


public class Configuration extends SourceViewerConfiguration
{
	private DoubleClickStrategy				doubleClickStrategy;
	private TreeViewer						classHierarchyViewer;
	private SaveOnResourceChangesListener	saveOnResourceChangesListener;
	
	
	public Configuration()
	{
		doubleClickStrategy = new DoubleClickStrategy();
		
		classHierarchyViewer = new TreeViewer( UnrealScriptClassHierarchyParser.parse(new File("C:/UDK/00_Test/Development/Src/")), SWT.MULTI|SWT.H_SCROLL|SWT.V_SCROLL );
		classHierarchyViewer.setContentProvider( new ClassHierarchyContentProvider() );
		classHierarchyViewer.setLabelProvider( new ClassHierarchyLabelProvider() );
		classHierarchyViewer.setAutoExpandLevel( 2 );
		//classHierarchyViewer.setInput( getInitalInput() );
		classHierarchyViewer.expandAll();
		
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					IProject project 				= UnrealScriptEditor.getActiveProject();
					IFolder scriptFolder 			= project.getFolder( "UnrealScript" );
					File rootFile 					= new File( scriptFolder.getLocationURI() );
					saveOnResourceChangesListener 	= new SaveOnResourceChangesListener( rootFile );
					saveOnResourceChangesListener.start();
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	
	@Override
	public IPresentationReconciler getPresentationReconciler( ISourceViewer sourceViewer )
	{
		return UnrealScriptSyntaxColor.createPresentationReconciler( sourceViewer );
	}
	@Override
	public String[] getConfiguredContentTypes( ISourceViewer sourceViewer )
	{
		return TagConstant.TAGS;
	}
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy( ISourceViewer sourceViewer, String contentType )
	{
		return doubleClickStrategy;
	}
	
	@Override
	public IAnnotationHover getAnnotationHover( ISourceViewer sourceViewer )
	{
		return new DefaultAnnotationHover();
	}
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies( ISourceViewer sourceViewer, String contentType )
	{
		return new IAutoEditStrategy[]{ new AutoEditStrategy() };
	}
	@Override
	public IContentAssistant getContentAssistant( ISourceViewer sourceViewer )
	{
		return new ContentAssistant();
	}
	
	@Override
	public ITextHover getTextHover( ISourceViewer sourceViewer, String contentType )
	{
		return new TextHover();
	}
	@Override
	public ITextHover getTextHover( ISourceViewer sourceViewer, String contentType, int stateMask )
	{
		return new TextHover();
	}
}