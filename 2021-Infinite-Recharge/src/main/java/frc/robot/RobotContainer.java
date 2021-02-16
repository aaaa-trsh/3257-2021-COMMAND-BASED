package frc.robot;

import java.util.ArrayList;
import java.util.List;

import com.ctre.phoenix.music.Orchestra;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.controller.RamseteController;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig;
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.util.Units;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import frc.robot.utils.RamseteCommand;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Magazine;
import frc.robot.subsystems.Shooter;
import frc.robot.utils.RamseteHelper;
import frc.robot.utils.control.XboxJoystick;
import frc.robot.Constants.DriveConstants;
import frc.robot.Constants.OIConstants;
import frc.robot.Constants.ShooterConstants;
import frc.robot.commands.AutoShoot;

public class RobotContainer {
    Shooter shooter = new Shooter();
    Drivetrain drivetrain = new Drivetrain();
    Magazine magazine = new Magazine();
    SendableChooser<Command> autoChooser = new SendableChooser<Command>();
    XboxJoystick driverController = new XboxJoystick(OIConstants.driverControllerPort);
    XboxJoystick operatorController = new XboxJoystick(OIConstants.operatorControllerPort);
    Orchestra orchestra;

    public RobotContainer() {
        orchestra = new Orchestra(drivetrain.getTalonFXs());
        orchestra.loadMusic("gourmet_race.chrp");

        SmartDashboard.putData(autoChooser);

        // Set drive and magazine commands
        drivetrain.setDefaultCommand(new RunCommand(() -> drivetrain.arcadeDrive(driverController.getLeftStickYValue(), -driverController.getRightStickXValue()), drivetrain)); 
        //magazine.setDefaultCommand(new AutoMagazine(magazine, shooter));
        configureButtonBindings();
    }

    public void log(){
        //SmartDashboard.putNumber(key, value)
    }

    // DRIVER
    // BALL LOCK - Hold A
    // TARGET LOCK - Hold X
    // INTAKE IN - Left Trigger
    // INTAKE OUT - Left Bumper

    // OPERATOR
    // SHOOT - Right Trigger
    // SHOOT MODE - Right Bumper
    // MAGAZINE IN - Left Trigger
    // MAGAZINE OUT - Left Bumper
    // AUTO SHOOT - Press A
    public void configureButtonBindings() {
        // DRIVER        
        // BALL LOCK - Hold A
        driverController.aButton
            .whenHeld(new RunCommand(() -> drivetrain.arcadeDrive(driverController.getLeftStickYValue(), -magazine.getIntakeLimelight().getYawError() * DriveConstants.trackingGain), drivetrain))
            .whenActive(() -> magazine.getIntakeLimelight().setLightState(3))
            .whenInactive(() -> magazine.getIntakeLimelight().setLightState(1));
        
        // TARGET LOCK - Hold X
        driverController.xButton
            .whenHeld(new RunCommand(() -> drivetrain.arcadeDrive(driverController.getLeftStickYValue(), (-shooter.getShooterLimelight().getYawError() * DriveConstants.shootingTrackingGain)), drivetrain))
            .whenActive(() -> {
                shooter.getShooterLimelight().setLightState(3);
                shooter.getShooterLimelight().setPipeline(0);
            })
            .whenInactive(() -> {
                shooter.getShooterLimelight().setPipeline(2);
                shooter.getShooterLimelight().setLightState(1);
            });

        // INTAKE IN - Left Trigger
        driverController.leftTriggerButton
            .whenActive(() -> magazine.setIntakeSpeed(.7))
            .whenInactive(() -> magazine.setIntakeSpeed(0));

        // INTAKE IN - Left Trigger
        driverController.rightTriggerButton
            .whenActive(() -> {
                drivetrain.setSlowMode(true);
            })
            .whenInactive(() -> {
                drivetrain.setSlowMode(false);
            });

        // INTAKE OUT - Left Bumper
        driverController.leftBumper
            .whenActive(() -> magazine.setIntakeSpeed(-.7))
            .whenInactive(() -> magazine.setIntakeSpeed(0));

        // OPERATOR
        // SHOOT - Right Trigger
        operatorController.rightTriggerButton
            .whenActive(() -> {
                shooter.setShooterPercent(1);
            })
            .whenInactive(() -> shooter.setShooterPercent(0));
        
        // SHOOT MODE - Right Bumper
        /*operatorController.rightBumper
            .whenActive(() -> shooter.setSetpoint(ShooterConstants.longshotRPM))
            .whenInactive(() -> shooter.setSetpoint(ShooterConstants.defaultRPM));*/
        
        // SHOOT - Right Trigger
        operatorController.aButton
        .whenActive(() -> {
            shooter.setShooterPercent(.7);
        }).whenInactive(() -> shooter.setShooterPercent(0));

        // MANUAL MAGAZINE - Left Trigger
        operatorController.leftTriggerButton
            .whenActive(() -> magazine.setMagazineSpeed(-.7))
            .whenInactive(() -> magazine.setMagazineSpeed(0));

        // MANUAL MAGAZINE OUT - Left Bumper
        operatorController.leftBumper
            .whenActive(() -> magazine.setMagazineSpeed(.7))
            .whenInactive(() -> magazine.setMagazineSpeed(0));
        // AUTO SHOOT - Press A
        //operatorController.aButton.whenActive(new AutoShoot(shooter, magazine));
    }

