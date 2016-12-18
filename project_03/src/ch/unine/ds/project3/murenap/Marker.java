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

import java.io.Serializable;

public class Marker implements Serializable{
	private static int NEXT_ID = 0;
	
	private int id;
	private String initiator;
	public Marker(String initiator){
		this.id = Marker.NEXT_ID;
		Marker.NEXT_ID ++;
		this.initiator = initiator;
	}
	
	public String toString(){
		return this.initiator + "_" + Integer.toString(this.id);
	}
}
