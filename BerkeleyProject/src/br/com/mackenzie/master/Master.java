package br.com.mackenzie.master;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Master {

	private String ip, port, slavefile, logfile;
	private LocalTime time;
	private int d;
	private ArrayList<String> servers;
	private byte[] sendData = new byte[1024];
	private byte[] receiveData = new byte[1024];
	private InetAddress IPAddress;
	private ArrayList<LocalTime> times;
	private long avg;

	public Master(String ip, String time, int d, String slavefile, String logfile) {
		String[] splitIP = ip.split(":");
		String[] splitTime = time.split(":");
		LocalTime dateTime = LocalTime.of(Integer.parseInt(splitTime[0]), Integer.parseInt(splitTime[1]));

		this.ip = splitIP[0];
		this.port = splitIP[1];
		this.slavefile = slavefile;
		this.time = dateTime;
		this.logfile = logfile;
		this.d = d;
	}

	public void start() {
		readSlaveFile();
		callSlaves();
		calculateAvg();
		setTime();
		answerSlaves();
	}

	private void callSlaves() {
		String sentence = "What time is it?";
		sendData = sentence.getBytes();

		for (String server : this.servers) {
			String hour;
			String[] split = server.split(":");
			String[] splitHourSlave;

			try {
				IPAddress = InetAddress.getByName(split[0]);
				DatagramSocket clientSocket = new DatagramSocket(Integer.parseInt(this.port), InetAddress.getByName(this.ip));
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, (InetAddress) IPAddress,
						Integer.parseInt(split[1]));
				clientSocket.send(sendPacket);

				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				clientSocket.receive(receivePacket);
				hour = new String(receivePacket.getData());
				splitHourSlave = hour.split(":");
				LocalTime lt = LocalTime.of(Integer.parseInt(splitHourSlave[0]), Integer.parseInt(splitHourSlave[0]));

				clientSocket.close();

				this.times.add(lt);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void readSlaveFile() {
		ArrayList<String> records = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this.slavefile));
			String line;
			while ((line = reader.readLine()) != null) {
				records.add(line);
			}
			reader.close();
			this.servers = records;
		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", this.slavefile);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void calculateAvg() {

		long avg = 0;
		long count = 0;
		int index = 0;

		for (LocalTime timeSlave : this.times) {
			long interval = ChronoUnit.MINUTES.between(timeSlave, this.time);
			if (d > interval) {
				avg += interval;
				count++;
			} else {
				times.remove(index);
				servers.remove(index);
				index--;
			}
			index++;
		}

		this.avg = avg / count;
	}
	
	private void setTime() {
		this.time.plusMinutes(avg);
	}
	
	private void answerSlaves() {
		String sentence = "The difference in minutes is :" + this.avg;
		sendData = sentence.getBytes();

		for (String server : this.servers) {
			String[] split = server.split(":");

			try {

				IPAddress = InetAddress.getByName(split[0]);
				DatagramSocket clientSocket = new DatagramSocket(Integer.parseInt(this.port), InetAddress.getByName(this.ip));
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, (InetAddress) IPAddress,
						Integer.parseInt(split[1]));
				clientSocket.send(sendPacket);

				clientSocket.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

}
