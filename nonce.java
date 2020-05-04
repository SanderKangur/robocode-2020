package rednas;
import robocode.*;
import java.awt.Color;
import robocode.util.Utils;
import java.awt.geom.*;

//Linear Targeting
//Width Lock
//Stop and Go
//Wall Avoidance

public class nonce extends AdvancedRobot{
private byte moveDirection = 1;
static double prevEnergy = 100.0; 
private int wallMargin = 60; 
private int tooCloseToWall = 0;

	public void run() {
	
		setColors(Color.black, Color.black, Color.yellow, Color.orange, Color.white);
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
	
		addCustomEvent(new Condition("pusku") {
			public boolean test() {
				return (
					 //üleval
					 (getY() >= getBattleFieldHeight() - wallMargin ||
					 //parem
					 getX() >= getBattleFieldWidth() - wallMargin ||
					 //vasak
					 getX() <= wallMargin ||
					 //all
					 getY() <= wallMargin)
					 
					);
				}
			});
	
		do {
		    if ( getRadarTurnRemaining() == 0.0) setTurnRadarRightRadians(Double.POSITIVE_INFINITY );
			seina();
		    execute();
		    } while ( true );
	}
	
	
	public void onCustomEvent(CustomEvent e) {
		if (e.getCondition().getName().equals("pusku"))
		{
			if (tooCloseToWall <= 0) {
				tooCloseToWall += wallMargin;
				setMaxVelocity(0); 
			}
		}
	}


	public void onScannedRobot(ScannedRobotEvent e) {
	
		setTurnRight(e.getBearing() + 90 - (13 * moveDirection));
	
		/*************STOP AND GO*****************/

	    if(getDistanceRemaining()==0.0 && prevEnergy-e.getEnergy()>0.0)	setAhead(36*moveDirection);

		prevEnergy = e.getEnergy(); 
		  
		/*************WIDTH LOCK******************/
	 
		double angleToEnemy = getHeadingRadians() + e.getBearingRadians();
	 
	    double radarTurn = Utils.normalRelativeAngle( angleToEnemy - getRadarHeadingRadians() );
	 
	    double extraTurn = Math.min( Math.atan( 36.0 / e.getDistance() ), Rules.RADAR_TURN_RATE_RADIANS );
	
	    if (radarTurn < 0) radarTurn -= extraTurn;
	    else radarTurn += extraTurn;
	 
	    setTurnRadarRightRadians(radarTurn);
		
		/**************LINEAR TARGETING******************/
	 
		double bulletPower = Math.min(3.0, 400 / e.getDistance());
		double myX = getX();
		double myY = getY();
		double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
		double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
		double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
		double enemyHeading = e.getHeadingRadians();
		double enemyVelocity = e.getVelocity();
		 
		 
		double deltaTime = 0;
		double battleFieldHeight = getBattleFieldHeight(), 
		       battleFieldWidth = getBattleFieldWidth();
		double predictedX = enemyX, predictedY = enemyY;
		while((++deltaTime) * (20.0 - 3.0 * bulletPower) < 
		      Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
			predictedX += Math.sin(enemyHeading) * enemyVelocity;	
			predictedY += Math.cos(enemyHeading) * enemyVelocity;
			if(	predictedX < 18.0 
				|| predictedY < 18.0
				|| predictedX > battleFieldWidth - 18.0
				|| predictedY > battleFieldHeight - 18.0){
				predictedX = Math.min(Math.max(18.0, predictedX), 
		                    battleFieldWidth - 18.0);	
				predictedY = Math.min(Math.max(18.0, predictedY), 
		                    battleFieldHeight - 18.0);
				break;
			}
		}
		double theta = Utils.normalAbsoluteAngle(Math.atan2(
		    predictedX - getX(), predictedY - getY()));
		 
		setTurnRadarRightRadians(
		    Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
		setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10)	fire(bulletPower);	
	}
	

	public void seina() {

		if (getTime() % 17 == 0) {
			moveDirection *= -1;
			setAhead(170 * moveDirection);
		}
		if (tooCloseToWall > 0) tooCloseToWall--;

		if (getVelocity() == 0) {
			setMaxVelocity(8);
			moveDirection *= -1;
			setAhead(1000 * moveDirection);
		}
	}
}
