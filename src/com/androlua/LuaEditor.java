package com.androlua;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.RadioGroup.*;
import android.widget.TextView.*;
import com.myopicmobile.textwarrior.android.*;
import com.myopicmobile.textwarrior.common.*;
import java.io.*;

public class LuaEditor extends FreeScrollingTextField
{

	private Document _inputingDoc;

	private boolean _isWordWrap;

	private Context mContext;

	private String _lastSelectedFile;

	private String fontDir="/sdcard/androlua/fonts/";

	public LuaEditor(Context context)
	{
		super(context);
		mContext = context;
		setTypeface(Typeface.MONOSPACE);
		File df=new File(fontDir + "default.ttf");
		if (df.exists())
			setTypeface(Typeface.createFromFile(df));
		File bf=new File(fontDir + "bold.ttf");
		if (bf.exists())
			setBoldTypeface(Typeface.createFromFile(bf));
		File tf=new File(fontDir + "italic.ttf");
		if (tf.exists())
			setItalicTypeface(Typeface.createFromFile(tf));
		DisplayMetrics dm=context.getResources().getDisplayMetrics();
		float size=TypedValue.applyDimension(2, BASE_TEXT_SIZE_PIXELS, dm);
		setTextSize((int)size);
		setShowLineNumbers(true);
		setHighlightCurrentRow(true);
		setWordWrap(false);
		setAutoIndentWidth(2);
		Lexer.setLanguage(LanguageLua.getInstance());
		setNavigationMethod(new YoyoNavigationMethod(this));
		TypedArray array = mContext.getTheme().obtainStyledAttributes(new int[] {  
																 android.R.attr.colorBackground, 
																 android.R.attr.textColorPrimary, 
																 android.R.attr.textColorHighlightInverse,
															 }); 
		int backgroundColor = array.getColor(0, 0xFF00FF); 
		int textColor = array.getColor(1, 0xFF00FF); 
		int textColorHighlight = array.getColor(1, 0xFF00FF); 
		array.recycle();
		setTextColor(textColor);
		//setTextHighligtColor(textColorHighlight);
	}

	public void setDark(boolean isDark)
	{
		if(isDark)
			setColorScheme(new ColorSchemeDark());
		else
			setColorScheme(new ColorSchemeLight());
	}
	
	
	
	public void setKeywordColor(int color)
	{
		getColorScheme().setColor(ColorScheme.Colorable.KEYWORD,color);
	}
	
	public void setUserwordColor(int color)
	{
		getColorScheme().setColor(ColorScheme.Colorable.LITERAL,color);
	}
	
	public void setBasewordColor(int color)
	{
		getColorScheme().setColor(ColorScheme.Colorable.NAME,color);
	}
	
	public void setStringColor(int color)
	{
		getColorScheme().setColor(ColorScheme.Colorable.STRING,color);
	}
	
	public void setCommentColor(int color)
	{
		getColorScheme().setColor(ColorScheme.Colorable.COMMENT,color);
	}
	
	public void setBackgoudColor(int color)
	{
		getColorScheme().setColor(ColorScheme.Colorable.BACKGROUND,color);
	}
	
	public void setTextColor(int color)
	{
		getColorScheme().setColor(ColorScheme.Colorable.FOREGROUND,color);
	}
	
	public void setTextHighligtColor(int color)
	{
		getColorScheme().setColor(ColorScheme.Colorable.SELECTION_BACKGROUND,color);
	}

	
	
		
	public void gotoLine()
	{
		// TODO: Implement this method
		startGotoMode();
	}

	public void search()
	{
		// TODO: Implement this method
		startFindMode();
	}

