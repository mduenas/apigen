package com.markduenas.android.apigen;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View.OnLongClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class apigenactivity extends Activity implements View.OnClickListener, OnGestureListener {
	
	protected static final String TAG = "apigen";
	public static final float MAX_TEXT_SIZE = (float) 72.0;
	WebView wvPi;
	TextView tvPi;
	TextView tvScale;
	EditText textNumberofDigits;
	Button buttonGenerate;
	String stringPi = "3.";

	GestureDetector doubleTapListener;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        tvPi = (TextView)findViewById(R.id.tvPi);
        tvPi.setMovementMethod(new ScrollingMovementMethod());
        tvPi.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				 Intent i = new Intent(Intent.ACTION_SEND);
				 i.setType("message/rfc822");
				 //i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"recipient@example.com"});
				 i.putExtra(Intent.EXTRA_SUBJECT, "Digits of Pi");
				 i.putExtra(Intent.EXTRA_TEXT   , tvPi.getText());
				 try {
				     startActivity(Intent.createChooser(i, "Send mail..."));
				 } catch (android.content.ActivityNotFoundException ex) {
				     Toast.makeText(apigenactivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
				 }
				return false;
			}
		});
        tvScale = (TextView)findViewById(R.id.tvScale);
        tvScale.setVisibility(View.GONE);
        textNumberofDigits = (EditText)findViewById(R.id.tvNumDigits);
        buttonGenerate = (Button)findViewById(R.id.buttonGenerate);
        buttonGenerate.setOnClickListener(this);
        
        // restore state
        if(savedInstanceState != null) {
        	textNumberofDigits.setText(savedInstanceState.getString("numberOfDigits"));
        	tvPi.setText(savedInstanceState.getString("generatedPi"));
        } else {
        	stringPi = "3.";
        }

        doubleTapListener = new GestureDetector(this);
        doubleTapListener.setOnDoubleTapListener(new simpleDoubleTapListener());
    }
    
    @Override
	public boolean onTouchEvent(MotionEvent event) {
		
		//scaleGestureDetector.onTouchEvent(event);
    	boolean result = doubleTapListener.onTouchEvent(event);//return the double tap events
        return result;
		
	}
    
    public class simpleDoubleTapListener implements GestureDetector.OnDoubleTapListener
    {
    	private float textSize = (float) 24.0;
    	private int zoom = 0;
    	private boolean zoomIn = true;
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if(zoomIn)
			{
				zoom++;
				textSize += 1;
				if(zoom >= 5)
					zoomIn = false;
			}
			else
			{
				zoom--;
				textSize -= 1;
				if(zoom <= 0)
					zoomIn=true;
			}
			tvPi.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);			
			tvScale.setText(String.format("Textsize: %s", String.valueOf(tvPi.getTextSize())));
			Log.v("onDoubleTap", String.format("zoom: %s, zoomIn %s", zoom, String.valueOf(zoomIn)));
			return false;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}
    	
    }

	public class simpleOnScaleGestureListener extends SimpleOnScaleGestureListener {

		float scale = (float) 1.0;
		float scaleBegin = (float) 1.0;
		float scaleEnd = (float)1.0;
		
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			// TODO Auto-generated method stub
			//scaleGesture.setText(String.valueOf(detector.getScaleFactor()));
			//tvPi.setTextSize((float) (tvPi.getTextSize()*detector.getScaleFactor() * 0.5));
			scale += (float) detector.getScaleFactor();
			tvScale.setText(String.format("Scale %s: Text: %s", String.valueOf(scale), tvPi.getTextSize()));
			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			// TODO Auto-generated method stub
			// scaleGesture.setVisibility(View.VISIBLE);
			scale = tvPi.getTextSize();
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			// TODO Auto-generated method stub
			//scaleGesture.setVisibility(View.INVISIBLE);
			scaleEnd = detector.getScaleFactor();
			scale = scaleEnd - scaleBegin;
			//tvScale.setText(String.format("Scale %s: Text: %s", String.valueOf(tvPi.getTextSize()*scale), tvPi.getTextSize()));
			
		}
	}
    
    @Override 
    protected void onRestoreInstanceState(Bundle savedInstanceState) { 
      super.onRestoreInstanceState(savedInstanceState); 
      textNumberofDigits.setText(savedInstanceState.getCharSequence("numberOfDigits"));
      tvPi.setText(savedInstanceState.getString("generatedPi"));
  	}
    
    @Override 
    protected void onSaveInstanceState(Bundle outState) { 
      super.onSaveInstanceState(outState); 
      outState.putString("numberOfDigits", textNumberofDigits.getText().toString());
      outState.putCharSequence("generatedPi", tvPi.getText());
    }
    /**
	 * Handle the click for each button
	 * @param view
	 */
	public void onClick(View view) {
		try {
			String digits = textNumberofDigits.getText().toString();
			int d = Integer.parseInt(digits);
			if(d > 3500) {
				d = 3500;
				textNumberofDigits.setText("3500");
				toastMsg("Max number allowed is 3500.");
				return;
			}
			if(d <= 0) {
				d = 5;
				textNumberofDigits.setText("2");
				toastMsg("Number must be greater than 0.");
				return;
			}
			//String pi = bigpi.calcPi(d);
			FetchSecuredResourceTask f = new FetchSecuredResourceTask();
			f.execute(String.format("%d", d));
			
		} catch (ActivityNotFoundException e) {
			Log.e("onclick", e.getMessage());
		} catch (NumberFormatException e) {
			Log.e("onclick", e.getMessage());
		}
	}
	
	ProgressDialog pd;
	
	class FetchSecuredResourceTask extends AsyncTask<String, Void, String>
	{

		 @Override
		 protected void onPreExecute()
		 {
			 pd = ProgressDialog.show(apigenactivity.this, "Working...", "Generating Pi", true, false);
		 }

		@Override
		protected String doInBackground(String... urls)
		{
			try {
				return bigpi.calcPi(Integer.parseInt(urls[0]));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "Error generating Pi!";
		}

		@Override
		protected void onPostExecute(String result)
		{
			//stringPi = "<html><body><div style='width: 200px;word-wrap:break-word;'>";
			//stringPi += result;
			//stringPi += "</div></body></html>";
			//wvPi.loadData(stringPi, "text/html", "utf-8");
			tvPi.setText(result);
			if(pd != null)
				pd.dismiss();
		}
	}
	
	/**
	 * show a toast message to the user
	 * @param msg
	 * @param toastLength
	 */
	private void toastMsg(String msg) {
		Toast
		.makeText(this, msg, Toast.LENGTH_LONG)
		.show();
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.itemCopyToClipboard:
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(tvPi.getText());
			toastMsg("Pi copied to clipboard.");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
