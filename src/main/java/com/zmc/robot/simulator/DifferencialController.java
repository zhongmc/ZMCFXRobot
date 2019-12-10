package com.zmc.robot.simulator;

import org.apache.log4j.Logger;

public class DifferencialController extends Controller {

	private final static String TAG = "DIF_CTRL";


	private double lastErrorIntegration1, lastError1;

	Logger log = Logger.getLogger(TAG);
	Output output = new Output();

	public DifferencialController() {
	}


	@Override
	Output execute(AbstractRobot robot, Input input, double dt) {


		double sv = input.v;
		double sw = input.w;
	
		double max_w = robot.max_w;
	
			if (sw > max_w)   //限制拐弯时的转速
			  sw = max_w;  
			else if (sw < -max_w)
			  sw = -max_w;
	
		if( sv != 0 )  //拐弯减速
		{
		  double av = Math.abs(sv);
		  av = (0.05 - av)/max_w * Math.abs(sw) + av;
		  if( av <= 0 )
			av = 0.01;
	
		  if( sv < 0  )
			av = -av;
		  
		  sv = av;
		}
	
		// log.info( String.format("%.3f,%.3f,%.3f,%.3f", input.v, input.w, sv, sw));


	  Vel vel = robot.uni_to_diff(sv, sw);
	
	  if( sv != 0 && vel.vel_l *vel.vel_r < 0)
	  {
		if( Math.abs(vel.vel_l) > Math.abs(vel.vel_r ) )
		vel.vel_r = 0;
		else
		{
		  vel.vel_l = 0;
		}
		
	  } 
	  double e1, e2, ei1, ei2, ed1,ed2;
	  
	  e1 = vel.vel_l - robot.vel_l;
	  e2 = vel.vel_r - robot.vel_r;
	
	  ei1 = lastErrorIntegration + e1 * dt;
	  ei2 = lastErrorIntegration1 + e2 * dt;
	  
	  if( Math.abs(ei1)*Ki > robot.max_vel  )
		  ei1 = ei1/3; 
	  if( Math.abs(ei2)*Ki > robot.max_vel  )
		  ei2 = ei2/3; 
	

	  ed1 = (e1-lastError)/dt;
	  ed2 = (e2-lastError1)/dt;
	
	  lastError = e1;
	  lastError1 = e2;
	
	  double vel_l = Kp * e1 + Ki * ei1 + Kd * ed1;
	  double vel_r = Kp * e2 + Ki * ei2 + Kd * ed2;



	  double nvel_l = robot.normalizeVel(vel.vel_l, vel_l );
	  double nvel_r = robot.normalizeVel(vel.vel_r, vel_r );
	
	  if( sv != 0 && nvel_l * nvel_r < 0)
	  {
		if( Math.abs(nvel_l) > Math.abs(nvel_r ) )
			nvel_r = 0;
		else
		{
		  nvel_l = 0;
		}
		
	  }
	
	  lastErrorIntegration = ei1;
	  lastErrorIntegration1 = ei2;
	
	  output.vel_l = nvel_l;
	  output.vel_r = nvel_r;

	  log.info( String.format("v:%.3f, w:%.3f, e1:%.3f, e2:%.3f, ei1:%.3f, ei2:%.3f, lc:%.3f, rc:%.3f, nl:%.3f, nr:%.3f", 
				  sv, sw, e1, e2, lastErrorIntegration, lastErrorIntegration1, 
				  vel_l, vel_r, nvel_l, nvel_r));


	  return output;
	}

	@Override
	void reset() {
		lastError = 0;
		lastErrorIntegration = 0;

		lastErrorIntegration1=0;
		lastError1 = 0;

	}

	public void updateSettings(Settings settings) {
        Kp = settings.pkp;
        Ki = settings.pki;
        Kd = settings.pkd;

        // Log.i("CTRL", "Update settings: kp=" + Kp + ", ki=" + Ki + ",kd=" + Kd);

    }


	@Override
	void getControllorInfo(ControllerInfo state) {
	}

}
