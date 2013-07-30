package com.patrick_vane.unrealscript.editor;

import java.io.File;
import java.util.HashMap;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import com.patrick_vane.unrealscript.editor.class_hierarchy.TypeHierarchyView;
import com.patrick_vane.unrealscript.editor.constants.TagConstant;
import com.patrick_vane.unrealscript.editor.default_classes.DoubleClickStrategy;
import com.patrick_vane.unrealscript.editor.extra.AutoEditStrategy;
import com.patrick_vane.unrealscript.editor.extra.CodeCompleter;
import com.patrick_vane.unrealscript.editor.extra.CodeFormatter;
import com.patrick_vane.unrealscript.editor.extra.TextHover;
import com.patrick_vane.unrealscript.editor.hyperlink_ctrl.HyperlinkDetector;
import com.patrick_vane.unrealscript.editor.outline.OutlineContentPage;
import com.patrick_vane.unrealscript.editor.syntaxcolor.UnrealScriptSyntaxColor;


public class Configuration extends SourceViewerConfiguration
{
	private static HashMap<File,FileChangesListener>	saveOnResourceChangesListeners	= new HashMap<File,FileChangesListener>();
	
	private final UnrealScriptEditor					editor;
	
	private OutlineContentPage							outlineContentPage;
	
	
	public Configuration( UnrealScriptEditor editor )
	{
		this.editor = editor;
		
		final Configuration THIS = this;
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					IProject project 		= UnrealScriptEditor.getActiveProject();
					IFolder scriptFolder 	= project.getFolder( "UnrealScript" );
					File rootFile 			= new File( scriptFolder.getLocationURI() );
					if( rootFile.exists() )
					{
						synchronized( saveOnResourceChangesListeners )
						{
							if( saveOnResourceChangesListeners.get(rootFile) == null )
							{
								FileChangesListener saveOnResourceChangesListener = new FileChangesListener( THIS, project, rootFile );
								saveOnResourceChangesListener.start();
								saveOnResourceChangesListeners.put( rootFile, saveOnResourceChangesListener );
							}
						}
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	
	public Object getAdapter( UnrealScriptEditor editor, Class required )
	{
		if( IContentOutlinePage.class.equals(required) )
		{
			if( outlineContentPage == null )
			{
				outlineContentPage = new OutlineContentPage( UnrealScriptEditor.getIFile(editor), editor.getEditorInput() );
			}
			return outlineContentPage;
		}
		return null;
	}
	
	
	public void fileChanged()
	{
		TypeHierarchyView.fileChanged();
		if( outlineContentPage != null )
		{
			outlineContentPage.update();
		}
	}
	
	
	@Override
	public IContentFormatter getContentFormatter( ISourceViewer sourceViewer )
	{
		MultiPassContentFormatter formatter = new MultiPassContentFormatter( getConfiguredDocumentPartitioning(sourceViewer), IDocument.DEFAULT_CONTENT_TYPE );
		formatter.setMasterStrategy( new CodeFormatter() );
		return formatter;
	}
	
	@Override
	public IUndoManager getUndoManager( ISourceViewer sourceViewer )
	{
		IUndoManager manager = super.getUndoManager( sourceViewer );
		manager.setMaximalUndoLevel( editor.getUndoHistorySize() );
		return manager;
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
		return new DoubleClickStrategy();
	}
	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors( ISourceViewer sourceViewer )
	{
		return new HyperlinkDetector[]{ new HyperlinkDetector() };
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
		ContentAssistant assistant = new ContentAssistant();
		assistant.setContentAssistProcessor( new CodeCompleter(), IDocument.DEFAULT_CONTENT_TYPE );
		return assistant;
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