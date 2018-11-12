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
import dji.common.flightcontroller.FlightControllerState;
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

    private Button status_solid ;
    private ToggleButton btnSimulator;
    private Button btnTakeOff;
    private Button autoLand;
    private Button forceLand;

    private TextView textView;
    private TextView flight_data;

    private EditText Bearing_val_text;
    private EditText FlightThrust_val_text;
    private EditText FlightUnit_val_text;
    private EditText YawThrust_val_text;
    private EditText YawUnit_val_text;
    private EditText ClimbThrust_val_text;
    private EditText ClimbUnit_val_text;
    private EditText distance_offset_text;

    private Button btn_start_Flight;
    private Button btn_start_Yaw;
    private Button btn_start_Climb;
    private Button btn_stop_inst;
    private ToggleButton btn_man_flight;
    //------------------------------Flight Permanent----------------------------------------------//
    private boolean Flight_starting;
    private ToggleButton FlightUnit_select;
    private double Bearing_val;
    private double FlightThrust_val;
    private double FlightUnit_val;
    private double Rightjoystick_X;
    private double Rightjoystick_Y;
    private long Flight_timestamp;
    private boolean FlightTime_sel;
    //------------------------------Yaw Permanent----------------------------------------------//
    private ToggleButton YawUnit_select;
    private boolean Yaw_starting;
    private double YawThrust_val;
    private double YawUnit_val;
    private double Leftjoystick_X;
    private long Yaw_timestamp;
    private boolean YawTime_sel;
    private float headingTmp;
    //------------------------------Climb Permanent----------------------------------------------//
    private ToggleButton ClimbUnit_select;
    private boolean Climb_starting;
    private double ClimbThrust_val;
    private double ClimbUnit_val;
    private double Leftjoystick_Y;
    private long Climb_timestamp;
    private boolean ClimbTime_sel;
    //------------------------------flight data--------------------------------------------------
    private float VelocityX;
    private float VelocityY;
    private float VelocityZ;
    private float CompassHeading;
    private float distance_offset;
    //-----------------------------Thread------------------------------------------------------..
    private  Thread FlightProcessThread;
    private boolean AutoFlight;
    private boolean ManFlight ;
    //private static double thrust_limit=0.5;

    //Spinner FlightUnit_spinner = (Spinner)findViewById(R.id.FlightUnit_spinner);
    //Spinner YawUnit_spinner = (Spinner)findViewById(R.id.YawUnit_spinner);
    // Spinner ClimbUnit_spinner = (Spinner)findViewById(R.id.ClimbUnit_spinner);
    //String[] UnitItems = new String[]{"Time (ms)", "distance (m)"};
    private OnScreenJoystick screenJoystickRight;
    private OnScreenJoystick screenJoystickLeft;
    private MobileRemoteController mobileRemoteController;
    private FlightControllerKey isSimulatorActived;

    private  FlightController flightController;
    private FlightControllerState mFlightControllerState;
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
        status_solid =(Button) findViewById(R.id.status_solid);
        status_solid.setBackgroundColor(0xB2FF0000);

        textView = (TextView) findViewById(R.id.textview_simulator);
        flight_data = (TextView) findViewById(R.id.flight_data);

        btnTakeOff = (Button) findViewById(R.id.btn_take_off);
        btnTakeOff.setOnClickListener(this);

        autoLand = (Button) findViewById(R.id.btn_auto_land);
        autoLand.setOnClickListener(this);
        forceLand = (Button) findViewById(R.id.btn_force_land);
        forceLand.setOnClickListener(this);

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

        distance_offset_text=(EditText)  findViewById(R.id.distance_offset);
        Flight_starting=false;
        Yaw_starting=false;
        Climb_starting=false;

        FlightTime_sel=false;
        YawTime_sel=false;
        ClimbTime_sel=false;

        screenJoystickRight = (OnScreenJoystick) findViewById(R.id.directionJoystickRight);
        screenJoystickLeft = (OnScreenJoystick) findViewById(R.id.directionJoystickLeft);


        btnSimulator = (ToggleButton) findViewById(R.id.btn_start_simulator);
        btnSimulator.setOnCheckedChangeListener(MobileInstructionRemoteControllerView.this);
        btn_man_flight = (ToggleButton) findViewById(R.id.btn_man_flight);
        btn_man_flight.setOnCheckedChangeListener(MobileInstructionRemoteControllerView.this);
        FlightUnit_select=(ToggleButton) findViewById(R.id.FlightUnit_select);
        FlightUnit_select.setOnCheckedChangeListener(MobileInstructionRemoteControllerView.this);
        YawUnit_select=(ToggleButton) findViewById(R.id.YawUnit_select);
        YawUnit_select.setOnCheckedChangeListener(MobileInstructionRemoteControllerView.this);
        ClimbUnit_select=(ToggleButton) findViewById(R.id.ClimbUnit_select);
        ClimbUnit_select.setOnCheckedChangeListener(MobileInstructionRemoteControllerView.this);

        ManFlight =false;
        AutoFlight=false;
        Boolean isSimulatorOn = (Boolean) KeyManager.getInstance().getValue(isSimulatorActived);
        if (isSimulatorOn != null && isSimulatorOn) {
            btnSimulator.setChecked(true);
            textView.setText("Simulator is On.");
        }
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
        screenJoystickLeft.setJoystickListener(new OnScreenJoystickListener() {

            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                if(ManFlight) {
                    setLeftStick(pX,pY);
                }
            }
        });

        screenJoystickRight.setJoystickListener(new OnScreenJoystickListener() {

            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                if(ManFlight) {
                    setRightStick(pX,pY);
                }
            }
        });
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
        Bearing_val = -(Float.parseFloat(Bearing_val_text.getText().toString()))+90.0;
        FlightThrust_val = Float.parseFloat(FlightThrust_val_text.getText().toString())/100.0;
        if(FlightThrust_val>1.0) FlightThrust_val=1.0;
        if(FlightThrust_val<-1.0) FlightThrust_val=-1.0;
        Rightjoystick_X= FlightThrust_val*Math.cos(Math.toRadians(Bearing_val));
        Rightjoystick_Y = FlightThrust_val*Math.sin(Math.toRadians(Bearing_val));
        FlightUnit_val = Float.parseFloat(FlightUnit_val_text.getText().toString())-distance_offset;
        Flight_timestamp=System.currentTimeMillis();
        //FlightUnit_val_text.setFocusable(false);
        Flight_starting=true;

    }
    private void set_Yaw_perm(){
        YawThrust_val =  Float.parseFloat(YawThrust_val_text.getText().toString())/100.0;
        if(YawThrust_val>1.0) YawThrust_val=1.0;
        if(YawThrust_val<-1.0) YawThrust_val=-1.0;
        YawUnit_val =  Float.parseFloat(YawUnit_val_text.getText().toString());
        Leftjoystick_X = YawThrust_val;
        Yaw_timestamp=System.currentTimeMillis();
        headingTmp=CompassHeading;
        //YawUnit_val_text.setFocusable(false);
        Yaw_starting=true;
    }
    private void set_Climb_perm(){

        ClimbThrust_val =  Float.parseFloat(ClimbThrust_val_text.getText().toString())/100.0;
        if(ClimbThrust_val>1.0) ClimbThrust_val=1.0;
        if(ClimbThrust_val<-1.0) ClimbThrust_val=-1.0;
        ClimbUnit_val =  Float.parseFloat(ClimbUnit_val_text.getText().toString());
        Leftjoystick_Y = ClimbThrust_val;

        Climb_timestamp=System.currentTimeMillis();
        //ClimbUnit_val_text.setFocusable(false);
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
    private double bearing_process(double x, double y){
        double angle=Math.toDegrees(Math.atan(Math.abs(y/x)));
        if(x<0&&y>0){
            angle=180-angle;
        }
        if(x<0&&y<0){
            angle+=180;
        }
        if(x>0&&y>0){
            angle=360-angle;
        }
        return angle;
    }
    private void Flight_instruction() {
        if(Flight_starting) {
            if(FlightTime_sel) {
                FlightUnit_val -= (System.currentTimeMillis() - Flight_timestamp);
            }
            else {
                double timediff=(double) (System.currentTimeMillis() - Flight_timestamp)/1000.0;
                double VFlight= Math.sqrt(VelocityX*VelocityX)+(VelocityY*VelocityY);
                FlightUnit_val-=(timediff*VFlight);


            }

            Flight_timestamp = System.currentTimeMillis();
            if (FlightUnit_val<=0.0f) {
                Rightjoystick_X = 0.0;
                Rightjoystick_Y = 0.0;
                Flight_starting = false;
                FlightUnit_val=0;
                FlightUnit_val_text.setFocusable(true);
            }
            else {
                if (VelocityY != 0) {
                    double angleNow = -bearing_process(VelocityY, VelocityX)+90;
                  //  double angleDiff = Bearing_val - angleNow;
                    //Rightjoystick_X = FlightThrust_val * Math.cos(Math.toRadians(Bearing_val + angleDiff));
                    //Rightjoystick_Y = FlightThrust_val * Math.sin(Math.toRadians(Bearing_val + angleDiff));

                }
            }
        }
        setRightStick((float) Rightjoystick_X, (float) Rightjoystick_Y);
    }
    private void Yaw_instruction(){
        if(Yaw_starting){
            if(YawTime_sel) {
                YawUnit_val -= (System.currentTimeMillis() - Yaw_timestamp);
                Yaw_timestamp = System.currentTimeMillis();
            }
            else {
                float diff=Math.abs(CompassHeading-headingTmp);
                if(diff>180.0f)
                    diff-=360.0f;
                YawUnit_val-=diff;
                headingTmp=CompassHeading;
            }
            if(YawUnit_val<=0.0f){
                YawUnit_val=0;
                Leftjoystick_X=0.0;
                Yaw_starting=false;
                YawUnit_val_text.setFocusable(true);
            }

        }
    }
    private void Climb_instruction(){
        if(Climb_starting) {
            if(ClimbTime_sel) {
                ClimbUnit_val -= (System.currentTimeMillis() - Climb_timestamp);
            }
            else {
                double timediff=(double) (System.currentTimeMillis() - Climb_timestamp)/1000.0;
                ClimbUnit_val-=(timediff*VelocityZ);

            }
            Climb_timestamp = System.currentTimeMillis();
            if ( ClimbUnit_val<= 0.0f) {
                ClimbUnit_val=0;
                Leftjoystick_Y = 0.0f;
                Climb_starting = false;
                ClimbUnit_val_text.setFocusable(true);
            }

        }
    }
    private  void ClimbNYaw_insturction(){
        if(Yaw_starting||Climb_starting){
            Yaw_instruction();
            Climb_instruction();

        }
        setLeftStick((float)Leftjoystick_X,(float)Leftjoystick_Y);

    }

    private  Runnable mAutoFlightprocess=new Runnable()
    {
        @Override
        public void run() {

            while(Flight_starting|Yaw_starting|Climb_starting){
                AutoFlight=true;
                ClimbNYaw_insturction();
                Flight_instruction();
                String buffer="FlightRemain:"+Float.toString((float)FlightUnit_val)+" YawRemain:"+Float.toString((float)YawUnit_val)+" ClimbRemain:"+ Float.toString((float)ClimbUnit_val);
                ToastUtils.setResultToText(textView,buffer);
                status_solid.post(new Runnable() {
                    @Override
                    public void run() {
                        status_solid.setBackgroundColor(0xB2FFFF00);
                    }
                });

            }
            AutoFlight=false;
            status_solid.post(new Runnable() {
                @Override
                public void run() {
                    if(ManFlight)
                        status_solid.setBackgroundColor(0xB200FF00);
                    else
                        status_solid.setBackgroundColor(0xB2FF0000);
                }
            });
        }
    };

    private void initFlightController(){
        flightController.setStateCallback(
                new FlightControllerState.Callback(){
                    @Override
                    public void onUpdate(FlightControllerState  FlightControllerState_in) {
                        mFlightControllerState=FlightControllerState_in;
                        VelocityX=mFlightControllerState.getVelocityX();
                        VelocityY=mFlightControllerState.getVelocityY();
                        VelocityZ=mFlightControllerState.getVelocityZ();
                        CompassHeading=flightController.getCompass().getHeading();
                        double angleNow = bearing_process(VelocityX, VelocityY)+90;
                        String buffer="Vx:"+Float.toString(VelocityX)+" Vy:"+Float.toString(VelocityY)+" Vz:"+Float.toString(VelocityZ)+" Heading:"+Float.toString(CompassHeading)+" Flying:"+ Float.toString((float)angleNow);
                        ToastUtils.setResultToText(flight_data,buffer);
                    }
                }
        );

    }

    private void AutoInsturction_flight(){
        if(!AutoFlight){
            //colortmp=status_solid.get
            textView.setText("Start");
            FlightProcessThread=new Thread(mAutoFlightprocess);
            FlightProcessThread.start();
        }
    }

    @Override
    public void onClick(View v) {
        distance_offset=Float.parseFloat(distance_offset_text.getText().toString());
        flightController = ModuleVerificationUtil.getFlightController();
        if (flightController == null) {
            return;
        }
        else{
            initFlightController();
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
                AutoInsturction_flight();
                break;
            case R.id.btn_start_Yaw:
                set_Yaw_perm();
                AutoInsturction_flight();
                break;
            case R.id.btn_start_Climb:
                set_Climb_perm();
                AutoInsturction_flight();
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
        else if (compoundButton == btn_man_flight) {
            if(b) {
                ManFlight=true;
                stop_inst();
                status_solid.setBackgroundColor(0xB200FF00);

            }
            else{
                ManFlight=false;
                status_solid.setBackgroundColor(0xB2FF0000);

            }
        }
        else if(compoundButton==FlightUnit_select){
            FlightTime_sel=b;

        }
        else if(compoundButton==YawUnit_select){
            YawTime_sel=b;

        }
        else if(compoundButton==ClimbUnit_select){
            ClimbTime_sel=b;
        }
    }

    private void onClickSimulator(boolean isChecked) {
        Simulator simulator = ModuleVerificationUtil.getSimulator();
        if (simulator == null) {
            return;
        }
        if (isChecked) {

            simulator.start(InitializationData.createInstance(new LocationCoordinate2D(23, 113), 10, 10),
                    new CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {

                        }
                    });
        } else {

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

