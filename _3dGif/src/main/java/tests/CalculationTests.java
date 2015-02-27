package tests;

import junit.framework.TestCase;

/**
 * Created by Carmina on 2/26/2015.
 */
public class CalculationTests extends TestCase {

    public void testCalculateDelta() {
        float[] orientations = {1f, 2f, 3f};
        int numBlends = 1;
        float[] expectedNewOrientations = {1f, 1.5f, 2f, 2.5f, 3f};
        float[] expectedDelta = {.5f, .5f, .5f, .5f};


    }

}
