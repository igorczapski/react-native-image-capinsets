package dk.madslee.imageCapInsets.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

/**
 * AsyncTask for generating 9patches from images.
 */
public class RCTImageLoaderTask extends AsyncTask<String, Void, Bitmap> {
	private static final String TAG = "RCTImageLoaderTask";

	/**
	 * Image uri. Can be resource name (i.e. "bubble" for "bubble.9.png" in res/drawable folder)
	 * Can be url to image on remote server (when starts with "http")
	 * Can be path to local file. Cannot start with "content://" schema or "file://", needs to be resolved as absolute path to
	 * file.
	 */
	private final String mUri;

	/**
	 * Weak reference to the Android context.
	 */
	private final WeakReference<Context> mContext;

	/**
	 * Weak reference to the listener (in case of listener being a context).
	 */
	private final WeakReference<RCTImageLoaderListener> mListener;

	/**
	 * Resource drawable helper.
	 */
	private final RCTResourceDrawableIdHelper mResourceDrawableIdHelper;

	public RCTImageLoaderTask(String uri, Context context, RCTImageLoaderListener listener) {
		mUri = uri;
		mContext = new WeakReference<>(context);
		mListener = new WeakReference<>(listener);
		mResourceDrawableIdHelper = new RCTResourceDrawableIdHelper();
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		if (mUri.startsWith("http")) {
			return fromNetworkFile(mUri);
		}
		Context ctx = mContext.get();
		if (ctx == null) {
			return null;
		}
		int resDrawableId = mResourceDrawableIdHelper.getResourceDrawableId(ctx, mUri);
		if (resDrawableId != 0) {
			return fromResourceFile(ctx, resDrawableId);
		}
		if (new File(mUri).exists()) {
			return fromLocalFile(mUri);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (!isCancelled() && mContext.get() != null && mListener.get() != null) {
			mListener.onImageLoaded(bitmap);
		}
	}

	/**
	 * Loads bitmap from resource.
	 *
	 * @param ctx Context.
	 * @param resDrawableId Drawable resource id.
	 *
	 * @return Loaded bitmap.
	 */
	private Bitmap fromResourceFile(Context ctx, int resDrawableId) {
		return BitmapFactory.decodeResource(ctx.getResources(), resDrawableId);
	}

	/**
	 * Loads bitmap from local file.
	 *
	 * @param uri Image path.
	 *
	 * @return Loaded bitmap or null if failed.
	 */
	private Bitmap fromLocalFile(String uri) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = false;
		opts.inSampleSize = calculateInSampleSize(uri, 200); //Arbitrary value, adapt it to your needs
		Bitmap bitmap = null;

		try {
			bitmap = BitmapFactory.decodeFile(uri, opts);
		}
		catch (Exception ex) {
			Log.e(TAG, "Cannot decode bitmap from file.");
			ex.printStackTrace();
		}
		return bitmap;
	}

	/**
	 * Loads bitmap from network.
	 *
	 * @param uri Uri to image.
	 *
	 * @return Loaded bitmap or null.
	 */
	private Bitmap fromNetworkFile(String uri) {
		Bitmap bitmap = null;
		InputStream in = null;
		try {
			in = new URL(uri).openStream();
			if (isCancelled()) {
				IOUtils.CloseQuietly(in);
				return null;
			}
			bitmap = BitmapFactory.decodeStream(in);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			IOUtils.CloseQuietly(in);
		}
		if (isCancelled() && bitmap != null) {
			bitmap.recycle();
			return null;
		}
		return bitmap;
	}

	/**
	 * Calculates inSampleSize - loads scaled down version of the image into memory. This method needs only one dimension as
	 * it will maintain aspect ratio.
	 *
	 * @param pathToLargeFile Path to image.
	 * @param requiredBiggerDimension Maximum length of the bigger dimension, we don't need any bigger.
	 *
	 * @return inSampleSize optimal parameter.
	 *
	 * @see <a href>https://developer.android.com/topic/performance/graphics/load-bitmap.html</a>
	 */
	private static int calculateInSampleSize(String pathToLargeFile, int requiredBiggerDimension) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathToLargeFile, opts);
		int imgHeight = opts.outHeight;
		int imgWidth = opts.outWidth;

		double ratio = 0;
		int requiredSmallerDimension = 1;
		int inSampleSize = 1;
		if (imgHeight >= imgWidth) {
			ratio = calculateAspectRatio(imgHeight, imgWidth);
			requiredSmallerDimension = (int) Math.floor((double) requiredBiggerDimension / ratio);
			if (imgHeight > requiredBiggerDimension) {
				int halfHeight = imgHeight / 2;
				int halfWidth = imgWidth / 2;
				while ((halfHeight / inSampleSize) >= requiredBiggerDimension
						&& (halfWidth / inSampleSize) >= requiredSmallerDimension) {
					inSampleSize *= 2;
				}
			}
		}
		else {
			ratio = calculateAspectRatio(imgWidth, imgHeight);
			requiredSmallerDimension = (int) Math.floor((double) requiredBiggerDimension / ratio);
			if (imgWidth > requiredBiggerDimension) {
				int halfHeight = imgHeight / 2;
				int halfWidth = imgWidth / 2;
				while ((halfHeight / inSampleSize) >= requiredSmallerDimension
						&& (halfWidth / inSampleSize) >= requiredBiggerDimension) {
					inSampleSize *= 2;
				}
			}
		}
		return inSampleSize;
	}

	/**
	 * Calculates aspect ratio.
	 *
	 * @param bigger bigger dimension.
	 * @param smaller smaller dimension.
	 *
	 * @return aspect ratio.
	 */
	private static double calculateAspectRatio(int bigger, int smaller) {
		return (double) bigger / (double) smaller;
	}
}
