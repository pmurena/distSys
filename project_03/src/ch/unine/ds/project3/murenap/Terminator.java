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

import java.util.LinkedHashMap;
import java.util.Map;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.View;

public class Terminator{
	/**
	 * The Terminator class handles the coin exchange between nodes.
	 * It, in particular, addresses the termination issues faced by the original implementation.
	 * It makes sure, that no coins are sent if there aren't any to send, it keeps track of the coins
	 * offered to but not yet received by other nodes, and it handles the fact that a co-player has 
	 * trully received a coin or not.
	 * 
	 * In a nutshell it deals with the "termination with 1 coin" issue addressed in "work to do 2".
	 * 
	 */

	public static final int INIT_COINS = 3;
	
	private Map<Address, Integer> transaction;
	private int coins;
	
	public Terminator(){
		this.coins = Terminator.INIT_COINS;
		this.transaction = new LinkedHashMap<Address, Integer>();
	}

	private void addCoin(CoinExchanger ch, Message msg){
		/**
		 * Increments coins if and only if the total coin count is bigger than 0.
		 * return true in case it could add the coin and false otherwise.
		 */
		if(this.getCoinsCount() > 0){
			this.coins ++;
			try{
				ch.channel.send(new Message(msg.getSrc(), null, "Ack"));
			}catch(Exception e){
				System.out.println(e);
			}
			System.out.println("** Got coin from " + msg.getSrc() + ", now I have " + this.toString());
		}
	}
	
	private void confirm(CoinExchanger ch, Address src){
		/**
		 * removed a coin from the outgoing buffer upon ack receival.
		 * this will definitly remove the coin from the current player.
		 */
		this.transaction.put(src, transaction.get(src) - 1);
		ch.updateThreadState();
		System.out.println("** Got Ack from " + src + ", now I have " + this.toString());
		
	}
	
	public String toString(){
		return Integer.toString(this.getCoinsCount());
	}
	
	public void send(Address destination){
		/**
		 * Checks if enough coins are left to send one to a co-player.
		 * if so, coins are decremented and a coin is added to the outgoing
		 * buffer of the luckyGuy.
		 * Returns true if the transaction can be performed and false otherwise.
		 */
		if(this.coins > 0){
			int newVal = 1;
			this.coins --;
			if(this.transaction.containsKey(destination)){
				newVal += this.transaction.get(destination);
			}
			this.transaction.put(destination, newVal);
			System.out.println("** Sent coin to " + destination + ", now I have " + this.toString());
		}
	}
	
	public int getCoinsCount(){
		/**
		 * return the effective coin count, meaning the sum of coins
		 * and the coins in transit.
		 */
		int returnVal = this.coins;
		for(int val: this.transaction.values()){
			returnVal += val;
		}
		return returnVal;
	}
	
	public void inComing(CoinExchanger ch, Message msg){

		switch(msg.getObject().toString()){
		case "Coin":
			/**
			 * Upon receival of a coin we will add it to our coin count, if and only if we are still in the game. 
			 * In case the coin can be accepted, we acknolegde it receival, if not we do nothing.
			 */
			this.addCoin(ch, msg);
			break;
		case "Ack":
			/**
			 * When receiving an aknowledgement we remove the coin fomr the outgoing buffer an thus effectivly 
			 * decrease our coin count.
			 */
			this.confirm(ch, msg.getSrc());
			break;
		default:
			break;
		}



	}
	
	public void redeem(View currentView, View newView){
		/**
		 * When a player quits the game, all coins he hasn't claimed in the
		 * outgoing buffer must be redeemed to avoid to lose them.
		 */
		
		if(currentView != null){
			for(Address a: currentView.getMembers()){
				if(!newView.containsMember(a)){
					System.out.println("** redeem coins from" + a);
					if(this.transaction.containsKey(a)){
						this.coins += this.transaction.remove(a);
					}
				}
			}
		}
	}
}
