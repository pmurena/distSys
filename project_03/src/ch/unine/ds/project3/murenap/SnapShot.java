/*
 * 
 * Author: Patrick Murena
 * Semester: Autumn 2016
 * Course: Distributed Systems
 * Problem Set: Project 3
 * Professor: Peter Kropf and Pierre Kuonen
 * Assistants: Veronica Estrada, Andrei Lapin
 * 
*/
package ch.unine.ds.project3.murenap;


import org.jgroups.View;

public class SnapShot {

	private static int DELAY = 0;
	public SnapShot(){
		// Deactivate default constructor.
	}
	
	public static boolean trigger(int currentCoins, View view){
		int nextInt = (int)(Math.random() * (currentCoins * view.getMembers().size()))+1;
		if(currentCoins == nextInt){
			return true;
		}
		return false;
	}
}
