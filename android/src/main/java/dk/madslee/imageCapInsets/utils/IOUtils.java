package dk.madslee.imageCapInsets.utils;

import android.util.Log;
import java.io.Closeable;

/**
 * Utils for I/O operations.
 */
public class IOUtils {
	/**
	 * Closes Closeable without raising exception.
	 *
	 * @param closeable to close.
	 */
	public static void CloseQuietly(Closeable closeable) {
		if (closeable == null)
			return;
		try {
			closeable.close();
		}
		catch (Exception ex) {
			Log.e("IOUtils", "Cannot close stream. Doing nothing.");
		}
	}
}