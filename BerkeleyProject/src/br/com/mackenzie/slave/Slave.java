package br.com.mackenzie.slave;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalTime;

public class Slave {

	private String ip, port, logfile, sentence;
	private LocalTime time;
	private long avg;

	public Slave(String ip, String time, String logfile) {
		String[] splitIP = ip.split(":");
		String[] splitTime = time.split(":");
		LocalTime dateTime = LocalTime.of(Integer.parseInt(splitTime[0]), Integer.parseInt(splitTime[1]));

		this.ip = splitIP[0];
		this.port = splitIP[1];
		this.time = dateTime;
		this.logfile = logfile;
	}

	public void start() {
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		String sentence, answer;
		boolean finished = true;
		String[] splitAnswer;

		while (finished) {
			try {
				DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(this.port),
						InetAddress.getByName(this.ip));
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				sentence = new String(receivePacket.getData());

				if (sentence.trim().contains("?")) {
					InetAddress IPAddress = receivePacket.getAddress();
					int port = receivePacket.getPort();
					answer = "" + this.time.getHour() + ":" + "" + this.time.getMinute();
					sendData = answer.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
					serverSocket.send(sendPacket);
					
				} else if(sentence.trim().contains(":")){
					splitAnswer = sentence.split(":");
					avg = Long.parseLong(splitAnswer[1]);
					this.time.plusMinutes(avg);
					serverSocket.close();
					finished = false;
				} else {
					finished = false;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
