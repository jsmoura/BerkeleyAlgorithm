package br.com.mackenzie.slave;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Slave {

	private String ip, port, logfile;
	private LocalTime time;
	private long avg;
	private final Logger logger = Logger.getLogger(Slave.class.getName());
	private FileHandler fh = null;

	public Slave(String ip, String time, String logfile) {
		String[] splitIP = ip.split(":");
		String[] splitTime = time.split(":");
		LocalTime dateTime = LocalTime.of(Integer.parseInt(splitTime[0]), Integer.parseInt(splitTime[1]));

		this.ip = splitIP[0];
		this.port = splitIP[1];
		this.time = dateTime;
		this.logfile = logfile;
	}

	public void start() throws Exception {
		Logger();
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		String sentence, answer;
		boolean finished = true;
		String[] splitAnswer;
		DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(this.port), InetAddress.getByName(this.ip));
		logger.info("[SLAVE] : Server time - " + this.time);
		
		while (finished) {
			try {
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				sentence = new String(receivePacket.getData());
				logger.info("[SLAVE] :  Message received from Master.");
				

				if (sentence.trim().contains("?")) {
					
					logger.info("[MASTER] :" +sentence.trim());
					InetAddress IPAddress = receivePacket.getAddress();
					int port = receivePacket.getPort();
					answer = "" + this.time.getHour() + ":" + "" + this.time.getMinute();
					sendData = answer.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
					serverSocket.send(sendPacket);
					logger.info("[SLAVE] : Message answered to the master.");
					
				} else if (sentence.trim().contains(":")) {
					logger.info("[MASTER] :" +sentence.trim());
					serverSocket.close();
					splitAnswer = sentence.trim().split(":");
					avg = Long.parseLong(splitAnswer[1]);
					this.time = this.time.plusMinutes(avg);
					logger.info("[SLAVE] : The time is: "+ this.time);
					finished = false;
				} else {
					serverSocket.close();
					finished = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		logger.info("[SLAVE] : System down.");
	}
	
	private void Logger() {
		try {
			this.fh = new FileHandler("C:\\Users\\jsmou\\Desktop\\"+this.logfile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		logger.addHandler(this.fh);
	}

}
