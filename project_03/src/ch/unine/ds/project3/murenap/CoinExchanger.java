
package ch.unine.ds.project3.murenap;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.protocols.TP;

public class CoinExchanger extends ReceiverAdapter {
	public enum ExchangerState {
		STARTING, RUNNING, ENDED;
	}
	
	// Some variables needed for the completion of the exercises
	private Terminator coins = new Terminator();
	
	JChannel channel;
	View currentView;
	Address leader;
	Address myself;
	Random rand = new Random();
	ExchangerState state = ExchangerState.STARTING;
	
	public void viewAccepted(View view) {
		synchronized (this) {
			/** 
			 * On capturing the exiting of a player, we call the redeem function to claim back the not akcnoledged coins we
			 * sent to the player who has exited the game.
			 */
			this.coins.redeem(this.currentView, view);
			
			currentView = view;
			leader = currentView.getMembers().get(0);
		}
		System.out.println("** New view: " + currentView);
	}

	public void receive(Message msg) {
		synchronized (this) {
			/**
			 * Handle the incoming messages
			 */
			this.coins.inComing(this, msg);
		}
	}

	public void mainLoop() throws Exception {
		channel = new JChannel();
		channel.setReceiver(this);
		channel.connect("CoinExchangerGroup");
		TP tp = channel.getProtocolStack().getTransport();
		System.out.println("** Transport layer TP: " + tp);
		System.out.println("** Total sent by TP: " + tp.getNumMessagesSent());
		System.out.println("** Total received by TP: " + tp.getNumMessagesReceived());
		
		while (state != ExchangerState.ENDED) {
			sendCoin();
			updateThreadState();
			Thread.sleep(1000);
		}
		System.out.println("** Total sent by TP: " + tp.getNumMessagesSent());
		System.out.println("** Total received by TP: " + tp.getNumMessagesReceived());
		System.out.println("** Exiting, I have: " + coins + " coins");
		channel.close();
	}

	public void sendCoin() {
		List<Address> members = currentView.getMembers();
		Address luckyGuy = members.get(rand.nextInt(members.size()));

		Message msg = new Message(luckyGuy, null, "Coin");
		
		try {
			channel.send(msg);
			synchronized (this) {
				/**
				 * We call the send coin function of the Terminator in order to decrease the coin count and add an entry to 
				 * the outgoing buffer to keep track of coins in transit. 
				 * Coins will only be sent if there are effectivly coins left (No credit allowed).
				 */
				this.coins.send(luckyGuy);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public synchronized void updateThreadState() {
		if (currentView.getMembers().size() > 1) {
			state = ExchangerState.RUNNING;
		} else {
			if (state == ExchangerState.RUNNING) {
				state = ExchangerState.ENDED;
			}
		}
		boolean hasInput = false;
		try {
			hasInput = (System.in.available() > 0);
		} catch (IOException e) {
		}
		/**
		 * The coin count has been replaced with the coins counter of the Terminator class as we insure to acount
		 * for the coins in transit by doing so. The logical test has been changed to == as the Terminator Class
		 * does not send coins that it doesn't hase.
		 */
		if (this.coins.getCoinsCount() == 0 || hasInput) {
			state = ExchangerState.ENDED;
		}
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("java.net.preferIPv4Stack", "true");
		CoinExchanger ce = new CoinExchanger();
		try {
			ce.mainLoop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}