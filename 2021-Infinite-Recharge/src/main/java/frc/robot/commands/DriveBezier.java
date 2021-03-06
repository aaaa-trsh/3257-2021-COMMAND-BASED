package frc.robot.commands;

import java.awt.geom.Point2D;

import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystems.Drivetrain;
import frc.robot.utils.BezierCurve;

public class DriveBezier extends CommandBase 
{
    public double speed;
    public Point2D end, control1, control2;
    BezierCurve curve;
    boolean inverted;
    Drivetrain drivetrain;
    double timeAlongCurve = 0;

    public DriveBezier(double speed, BezierCurve curve, Drivetrain drivetrain) 
    {
        addRequirements(drivetrain);
        this.speed = speed;
        this.curve = curve;
        this.inverted = false;
        this.drivetrain = drivetrain;
    }

    public DriveBezier(double speed, BezierCurve curve, boolean inverted, Drivetrain drivetrain)
    {
        addRequirements(drivetrain);
        this.speed = speed;
        this.curve = curve;
        this.inverted = inverted;
        this.drivetrain = drivetrain;
    }

    public DriveBezier(double speed, Point2D end, Point2D control1, Point2D control2, Drivetrain drivetrain) 
    {
        addRequirements(drivetrain);
        this.speed = speed;
        this.curve = new BezierCurve(control1, control2, end);
        this.inverted = false;
        this.drivetrain = drivetrain;
    }

    public DriveBezier(double speed, Point2D end, Point2D control1, Point2D control2, boolean inverted, Drivetrain drivetrain) 
    {
        addRequirements(drivetrain);
        this.speed = speed;
        this.curve = new BezierCurve(control1, control2, end);
        this.inverted = inverted;
        this.drivetrain = drivetrain;
    }

    @Override
    public void initialize() {
    }


    @Override
    public void execute()
    {
        SmartDashboard.putNumber("Bezier Curve: ",  curve.getPos(timeAlongCurve).getY());
        System.out.println((drivetrain.getLeftEncoderPosition() + " " + drivetrain.getRightEncoderPosition() + " " + curve.getLength()));
        double temp = Math.abs((drivetrain.getLeftEncoderPosition() + drivetrain.getRightEncoderPosition()) / 2);
        timeAlongCurve = inverted ? (curve.getLength() - temp) - 0.02 : temp;

        System.out.println("T:" + timeAlongCurve + "CURVE ANGLE: " + curve.getAngle(timeAlongCurve) + ", GYRO:" + drivetrain.getHeading());
        drivetrain.arcadeDrive(inverted ? -speed : speed, -(curve.getAngle(timeAlongCurve / curve.getLength()) - drivetrain.getHeading()) * 0.02);
    }

    @Override
    public boolean isFinished() 
    {
        return false;
        //return inverted ? timeAlongCurve < 0.02 : timeAlongCurve >= curve.getLength();
    }

    double Clamp(double value, double min, double max)
    {
        double retVal;
        if (value < min) { retVal = min; } 
        else if (value > max) { retVal = max; } 
        else { retVal = value; }
        return inverted ? -retVal : retVal;
    }

    @Override
    public void end(boolean interrupted) 
    {
        drivetrain.tankDrive(0, 0);
    }
}