package tests;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ActivityUnitTestCase;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.util.Log;
import android.widget.Button;

import com.example.dgif.Loaded3DObject;
import com.example.dgif.Preview3DObject;
import com.example.dgif.R;

/**
 * Created by Carmina on 2/26/2015.
 *
 * Tests activity life cycles of activities
 */
public class Preview3DObjectActivityTest extends ActivityInstrumentationTestCase2<Preview3DObject> {

    private static final int LAST_N_PICS = 3;
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
        float[] orientations = {0f, 1f, 2f, 3f};
        setUpPreview(orientations);
        assertEquals(1f, mLoaded3DObject.getDelta(), 0.0f);
    }

    public void testDeltaCalculation2() {
        float[] orientations = {0f, 1f, 2f, 3f};
        setUpPreview(orientations);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoaded3DObject.changeBlendCount(1);
                assertEquals(0.5f, mLoaded3DObject.getDelta(), 0.0f);
            }
        });
    }


    private void setUpPreview(float[] orientations) {
        Intent i = new Intent();
        i.putExtra(LAST_N_PICS_TAG, LAST_N_PICS);
        i.putExtra(ORIENTATIONS_TAG, orientations);
        setActivityIntent(i);
        mActivity = getActivity();
        mLoaded3DObject = mActivity.getLoaded3DObject();
        mGyroBtn = (Button) mActivity.findViewById(R.id.btn_start_gyro);
    }




}
