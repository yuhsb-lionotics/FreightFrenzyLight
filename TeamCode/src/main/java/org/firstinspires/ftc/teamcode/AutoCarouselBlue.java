package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;
import org.openftc.easyopencv.OpenCvWebcam;

import java.util.ArrayList;
import java.util.List;

@Autonomous(name = "Blue Carousel")
public class AutoCarouselBlue extends Hardware {

    OpenCvInternalCamera phoneCam;
    OpenCvWebcam webcam;
    OpenCvDetector pipeline = new OpenCvDetector();
    public OpenCvDetector.ElementLocation elementLocation = OpenCvDetector.ElementLocation.ERROR;
    double angleToTurnTo, angleThatis,forwardInches  = 0;
    int diff;
    int delay = 0;
    public ParkingPosition pos = ParkingPosition.STORAGE_UNIT;



    ParkingPosition parkingPosition = ParkingPosition.WAREHOUSE_TOWARDS_SHARED_HUB;

        @Override
        public void runOpMode(){
            int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
            webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
            webcam.setPipeline(pipeline);
            webcam.setMillisecondsPermissionTimeout(2500); // Timeout for obtaining permission is configurable. Set before opening.
            webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
            {
                @Override
                public void onOpened()
                {
                    webcam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
                }

                @Override
                public void onError(int errorCode)
                {
                    /*
                     * This will be called if the camera could not be opened
                     */
                    elementLocation = OpenCvDetector.ElementLocation.ERROR;
                }
            });
            hardwareSetup();
            imuSetup();
            /*
             * The INIT-loop:
             * This REPLACES waitForStart!
             */

            while (!isStarted() && !isStopRequested())
            {
                elementLocation = pipeline.getLocation();
                telemetry.addData("Realtime analysis", elementLocation);
                telemetry.addData("Frame Count", webcam.getFrameCount());
                telemetry.addData("Selected Delay",delay);
                telemetry.addData("Selected Parking Position",pos);
                telemetry.update();

                // Don't burn CPU cycles busy-looping in this sample
                sleep(100);
            }

           // When we hit this point start has been pressed. First thing to do is save where we have the marker as being
            elementLocation = pipeline.getLocation();
            webcam.stopStreaming();
            telemetry.addData("Final location:", elementLocation);
            telemetry.update();
            sleep(delay * 1000);

            switch (elementLocation){
                case LEFT:
                    raiseClawPos(LOW_POSITION,0.7);
                    forwardInches = 1;
                    break;
                case MIDDLE:
                    raiseClawPos(MIDDLE_POSITION,0.7);
                    forwardInches = 2.5;
                    break;
                case RIGHT:
                case ERROR:
                    raiseClawPos(HIGH_POSITION,0.7);
                    forwardInches = 3;
                    break;
            }
            //@TODO: combine these two driving commands
            //move diagonally towards the Shipping Hub
            encoderDrive(0.6, 33, 0, 33, 0);
            //move forward a little
            encoderDriveAnd(0.3, forwardInches,  forwardInches, forwardInches, forwardInches);
            while(clawPulley.isBusy()) {
                telemetry.addData("Status","waiting for clawPulley");
                telemetry.update();
                idle();
            }

            //release the pre-load box
            telemetry.addData("Status","releasing pre-load box");
            telemetry.update();
            grabber.setPower(-1);
            sleep(1000);
            grabber.setPower(0);


            // move back
            telemetry.addData("Status","Going to Carousel");
            telemetry.update();
            encoderDriveAnd(0.8, -(14.5 +forwardInches), -(14.5 +forwardInches), -(14.5 +forwardInches), -(14.5 +forwardInches));
            rotate(90,1, false);
            // Note orientation so we can move rotate back if needed
            // @TODO: change this to absolute rather than relative
            angleToTurnTo = getAngle();
            telemetry.addData("AngleToTurnTo",angleToTurnTo);
            telemetry.update();
            raiseClawPos(LOW_POSITION,0.6);
            encoderDriveAnd(0.8,-32,-32,-32,-32);
            //approach the carousel diagonally
            encoderDrive(0.4,0,-5,0,-5);


            // Spin duck
            telemetry.addData("Status","Spinning Duck");
            telemetry.update();
            carousel.setPower(-1);
            sleep(3000);
            carousel.setPower(0);

            // GO TO WAREHOUSE:
            // move away from the carousel
            telemetry.addData("Status","Parking in " + parkingPosition);
            telemetry.update();
            encoderDriveAnd(0.8,3,3,3,3);
            angleThatis = getAngle();
            diff = -(int) Math.round((angleThatis - angleToTurnTo) * 1000 ) / 1000;
            telemetry.addData("Diff",diff);
            telemetry.addData("AngleToTurnTo",angleToTurnTo);
            telemetry.addData("AngleThatIs",angleThatis);
            telemetry.update();
            rotate(diff, 0.9, true);
            //avoid sitting duck alliance partner
            encoderDrive(0.8,0,24,0,24);
            encoderDriveAnd(0.8,20,20,20,20);
            encoderDriveAnd(0.8,24,0,24,0);

        }
        public void selectParameters(){
            boolean delaySelected = false;
            boolean parkingSelected = false;

            // Delay
            // Parking Position
            // Start location ?
            while(!delaySelected){
                telemetry.addData("Currently Selecting", "Delay");
                telemetry.addData("Press Dpad up to raise value","Press Dpad down to lower value");
                telemetry.addData("Press a to make final","!");
                telemetry.addData("Current selection", delay);
                telemetry.update();
                if(gamepad1.dpad_up){
                    delay++;
                } else if(gamepad1.dpad_down){
                    delay--;
                } else if (gamepad1.a){
                    delaySelected = true;
                }
                idle();
            }
            List<ParkingPosition> positions = new ArrayList<>();
            positions.add(ParkingPosition.STORAGE_UNIT);
            positions.add(ParkingPosition.WAREHOUSE_JUST_INSIDE);
            positions.add(ParkingPosition.WAREHOUSE_TOWARDS_SHARED_HUB);
            positions.add(ParkingPosition.WAREHOUSE_AGAINST_BACK_WALL);
            int num = 0;

            while(!parkingSelected){
                telemetry.addData("Selected Delay",delay);
                telemetry.addData("Currently Selecting", "Parking Position");
                telemetry.addData("Press Dpad up to raise value","Press Dpad down to lower value");
                telemetry.addData("Press A to make final","!");
                telemetry.addData("Current selection", pos);
                pos = positions.get(num);
                if(gamepad1.dpad_up){
                    if(num <= 3){
                        num++;
                    } else {
                        num = 0;
                    }
                } else if (gamepad1.dpad_down){
                    if(num >= 1){
                        num--;
                    } else {
                        num = 3;
                    }
                } else if(gamepad1.a){
                    parkingSelected = true;
                }
                idle();
            }

        }

    }


