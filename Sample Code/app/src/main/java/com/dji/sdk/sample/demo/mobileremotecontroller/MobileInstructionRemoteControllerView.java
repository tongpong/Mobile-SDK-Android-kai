package com.dji.sdk.sample.demo.mobileremotecontroller;

import android.app.Service;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.OnScreenJoystickListener;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.DialogUtils;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.OnScreenJoystick;
import com.dji.sdk.sample.internal.utils.ToastUtils;
import com.dji.sdk.sample.internal.view.PresentableView;

import dji.common.error.DJIError;
import dji.common.flightcontroller.simulator.InitializationData;
import dji.common.flightcontroller.simulator.SimulatorState;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks.CompletionCallback;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.Simulator;
import dji.sdk.mobilerc.MobileRemoteController;
import dji.sdk.products.Aircraft;

/**
 * Class for mobile remote controller.
 */
public class MobileInstructionRemoteControllerView extends RelativeLayout
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, PresentableView {

    private ToggleButton btnSimulator;
    private Button btnTakeOff;
    private Button autoLand;
    private Button forceLand;

    private TextView textView;

    private EditText Bearing_val_text;
    private EditText FlightThrust_val_text;
    private EditText FlightUnit_val_text;
    private EditText YawThrust_val_text;
    private EditText YawUnit_val_text;
    private EditText ClimbThrust_val_text;
    private EditText ClimbUnit_val_text;

    private Button btn_start_Flight;
    private Button btn_start_Yaw;
    private Button btn_start_Climb;
    private  Button btn_stop_inst;

    //------------------------------Flight Permanent----------------------------------------------//
    private boolean Flight_starting;
    private double Bearing_val;
    private double FlightThrust_val;
    private double FlightUnit_val;
    private double Rightjoystick_X;
    private double Rightjoystick_Y;
    private long Flight_timestamp;
    //------------------------------Yaw Permanent----------------------------------------------//
    private boolean Yaw_starting;
    private double YawThrust_val;
    private double YawUnit_val;
    private double Leftjoystick_X;
    private long Yaw_timestamp;
    //------------------------------Climb Permanent----------------------------------------------//
    private boolean Climb_starting;
    private double ClimbThrust_val;
    private double ClimbUnit_val;
    private double Leftjoystick_Y;
    private long Climb_timestamp;




    //Spinner FlightUnit_spinner = (Spinner)findViewById(R.id.FlightUnit_spinner);
    //Spinner YawUnit_spinner = (Spinner)findViewById(R.id.YawUnit_spinner);
    // Spinner ClimbUnit_spinner = (Spinner)findViewById(R.id.ClimbUnit_spinner);
    //String[] UnitItems = new String[]{"Time (ms)", "distance (m)"};
    //private OnScreenJoystick screenJoystickRight;
    // private OnScreenJoystick screenJoystickLeft;
    private MobileRemoteController mobileRemoteController;
    private FlightControllerKey isSimulatorActived;

    public MobileInstructionRemoteControllerView(Context context) {
        super(context);
        init(context);
    }

    @NonNull
    @Override
    public String getHint() {
        return this.getClass().getSimpleName() + ".java";
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setUpListeners();
    }

    @Override
    protected void onDetachedFromWindow() {
        tearDownListeners();
        super.onDetachedFromWindow();
    }

    private void init(Context context) {
        setClickable(true);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_mobile_inst_rc, this, true);
        initAllKeys();
        initUI();
    }

    private void initAllKeys() {
        isSimulatorActived = FlightControllerKey.create(FlightControllerKey.IS_SIMULATOR_ACTIVE);
    }

    private void initUI() {
        btnTakeOff = (Button) findViewById(R.id.btn_take_off);
        autoLand = (Button) findViewById(R.id.btn_auto_land);
        autoLand.setOnClickListener(this);
        forceLand = (Button) findViewById(R.id.btn_force_land);
        forceLand.setOnClickListener(this);
        btnSimulator = (ToggleButton) findViewById(R.id.btn_start_simulator);

        textView = (TextView) findViewById(R.id.textview_simulator);
        btn_start_Flight = (Button) findViewById(R.id.btn_start_Flight);
        btn_start_Flight.setOnClickListener(this);
        btn_start_Yaw = (Button) findViewById(R.id.btn_start_Yaw);
        btn_start_Yaw.setOnClickListener(this);
        btn_start_Climb = (Button) findViewById(R.id.btn_start_Climb);
        btn_start_Climb.setOnClickListener(this);
        btn_stop_inst = (Button) findViewById(R.id.btn_stop_inst);
        btn_stop_inst.setOnClickListener(this);

        Bearing_val_text=(EditText)  findViewById(R.id.Bearing_val);
        FlightThrust_val_text=(EditText)  findViewById(R.id.FlightThrust_val);
        FlightUnit_val_text=(EditText)  findViewById(R.id.FlightUnit_val);

        YawThrust_val_text=(EditText)  findViewById(R.id.YawThrust_val);
        YawUnit_val_text=(EditText)  findViewById(R.id.YawUnit_val);

        ClimbThrust_val_text=(EditText)  findViewById(R.id.ClimbThrust_val);
        ClimbUnit_val_text=(EditText)  findViewById(R.id.ClimbUnit_val);

        Flight_starting=false;
        Yaw_starting=false;
        Climb_starting=false;
        //ArrayAdapter<String> UnitAdapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_dropdown_item,UnitItems);


        //FlightUnit_spinner.setAdapter(UnitAdapter);
        //YawUnit_spinner.setAdapter(UnitAdapter);
        //ClimbUnit_spinner.setAdapter(UnitAdapter);

        //screenJoystickRight = (OnScreenJoystick) findViewById(R.id.directionJoystickRight);
        //screenJoystickLeft = (OnScreenJoystick) findViewById(R.id.directionJoystickLeft);

        btnTakeOff.setOnClickListener(this);
        btnSimulator.setOnCheckedChangeListener(MobileInstructionRemoteControllerView.this);

        Boolean isSimulatorOn = (Boolean) KeyManager.getInstance().getValue(isSimulatorActived);
        if (isSimulatorOn != null && isSimulatorOn) {
            btnSimulator.setChecked(true);
            textView.setText("Simulator is On.");
        }
    }
    private void setLeftStick(float pX, float pY){
        if (Math.abs(pX) < 0.02) {
            pX = 0;
        }

        if (Math.abs(pY) < 0.02) {
            pY = 0;
        }
        if (mobileRemoteController != null) {
            mobileRemoteController.setLeftStickHorizontal(pX);
            mobileRemoteController.setLeftStickVertical(pY);
        }
    }
    private void setRightStick(float pX, float pY){
        if (Math.abs(pX) < 0.02) {
            pX = 0;
        }

        if (Math.abs(pY) < 0.02) {
            pY = 0;
        }
        if (mobileRemoteController != null) {
            mobileRemoteController.setRightStickHorizontal(pX);
            mobileRemoteController.setRightStickVertical(pY);
        }
    }
    private void Flight_instruction() {
        if(Flight_starting) {
            long timediff = System.currentTimeMillis() - Flight_timestamp;
            long time_remain=((long) FlightUnit_val - timediff);
            if (time_remain<=0.0f) {
                Rightjoystick_X = 0.0;
                Rightjoystick_Y = 0.0;
                Flight_starting = false;
                time_remain=0;
                FlightUnit_val_text.setFocusable(true);
            }

            FlightUnit_val=time_remain;
            FlightUnit_val_text.setText(Float.toString((float)time_remain));
        }
        setRightStick((float) Rightjoystick_X, (float) Rightjoystick_Y);
    }
    private void Yaw_instruction(){
        if(Yaw_starting){
            long timediff=System.currentTimeMillis()-Yaw_timestamp;
            long time_remain=((long)YawUnit_val-timediff);
            if(time_remain<=0.0f){
                time_remain=0;
                Leftjoystick_X=0.0;
                Yaw_starting=false;
                YawUnit_val_text.setFocusable(true);
            }
            YawUnit_val=time_remain;
            YawUnit_val_text.setText(Float.toString((float)time_remain));
        }
    }
    private void Climb_instruction(){
        if(Climb_starting) {
            long timediff = System.currentTimeMillis() - Climb_timestamp;
            long time_remain=((long) ClimbUnit_val - timediff);
            if ( time_remain<= 0.0f) {
                time_remain=0;
                Leftjoystick_Y = 0.0f;
                Climb_starting = false;
                ClimbUnit_val_text.setFocusable(true);
            }
            ClimbUnit_val=time_remain;
            ClimbUnit_val_text.setText(Float.toString((float)time_remain));
        }
    }
    private  void ClimbNYaw_insturction(){
        if(Yaw_starting||Climb_starting){
            Yaw_instruction();
            Climb_instruction();
        }
        setLeftStick((float)Leftjoystick_X,(float)Leftjoystick_Y);
    }
    private void setUpListeners() {
        Simulator simulator = ModuleVerificationUtil.getSimulator();
        if (simulator != null) {
            simulator.setStateCallback(new SimulatorState.Callback() {
                @Override
                public void onUpdate(final SimulatorState djiSimulatorStateData) {
                    ToastUtils.setResultToText(textView,
                            "Yaw : "
                                    + djiSimulatorStateData.getYaw()
                                    + ","
                                    + "X : "
                                    + djiSimulatorStateData.getPositionX()
                                    + "\n"
                                    + "Y : "
                                    + djiSimulatorStateData.getPositionY()
                                    + ","
                                    + "Z : "
                                    + djiSimulatorStateData.getPositionZ());
                }
            });
        } else {
            ToastUtils.setResultToToast("Disconnected!");
        }
        try {
            mobileRemoteController =
                    ((Aircraft) DJISampleApplication.getAircraftInstance()).getMobileRemoteController();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (mobileRemoteController != null) {
            textView.setText(textView.getText() + "\n" + "Mobile Connected");

        } else {
            textView.setText(textView.getText() + "\n" + "Mobile Disconnected");
        }
        ClimbNYaw_insturction();
        Flight_instruction();
        /*screenJoystickLeft.setJoystickListener(new OnScreenJoystickListener() {

            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                if (Math.abs(pX) < 0.02) {
                    pX = 0;
                }

                if (Math.abs(pY) < 0.02) {
                    pY = 0;
                }

                if (mobileRemoteController != null) {
                    mobileRemoteController.setLeftStickHorizontal(pX);
                    mobileRemoteController.setLeftStickVertical(pY);
                }
            }
        });

        screenJoystickRight.setJoystickListener(new OnScreenJoystickListener() {

            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                if (Math.abs(pX) < 0.02) {
                    pX = 0;
                }

                if (Math.abs(pY) < 0.02) {
                    pY = 0;
                }
                if (mobileRemoteController != null) {
                    mobileRemoteController.setRightStickHorizontal(pX);
                    mobileRemoteController.setRightStickVertical(pY);
                }
            }
        });*/
    }

    private void tearDownListeners() {
        Simulator simulator = ModuleVerificationUtil.getSimulator();
        if (simulator != null) {
            simulator.setStateCallback(null);
        }
        //screenJoystickLeft.setJoystickListener(null);
        //screenJoystickRight.setJoystickListener(null);
    }
    private void set_Flight_perm(){

        textView.setText("start2");
        Bearing_val = Float.parseFloat(Bearing_val_text.getText().toString());
        textView.setText("start3");
        FlightThrust_val = Float.parseFloat(FlightThrust_val_text.getText().toString())/100.0;
        textView.setText(textView.getText()+"\n"+Float.toString((float) FlightThrust_val));
        Rightjoystick_X= FlightThrust_val*Math.cos(Math.toRadians(Bearing_val));
        textView.setText(textView.getText()+"\n"+Float.toString((float)Rightjoystick_X));
        Rightjoystick_Y = FlightThrust_val*Math.sin(Math.toRadians(Bearing_val));
        textView.setText(textView.getText()+"\n"+Float.toString((float)Rightjoystick_Y));
        FlightUnit_val = Float.parseFloat(FlightUnit_val_text.getText().toString());
        textView.setText(textView.getText()+"\n"+Float.toString((float)FlightUnit_val));
        Flight_timestamp=System.currentTimeMillis();
        FlightUnit_val_text.setFocusable(false);
        Flight_starting=true;
    }
    private void set_Yaw_perm(){
        YawThrust_val =  Float.parseFloat(YawThrust_val_text.getText().toString())/100.0;
        YawUnit_val =  Float.parseFloat(YawUnit_val_text.getText().toString());
        Leftjoystick_X = YawThrust_val;

        Yaw_timestamp=System.currentTimeMillis();
        YawUnit_val_text.setFocusable(false);
        Yaw_starting=true;
    }
    private void set_Climb_perm(){
        ClimbThrust_val =  Float.parseFloat(ClimbThrust_val_text.getText().toString())/100.0;
        ClimbUnit_val =  Float.parseFloat(ClimbUnit_val_text.getText().toString());
        Leftjoystick_Y = ClimbThrust_val;

        Climb_timestamp=System.currentTimeMillis();
        ClimbUnit_val_text.setFocusable(false);
        Climb_starting=true;
    }
    private void stop_inst(){
        ClimbUnit_val=0;
        YawUnit_val=0;
        FlightUnit_val=0;
        Leftjoystick_X=0;
        Leftjoystick_Y=0;
        Rightjoystick_X=0;
        Rightjoystick_Y=0;
    }
    @Override
    public void onClick(View v) {
        FlightController flightController = ModuleVerificationUtil.getFlightController();
        if (flightController == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.btn_take_off:

                flightController.startTakeoff(new CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        DialogUtils.showDialogBasedOnError(getContext(), djiError);
                    }
                });
                break;
            case R.id.btn_force_land:
                flightController.confirmLanding(new CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        DialogUtils.showDialogBasedOnError(getContext(), djiError);
                    }
                });
                break;
            case R.id.btn_auto_land:
                flightController.startLanding(new CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        DialogUtils.showDialogBasedOnError(getContext(), djiError);
                    }
                });
                break;
            case R.id.btn_start_Flight:
                textView.setText("start");
                set_Flight_perm();
                break;
            case R.id.btn_start_Yaw:
                set_Yaw_perm();
                break;
            case R.id.btn_start_Climb:
                set_Climb_perm();
                break;
            case R.id.btn_stop_inst:
                stop_inst();
                break;
            default:
                break;
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton == btnSimulator) {
            onClickSimulator(b);
        }
    }

    private void onClickSimulator(boolean isChecked) {
        Simulator simulator = ModuleVerificationUtil.getSimulator();
        if (simulator == null) {
            return;
        }
        if (isChecked) {

            textView.setVisibility(VISIBLE);
            simulator.start(InitializationData.createInstance(new LocationCoordinate2D(23, 113), 10, 10),
                    new CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {

                        }
                    });
        } else {

            textView.setVisibility(INVISIBLE);
            simulator.stop(new CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
        }
    }

    @Override
    public int getDescription() {
        return R.string.component_listview_mobile_insturction_remote_controller;
    }
}

