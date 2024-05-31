/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.sensor;

import java.lang.reflect.Method;
import java.util.List;
import javax.vecmath.Point3d;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.sensor.ComplexAttributeFieldManager.ComplexAttributeField;
import static org.hamcrest.CoreMatchers.instanceOf;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A jUnit test for the Tuple3dValue
 *
 * @author jleonard
 */
public class Tuple3dValueTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructor() {

        Tuple3dValue tuple = new Tuple3dValue(SensorAttributeNameEnum.ACCELERATION3D, new Point3d(1, 2, 3));

        List<ComplexAttributeField> attributeFields = ComplexAttributeFieldManager.getInstance().getFieldsForAttributeClass(Tuple3dValue.class);

        for (ComplexAttributeField field : attributeFields) {

            try {

                Method getterMethod = field.getMethod();
                Object returnVal = getterMethod.invoke(tuple);

                assertThat(returnVal, instanceOf(Number.class));

            } catch (Exception e) {

                System.err.println("Error with label of " + field.getLabel());
                e.printStackTrace();
            }
        }//end for
    }
}
