package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.util.Range;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name="TeleOp")
public class TeleOp extends Hardware{
    @Override
    public void runOpMode() throws InterruptedException {
        hardwareSetup();
        boolean clawOpened = true;
        int clawPos = 1;
        waitForStart();
        claw.setPosition(clawPos);
        while(opModeIsActive()){
            tankControl(0.6); //CHANGE THIS
            if (gamepad1.a) {
                claw.setPosition(0.55);
            } else if (gamepad1.b) {
                claw.setPosition(1);
            } else if (gamepad1.dpad_up){
                raiseClaw(0.5);
            } else if (gamepad1.dpad_down){
                raiseClaw(-0.5);
            } else if (gamepad1.dpad_left){
                raiseClawPos(2645, 0.5);
            } else if (gamepad1.dpad_right){
                raiseClawPos(0, 0.5);
            } else if (gamepad1.x){
                carousel.setPower(0.5);
            }
            else {
                raiseClaw(0);
                setCaroselPower(0);
            }
            telemetry.addData("ClawPos",clawPos);
            telemetry.update();


            // Top should be 2545




        }
    }
    public void tankControl(double maxPower) /* 0 < maxPower <= 1 */ {
        double leftPower = -gamepad1.left_stick_y * maxPower;
        double rightPower = -gamepad1.right_stick_y * maxPower;
        double strafePower = (gamepad1.left_stick_x + gamepad1.right_stick_x)/2 * maxPower;

        //double strafePower = (gamepad1.right_trigger - gamepad1.left_trigger) * maxPower; //positive is to the right

        double strafePowerLimit = Math.min(1 - Math.abs(rightPower) , 1 - Math.abs(leftPower));
        strafePower = Range.clip(strafePower, -strafePowerLimit, strafePowerLimit);

        // This will set each motor to a power between -1 and +1 such that the equation for
        // holonomic wheels works.
        frontLeft.setPower(leftPower  + strafePower);
        backLeft.setPower(leftPower  - strafePower);
        frontRight.setPower(rightPower - strafePower);
        backRight.setPower(rightPower + strafePower);
    }
}