    /**
     * Gets the selected auto command
     * @return the selected auto command
     */
    public Command getAutonomousCommand() {
        //return autoChooser.getSelected();
        drivetrain.resetOdometry();
        var waypoints = new ArrayList<Translation2d>();
        //waypoints.add(new Translati)
        TrajectoryConfig config = new TrajectoryConfig(2, 2);

        Trajectory trajectory = TrajectoryGenerator.generateTrajectory(
            new Pose2d(0, 0, new Rotation2d(0)),
            waypoints,
            new Pose2d(2, 0, new Rotation2d(0)),
            config
        );
        drivetrain.resetOdometry(trajectory.getInitialPose());
        System.out.println("auto command !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println(drivetrain.getFeedForward());
        System.out.println(drivetrain.getWheelSpeeds());
        System.out.println(drivetrain.getLeftController());
        System.out.println(drivetrain.getRightController());
        return new RamseteCommand(
            trajectory,
            drivetrain::getPose,
            new RamseteController(2, .7),
            drivetrain.getFeedForward(),
            drivetrain.getKinematics(),
            drivetrain::getWheelSpeeds,
            drivetrain.getLeftController(),
            drivetrain.getRightController(),
            (leftVolts, rightVolts) -> {
                drivetrain.tankDriveVolts(leftVolts, rightVolts);
            },
            drivetrain
        ).andThen(() -> drivetrain.tankDriveVolts(0, 0));
    }


    ArrayList<Double> entries = new ArrayList<Double>();
    NetworkTableEntry autoSpeedEntry = NetworkTableInstance.getDefault().getEntry("/robot/autospeed");
    NetworkTableEntry telemetryEntry = NetworkTableInstance.getDefault().getEntry("/robot/telemetry");
    NetworkTableEntry rotateEntry = NetworkTableInstance.getDefault().getEntry("/robot/rotate");
    double priorAutospeed = 0;
    double[] data = new double[10];
    String entry = "";

    public void characterizationDisabled() {
        System.out.println("Robot disabled");
        drivetrain.tankDrive(0, 0);

        entry = entries.toString();
        entry = entry.substring(1, entry.length() - 1) + ", ";
        telemetryEntry.setString(entry);
        entries.clear();
        entry = "";
    }
    /**
     * Sends encoder values, volts, and more to calculate feedforward
     */
    public void characterizationPeriodic() {
        double now = Timer.getFPGATimestamp();

        double leftPosition = drivetrain.getLeftEncoderPosition();
        double leftRate = drivetrain.getLeftEncoderVelocity();

        double rightPosition = drivetrain.getRightEncoderPosition();
        double rightRate = drivetrain.getRightEncoderVelocity();

        double battery = RobotController.getBatteryVoltage();
        double motorVolts = battery * Math.abs(priorAutospeed);

        double leftMotorVolts = motorVolts;
        double rightMotorVolts = motorVolts;

        double autospeed = autoSpeedEntry.getDouble(0);
        priorAutospeed = autospeed;

        System.out.println(autospeed);

        drivetrain.tankDrive((rotateEntry.getBoolean(false) ? -1 : 1) * autospeed, autospeed);

        data[0] = now;
        data[1] = battery;
        data[2] = autospeed;
        data[3] = leftMotorVolts;
        data[4] = rightMotorVolts;
        data[5] = leftPosition;
        data[6] = rightPosition;
        data[7] = leftRate;
        data[8] = rightRate;
        data[9] = Rotation2d.fromDegrees(drivetrain.getHeading()).getRadians();

        for (double num : data) {
            entries.add(num);
        }
    }

    
    public void resetOdometry() {
        drivetrain.resetOdometry();
    }

    public void resetOdometry(Pose2d pose2d) {
        drivetrain.resetOdometry(pose2d);
    }
}