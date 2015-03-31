package tests;

import android.content.Intent;
import android.graphics.Bitmap;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ActivityUnitTestCase;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.util.Log;
import android.widget.Button;

import com.example.dgif.Loaded3DObject;
import com.example.dgif.Preview3DObject;
import com.example.dgif.R;
import com.example.dgif.utils.RenderUtils;

/**
 * Created by Carmina on 2/26/2015.
 *
 * Tests activity life cycles of activities
 */
public class Preview3DObjectActivityTest extends ActivityInstrumentationTestCase2<Preview3DObject> {

    private static final int LAST_N_PICS = 2;
    private static final String LAST_N_PICS_TAG = "lastIndices";
    private static final String ORIENTATIONS_TAG = "orientations";


    Preview3DObjectActivityTest THIS = this;

    private Preview3DObject mActivity;
    private Button mGyroBtn;
    private Loaded3DObject mLoaded3DObject;

   public Preview3DObjectActivityTest() {
       super(Preview3DObject.class);
   }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);

    }


    public void testDeltaCalculation1() {
        setUpPreview(null);
        int n = 5;
        byte[] array = null;
        Bitmap a = mLoaded3DObject.getFrames()[0];
        Bitmap b = mLoaded3DObject.getFrames()[1];

        // Benchmark creation of intermediate image byte array with
        // a transformation matrix.
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            array = RenderUtils.getIntermediateImageByteArray(a, b, 0.5);
        }
        long end = System.currentTimeMillis();
        long delta = end - start;
        Log.d("BENCHMARK", "Bitmaps to Transformation Matrix Byte Array:    " + delta);
        assertEquals(true, true);
    }




    private void setUpPreview(float[] orientations) {
        Intent i = new Intent();
        i.putExtra(LAST_N_PICS_TAG, LAST_N_PICS);
      //  i.putExtra(ORIENTATIONS_TAG, orientations);
        setActivityIntent(i);
        mActivity = getActivity();
        mLoaded3DObject = mActivity.getLoaded3DObject();
        mGyroBtn = (Button) mActivity.findViewById(R.id.btn_start_gyro);
    }




}