	public void startGotoMode()
	{
		// TODO: Implement this method
		startActionMode(new ActionMode.Callback(){

				private int idx;

				private EditText edit;

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu)
				{
					// TODO: Implement this method
					mode.setTitle("转到");
					mode.setSubtitle(null);

					edit = new EditText(mContext){
						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count)
						{
							if (s.length() > 0)
							{
								idx = 0;
								_gotoLine();
							}
						}

						};
					
					edit.setSingleLine(true);
					edit.setInputType(2);
					edit.setImeOptions(2);
					edit.setOnEditorActionListener(new OnEditorActionListener(){

							@Override
							public boolean onEditorAction(TextView p1, int p2, KeyEvent p3)
							{
								// TODO: Implement this method
								_gotoLine();
								return true;
							}
						});
					edit.setLayoutParams(new LayoutParams(getWidth() / 3, -1));
					menu.add(0, 1, 0, "").setActionView(edit);
					menu.add(0, 2, 0, mContext.getString(android.R.string.ok));
					edit.requestFocus();
					return true;
				}
				
				private void _gotoLine()
				{
					String s=edit.getText().toString();
					if(s.isEmpty())
						return;
						
					int l=Integer.valueOf(s).intValue();
					if(l>_hDoc.getRowCount()){
						l=_hDoc.getRowCount();
					}
					gotoLine(l);
					// TODO: Implement this method
				}
				
				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu)
				{
					// TODO: Implement this method
					return false;
				}

				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item)
				{
					// TODO: Implement this method
					switch (item.getItemId())
					{
						case 1:
							break;
						case 2:
							_gotoLine();
							break;

					}
					return false;
				}

				@Override
				public void onDestroyActionMode(ActionMode p1)
				{
					// TODO: Implement this method
				}
			});

	}

	public void startFindMode()
	{
		// TODO: Implement this method
		startActionMode(new ActionMode.Callback(){

				private LinearSearchStrategy finder;

				private int idx;

				private EditText edit;

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu)
				{
					// TODO: Implement this method
					mode.setTitle("搜索");
					mode.setSubtitle(null);

					edit = new EditText(mContext){
						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count)
						{
							if (s.length() > 0)
							{
								idx = 0;
								findNext();
							}
						}
					};
					edit.setSingleLine(true);
					edit.setImeOptions(3);
					edit.setOnEditorActionListener(new OnEditorActionListener(){

							@Override
							public boolean onEditorAction(TextView p1, int p2, KeyEvent p3)
							{
								// TODO: Implement this method
								findNext();
								return true;
							}
						});
					edit.setLayoutParams(new LayoutParams(getWidth() / 3, -1));
					menu.add(0, 1, 0, "").setActionView(edit);
					menu.add(0, 2, 0, mContext.getString(android.R.string.search_go));
					edit.requestFocus();
					return true;
				}

				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu)
				{
					// TODO: Implement this method
					return false;
				}

				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item)
				{
					// TODO: Implement this method
					switch (item.getItemId())
					{
						case 1:
							break;
						case 2:
							findNext();
							break;

					}
					return false;
				}

				private void findNext()
				{
					// TODO: Implement this method
					finder = new LinearSearchStrategy();
					String kw=edit.getText().toString();
					if (kw.isEmpty())
						return;
					idx = finder.find(getText(), kw, idx, getText().length(), false, false);
					if (idx == -1)
					{
						Toast.makeText(mContext, "未找到", 500).show();
						idx = 0;
						return;
					}
					setSelection(idx, edit.getText().length());
					idx += edit.getText().length();
					moveCaret(idx);
				}

				@Override
				public void onDestroyActionMode(ActionMode p1)
				{
					// TODO: Implement this method
				}
			});

	}



	@Override
	public void setWordWrap(boolean enable)
	{
		// TODO: Implement this method
		_isWordWrap = enable;
		super.setWordWrap(enable);
	}

	public DocumentProvider getText()
	{
		return createDocumentProvider();
	}

	
	public void setText(CharSequence c,boolean isRep)
	{
		replaceText(0,getLength()-1,c.toString());
	}
	
	public void setText(CharSequence c)
	{
		//TextBuffer text=new TextBuffer();
		Document doc=new Document(this);
		doc.setWordWrap(_isWordWrap);
		doc.setText(c);
		setDocumentProvider(new DocumentProvider(doc));
		doc.analyzeWordWrap();
	}

	public void setSelection(int indeex)
	{
		selectText(false);
		moveCaret(indeex);
	}

	public void gotoLine(int line)
	{
		int i=getText().getLineOffset(line - 1);
		setSelection(i);
	}

	

	public void undo()
	{
		DocumentProvider doc = createDocumentProvider();
		int newPosition = doc.undo();

		if (newPosition >= 0)
		{
			//TODO editor.setEdited(false); if reached original condition of file
			setEdited(true);

			respan();
			selectText(false);
			moveCaret(newPosition);
			invalidate();
		}

	}

	public void redo()
	{
		DocumentProvider doc = createDocumentProvider();
		int newPosition = doc.redo();

		if (newPosition >= 0)
		{
			setEdited(true);

			respan();
			selectText(false);
			moveCaret(newPosition);
			invalidate();
		}

	}

	public void open(String filename)
	{
		_lastSelectedFile = filename;

		File inputFile = new File(filename);
		_inputingDoc = new Document(this);
		_inputingDoc.setWordWrap(this.isWordWrap());
		ReadTask _taskRead = new ReadTask(this, inputFile);
		_taskRead.start();
	}

	public void save(String filename, boolean overwrite)
	{
		File outputFile = new File(filename);

		if (outputFile.exists())
		{
			if (!outputFile.canWrite())
			{
				return;
			}
		}

	}
}
