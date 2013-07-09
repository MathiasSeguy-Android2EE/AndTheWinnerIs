package com.android2ee.project.contest.result;

import java.security.InvalidAlgorithmParameterException;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {
	/******************************************************************************************/
	/** Attributes **************************************************************************/
	/******************************************************************************************/

	/**
	 * La liste des participants au concours
	 */
	private final String[] participants = { "Vincent M.", "Franck J.", "Ludovic B.", "Joseph K.", "Elisabeth D.",
			"Eric T.", "Stephane Lo.", "Alain D.", "Mehdi C.", "Philippe L.", "Julien Q.", "Fabien F.", "Sahin E.",
			"Stephane La.", "Setan L.", "Bertrand L.J.", "Thierry D.", "Paul L.", "Marc D.", "JeanLouis D.",
			"Veronique C.", "Julien V.", "Denis B" };
	/**
	 * La liste des appareils participants au concours
	 */
	private final int[] devices = { R.drawable.nexus4, R.drawable.nexus7, R.drawable.nexus10 };
	private final int[] devicesFound = { R.drawable.nexus4_win, R.drawable.nexus7_win, R.drawable.nexus10_win };

	/**
	 * La clef pour le sharedPreference du gagnant
	 */
	private final String winnerKey = "AndTheWinnerIs";
	/**
	 * La clef pour le sharedPreference de l'appareil gagné
	 */
	private final String deviceKey = "AndTheDeviceIs";

	/**
	 * Le tag pour les logs
	 */
	private final String tag = "MainActivity";
	private Bitmap checkIcon;
	/******************************************************************************************/
	/** GUI Attributes **************************************************************************/
	/******************************************************************************************/

	private ImageView imvDevice;
	private ImageView imvParticipants;
	private Button btnLaunchDraw;
	private MyView myView;

	/******************************************************************************************/
	/** Managing LifeCycle **************************************************************************/
	/******************************************************************************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		try {
			testMathFormula();
		} catch (InvalidAlgorithmParameterException e) {
			finish();
		}
		checkIcon = BitmapFactory.decodeResource(getResources(), R.drawable.check);
		myView = (MyView) findViewById(R.id.myview);
		btnLaunchDraw = (Button) findViewById(R.id.btn_start_draw);
		btnLaunchDraw.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchDraw();
			}
		});
		imvParticipants = (ImageView) findViewById(R.id.imv_participants);
		imvDevice = (ImageView) findViewById(R.id.imv_device);
	}

	private void launchDraw() {
		deviceAnimDuration = 300;
		initializeNexusDrawable();
		initializeParticipantsDrawable();
		btnLaunchDraw.setVisibility(View.GONE);
		SlowingDownAsyncTask async = new SlowingDownAsyncTask();
		async.execute(8);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (myView != null) {
			myView.stopAnimation();
		}
	}

	/******************************************************************************************/
	/** Managing Animations **************************************************************************/
	/******************************************************************************************/

	/**
	 * The duration for the device animation
	 */
	int deviceAnimDuration = 100;
	/**
	 * The duration for the participants animation
	 */
	int participantsAnimDuration = 90;

	/**
	 * Set the animation on the imageView that displays the device
	 */
	private void initializeNexusDrawable() {
		final AnimationDrawable animationDrawable = new AnimationDrawable();
		Drawable nexus4 = getResources().getDrawable(devices[0]);
		Drawable nexus7 = getResources().getDrawable(devices[1]);
		Drawable nexus10 = getResources().getDrawable(devices[2]);
		animationDrawable.addFrame(nexus4, deviceAnimDuration);
		animationDrawable.addFrame(nexus7, deviceAnimDuration);
		animationDrawable.addFrame(nexus10, deviceAnimDuration);
		animationDrawable.setExitFadeDuration(500);
		// Run until we say stop
		animationDrawable.setOneShot(false);
		imvDevice.setImageDrawable(animationDrawable);

		animationDrawable.start();
	}

	/**
	 * Set the animation on the imageView that displays the device
	 */
	private void initializeParticipantsDrawable() {
		final AnimationDrawable animationDrawable = new AnimationDrawable();
		int l=participants.length;
		for (int i = 0; i < participants.length; i++) {
			animationDrawable.addFrame(createDrawableForParticipants((int)(Math.random()*l),false), participantsAnimDuration);
		}
		// Run until we say stop
		animationDrawable.setOneShot(false);
		imvParticipants.setImageDrawable(animationDrawable);
		animationDrawable.start();

	}

	Paint paint = null;

	/**
	 * @param participantNumber
	 * @return
	 */
	private BitmapDrawable createDrawableForParticipants(int participantNumber, boolean withCheck) {
		Bitmap bitmap = Bitmap.createBitmap(700, 100, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(getResources().getColor(R.color.translucent));
		if (paint == null) {
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setTextSize(60);
			paint.setColor(Color.BLACK);
			paint.setShadowLayer(2.5f, 2f, 2f, getResources().getColor(R.color.shape_stroke_color));
			paint.setTextAlign(Align.CENTER);
		}
		canvas.drawText(participants[participantNumber], 350, 80, paint);
		if(withCheck) {
			canvas.drawBitmap(checkIcon, 500, 0, paint);
		}
		return new BitmapDrawable(getResources(), bitmap);
	}

	public class SlowingDownAsyncTask extends AsyncTask<Integer, Integer, String> {
		public SlowingDownAsyncTask() {
			super();
		}

		// override of the method doInBackground (the one which is running in a separate thread)
		@Override
		protected String doInBackground(Integer... loopsNumber) {
			int count = 0;
			try {
				// Manage your thread's life cycle using the AtomicBooleans
				while (count < loopsNumber[0]) {
					// Make a pause or something like that
					Thread.sleep(2000);
					// increment the counter
					count++;
					publishProgress(count);
				}
			} catch (InterruptedException t) {
				// just end the background thread
				return ("The sleep operation failed");
			}
			return ("return object when task is finished");
		}

		// override of the onProgressUpdate method (runs in the GUI thread)
		@Override
		protected void onProgressUpdate(Integer... diff) {
			int count = diff[0];
			deviceAnimDuration = deviceAnimDuration + count * 50;
			participantsAnimDuration = participantsAnimDuration + count * 30;
			initializeNexusDrawable();
			initializeParticipantsDrawable();
		}

		// override of the onPostExecute method (runs in the GUI thread)
		@Override
		protected void onPostExecute(String message) {
			showFinalResult();

		}
	}

	/******************************************************************************************/
	/** Managing the draw (tirage au sort) **************************************************************************/
	/******************************************************************************************/

	/**
	 * 
	 */
	private void showFinalResult() {
//		int winnerIs = 5;
//		int deviceIs = 2;
		// find the result
		 int winnerIs=findTheWinner();
		 int deviceIs=findTheDevice();
		// display the device, display the participants
		imvDevice.setImageDrawable(getResources().getDrawable(devicesFound[deviceIs]));
		imvParticipants.setImageDrawable(createDrawableForParticipants(winnerIs,true));
		// displays the congrats
		findViewById(R.id.lilFelicitation).setVisibility(View.VISIBLE);
	}

	/**
	 * Find the winner is it hasn't been drawn yet or return the one drawn before
	 * 
	 * @return The winner
	 */
	private int findTheWinner() {
		// Retrieve If the winner has already been drawn
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int winner = prefs.getInt(winnerKey, -1);
		// The winner does not exist
		if (-1 == winner) {
			// 0<=Math.random()<1
			// So 0<=(Math.random)*participants.length<participants.length
			// SO if participants={"A","B"} then 0<=Math.rand*p.length<2
			// meaning math.rand*p.size=0,... or 1,...
			double rand = Math.random() * participants.length;
			// So we just need the integer part
			winner = formula(rand);
			// And storeIt
			Editor prefEdit = prefs.edit();
			prefEdit.putInt(winnerKey, winner);
			prefEdit.commit();
		}
		// Now the winner exists and set in stone
		return winner;
	}

	/**
	 * Find the winner is it hasn't been drawn yet or return the one drawn before
	 * 
	 * @return The winner
	 */
	private int findTheDevice() {
		// Retrieve If the winner has already been drawn
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int device = prefs.getInt(deviceKey, -1);
		// The winner does not exist
		if (-1 == device) {
			// 0<=Math.random()<1
			// So 0<=(Math.random)*devices.length<devices.length
			// SO if devices={"A","B"} then 0<=Math.rand*d.length<2
			// meaning math.rand*d.size=0,... or 1,...
			double rand = Math.random() * devices.length;
			Log.e(tag, "devices found : "+rand+" (devices.lenght = "+ devices.length);
			// So we just need the integer part
			device = formula(rand);
			// And storeIt
			Editor prefEdit = prefs.edit();
			prefEdit.putInt(deviceKey, device);
			prefEdit.commit();
		}
		// Now the device exists and set in stone
		return device;
	}

	/**
	 * @param value
	 *            the double from which we want to extract the integer part
	 * @return the integer part
	 */
	private int formula(double value) {
		return (int) value;
	}

	/**
	 * Ensure the formula is the good one
	 * 
	 * @throws InvalidAlgorithmParameterException
	 */
	private void testMathFormula() throws InvalidAlgorithmParameterException {
		double test0 = 0.5;
		double test00 = 0.25;
		double test000 = 0.75;
		double test0000 = 0;
		if (formula(test0) != 0) {
			throw new InvalidAlgorithmParameterException();
		}
		;
		if (formula(test00) != 0) {
			throw new InvalidAlgorithmParameterException();
		}
		;
		if (formula(test000) != 0) {
			throw new InvalidAlgorithmParameterException();
		}
		;
		if (formula(test0000) != 0) {
			throw new InvalidAlgorithmParameterException();
		}
		;
		Log.e(tag, "Test should return 0 " + formula(test0));
		Log.e(tag, "Test should return 0 " + formula(test00));
		Log.e(tag, "Test should return 0 " + formula(test000));
		Log.e(tag, "Test should return 0 " + formula(test0000));
		double test1 = 1.5;
		double test11 = 1.25;
		double test111 = 1.75;
		double test1111 = 1;
		if (formula(test1) != 1) {
			throw new InvalidAlgorithmParameterException();
		}
		;
		if (formula(test11) != 1) {
			throw new InvalidAlgorithmParameterException();
		}
		;
		if (formula(test111) != 1) {
			throw new InvalidAlgorithmParameterException();
		}
		;
		if (formula(test1111) != 1) {
			throw new InvalidAlgorithmParameterException();
		}
		;
		Log.e(tag, "Test should return 1 " + formula(test1));
		Log.e(tag, "Test should return 1 " + formula(test11));
		Log.e(tag, "Test should return 1 " + formula(test111));
		Log.e(tag, "Test should return 1 " + formula(test1111));
	}

	/******************************************************************************************/
	/** Managing the Menu **************************************************************************/
	/******************************************************************************************/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/******************************************************************************************/
	/** The Felicitation View that bounces **************************************************************************/
	/******************************************************************************************/

	/**
	 * @author CHET HAASE and Me (but me a few)
	 *         Thanks guy
	 */
	static class MyView extends View {

		Bitmap mBitmap;
		Paint paint = new Paint();
		int mShapeX, mShapeY;
		int mShapeW, mShapeH;
		int height, width;

		public MyView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			setupShape();
		}

		public MyView(Context context, AttributeSet attrs) {
			super(context, attrs);
			setupShape();
		}

		public MyView(Context context) {
			super(context);
			setupShape();
		}

		private void setupShape() {
			mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.feliciations);
			mShapeW = mBitmap.getWidth();
			mShapeH = mBitmap.getHeight();
			setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startAnimation();
				}
			});
		}

		public void stopAnimation() {
			anim.end();
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			height = h;
			mShapeX = (w - mBitmap.getWidth()) / 2;
			startAnimation();
		}

		ObjectAnimator anim;

		void startAnimation() {
			// This variation uses an ObjectAnimator. The functionality is exactly the same as
			// in Bouncer2, but this time the boilerplate code is greatly reduced because we
			// tell ObjectAnimator to automatically animate the target object for us, so we no
			// longer need to listen for frame updates and do that work ourselves.
			anim = getObjectAnimator();
			anim.setRepeatCount(16);
			anim.setRepeatMode(ValueAnimator.REVERSE);
			anim.setInterpolator(new AccelerateInterpolator());
			anim.start();
		}

		ObjectAnimator getObjectAnimator() {
			return ObjectAnimator.ofInt(this, "shapeY", 0, (height - mShapeH));
		}

		public void setShapeY(int shapeY) {
			int minY = mShapeY;
			int maxY = mShapeY + mShapeH;
			mShapeY = shapeY;
			minY = Math.min(mShapeY, minY);
			maxY = Math.max(mShapeY + mShapeH, maxY);
			// ask to redraw the rectangle area
			invalidate(mShapeX, minY, mShapeX + mShapeW, maxY);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawBitmap(mBitmap, mShapeX, mShapeY, paint);
		}
	}
}
