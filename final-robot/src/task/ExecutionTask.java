package task;

import static robo.ConstantsVariables.IS_INTERRUPTED;

import lejos.hardware.Sound;
import out.LCDController;
import robo.Main;
import static robo.MovementController.*;

public class ExecutionTask implements Runnable {


	public void run() {
		Sound.playTone(440, 100, 10);
		LCDController.print("execution");



	}

	public void grapBall() {
		// TODO Auto-generated method stub

	}



}
