package com.patrick_vane.unrealscript.editor;

import java.io.File;
import java.util.HashMap;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import com.patrick_vane.unrealscript.editor.class_hierarchy.TypeHierarchyView;
import com.patrick_vane.unrealscript.editor.constants.TagConstant;
import com.patrick_vane.unrealscript.editor.constants.UnrealScriptID;
import com.patrick_vane.unrealscript.editor.default_classes.DoubleClickStrategy;
import com.patrick_vane.unrealscript.editor.default_classes.HoverInformationControl;
import com.patrick_vane.unrealscript.editor.extra.AutoEditStrategy;
import com.patrick_vane.unrealscript.editor.extra.CodeCompleter;
import com.patrick_vane.unrealscript.editor.extra.CodeFormatter;
import com.patrick_vane.unrealscript.editor.extra.HyperlinkDetector;
import com.patrick_vane.unrealscript.editor.extra.TextHover;
import com.patrick_vane.unrealscript.editor.outline.OutlineContentPage;
import com.patrick_vane.unrealscript.editor.syntaxcolor.UnrealScriptSyntaxColor;


public class Configuration extends SourceViewerConfiguration
{
	private static HashMap<File,FileChangesListener>	saveOnResourceChangesListeners	= new HashMap<File,FileChangesListener>();
	
	private final UnrealScriptEditor					editor;
	
	private ContentAssistant							contentAssistant				= new ContentAssistant();
	private OutlineContentPage							outlineContentPage;
	
	
	public Configuration( final UnrealScriptEditor editor )
	{
		this.editor = editor;
		
		final Configuration THIS = this;
		new Thread()
		{
			@Override
			public void run()
			{
				editor.waitForInitialization();
				
				
				Display.getDefault().syncExec
				(
					new Runnable()
					{
						@Override
						public void run()
						{
							// content assistant >>
								CodeCompleter codeCompleter = new CodeCompleter();
								for( String tag : TagConstant.TAGS )
								{
									contentAssistant.setContentAssistProcessor( codeCompleter, tag );
								}
								
								if( UnrealScriptEditor.getActivatorPreferenceStore() != null )
								{
									contentAssistant.enableAutoActivation( UnrealScriptEditor.getActivatorPreferenceStore().getBoolean(UnrealScriptID.PROPERTY_CONTENT_ASSISTANT_ENABLED.toString()) );
									contentAssistant.setAutoActivationDelay( UnrealScriptEditor.getActivatorPreferenceStore().getInt(UnrealScriptID.PROPERTY_CONTENT_ASSISTANT_DELAY.toString()) );
									
									UnrealScriptEditor.getActivatorPreferenceStore().addPropertyChangeListener
									(
										new IPropertyChangeListener()
										{
											@Override
											public void propertyChange( PropertyChangeEvent event )
											{
												if( contentAssistant != null )
												{
													String key = event.getProperty();
													if( key != null )
													{
														if( key.equals(UnrealScriptID.PROPERTY_CONTENT_ASSISTANT_ENABLED.toString()) || key.equals(UnrealScriptID.PROPERTY_CONTENT_ASSISTANT_DELAY.toString()) )
														{
															
															Display.getDefault().syncExec
															(
																new Runnable()
																{
																	@Override
																	public void run()
																	{
																		contentAssistant.enableAutoActivation( UnrealScriptEditor.getActivatorPreferenceStore().getBoolean(UnrealScriptID.PROPERTY_CONTENT_ASSISTANT_ENABLED.toString()) );
																		contentAssistant.setAutoActivationDelay( UnrealScriptEditor.getActivatorPreferenceStore().getInt(UnrealScriptID.PROPERTY_CONTENT_ASSISTANT_DELAY.toString()) );
																	}
																}
															);
															
														}
													}
												}
											}
										}
									);
								}
								
								contentAssistant.setProposalPopupOrientation( IContentAssistant.PROPOSAL_OVERLAY );
								contentAssistant.setContextInformationPopupOrientation( IContentAssistant.CONTEXT_INFO_ABOVE );
							// content assistant <<
						}
					}
				);
				
				
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
	
	
	public Object getAdapter( UnrealScriptEditor editor, Class<?> required )
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
		contentAssistant.setInformationControlCreator( getInformationControlCreator(sourceViewer) );
		return contentAssistant;
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
	
	@Override
	public IInformationControlCreator getInformationControlCreator( ISourceViewer sourceViewer )
	{
		return new IInformationControlCreator()
		{
			@Override
			public IInformationControl createInformationControl( Shell parent )
			{
				return new HoverInformationControl( this, parent, false, SWT.LEFT_TO_RIGHT, null );
			}
		};
	}
}