package au.com.sharonblain.longhairhow2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
 
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class HelperUtils {
	private Context _context;
	 
    // constructor
    public HelperUtils(Context context) {
        this._context = context;
    }
 
    // Reading file paths from SDCard
    public ArrayList<String> getFilePaths() {
        ArrayList<String> filePaths = new ArrayList<String>();
 
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 1; i < 32; i++) {
            list.add(i);
        }

        // Shuffle it
        Collections.shuffle(list);

        for (int i = 0; i < list.size(); i++) {
            filePaths.add("@drawable/sharonlogin" + String.valueOf(list.get(i))) ;
        }
 
        return filePaths;
    }
    /*
     * getting screen width
     */
    @SuppressWarnings("deprecation")
	@SuppressLint("NewApi") public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) _context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
 
        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) { // Older device
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        columnWidth = point.x;
        return columnWidth;
    }
}
