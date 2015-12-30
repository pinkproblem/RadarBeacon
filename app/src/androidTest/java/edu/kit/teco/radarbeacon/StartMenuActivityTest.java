package edu.kit.teco.radarbeacon;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.CheckBox;

import com.robotium.solo.Solo;

import org.junit.Test;


/**
 * Created by Iris on 16.12.2015.
 */
public class StartMenuActivityTest extends ActivityInstrumentationTestCase2<StartMenuActivity> {

    private Solo solo;

    public StartMenuActivityTest() {
        super(StartMenuActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Test
    public void testSimple() throws Exception {
        solo.assertCurrentActivity("Main Menu", StartMenuActivity.class);
        StartMenuActivity activity = (StartMenuActivity) solo.getCurrentActivity();

        //check whether all ui components that should be visible are so
        assertTrue(solo.searchButton(solo.getString(R.string.start_scan)));
        assertTrue(solo.searchText(solo.getString(R.string.checkbox_save_energy)));

        //check whether all ui components that should be invisible are so
        assertFalse(solo.searchButton(solo.getString(R.string.connecting), true));
        View progressBar = solo.getView(R.id.connection_progress);
        assertTrue(progressBar.getVisibility() == View.INVISIBLE);
    }

    @Test
    public void testEnergyInfo() throws Exception {
        solo.assertCurrentActivity("Main Menu", StartMenuActivity.class);
        StartMenuActivity activity = (StartMenuActivity) solo.getCurrentActivity();

        solo.clickOnImage(0);
        solo.waitForDialogToOpen();

        solo.clickOnText(solo.getString(R.string.ok));
        solo.waitForDialogToClose();
    }

    @Test
    public void testEnergyOption() throws Exception {
        solo.assertCurrentActivity("Main Menu", StartMenuActivity.class);
        StartMenuActivity activity = (StartMenuActivity) solo.getCurrentActivity();
        CheckBox cb = (CheckBox) solo.getView(R.id.checkbox_save_energy);
        assertTrue(cb != null);

        //check correct switch on checkbox click
//        solo.clickOnView(cb);
        solo.clickOnCheckBox(0);
        assertTrue(solo.isCheckBoxChecked(0));
        solo.clickOnView(cb);
        assertFalse(solo.isCheckBoxChecked(0));
    }

    @Test
    public void testDeviceSelection() throws Exception {
        solo.assertCurrentActivity("Main Menu", StartMenuActivity.class);
        StartMenuActivity activity = (StartMenuActivity) solo.getCurrentActivity();

        //no devices found
        solo.clickOnText(solo.getString(R.string.button_start_connected));
        solo.waitForDialogToOpen();
        assertTrue(solo.searchText(solo.getString(R.string.device_dialog_empty)));
        solo.clickOnText(solo.getString(R.string.start_scan));
        solo.waitForText(solo.getString(R.string.toast_select_one));
        solo.clickOnText(solo.getString(R.string.cancel));
        solo.waitForDialogToClose();
    }

    @Test
    public void testDeviceSelection2() throws Exception {
        solo.assertCurrentActivity("Main Menu", StartMenuActivity.class);
        StartMenuActivity activity = (StartMenuActivity) solo.getCurrentActivity();

        //found one device
        BluetoothDevice device = ((BluetoothManager) activity.getSystemService(Context
                .BLUETOOTH_SERVICE)).getAdapter().getRemoteDevice("00:07:80:1B:5C:7C");
        activity.scanCallback.onLeScan(device, 0, null);

        solo.clickOnText(solo.getString(R.string.button_start_connected));
        solo.waitForDialogToOpen();
        assertTrue(solo.searchText(device.getName()));
        assertTrue(solo.searchText(device.getAddress()));
        solo.clickOnText(solo.getString(R.string.start_scan));
        solo.waitForText(solo.getString(R.string.toast_select_one));


        solo.clickOnText(solo.getString(R.string.cancel));
        solo.waitForDialogToClose();
    }

    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}