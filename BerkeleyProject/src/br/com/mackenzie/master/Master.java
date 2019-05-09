package br.com.mackenzie.master;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import br.com.mackenzie.slave.Slave;

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
	private final Logger logger = Logger.getLogger(Slave.class.getName());
	private FileHandler fh = null;

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
		this.times = new ArrayList<>();
		this.servers = new ArrayList<>();
	}

	public void start() {
		Logger();
		readSlaveFile();
		callSlaves();
		calculateAvg();
		setTime();
		answerSlaves();
	}
	
	private void readSlaveFile() {
		ArrayList<String> records = new ArrayList<String>();
		try {
			logger.info("[MASTER] : Reading de slave file...");
			BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\jsmou\\Desktop\\"+this.slavefile));
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

	private void callSlaves() {
		String sentence = "What time is it?";
		sendData = sentence.getBytes();

		for (String server : this.servers) {
			String hour;
			String[] split = server.split(":");
			String[] splitHourSlave;

			try {
				logger.info("[MASTER] : Creating a communication with " + server + ".");
				IPAddress = InetAddress.getByName(split[0]);
				DatagramSocket clientSocket = new DatagramSocket(Integer.parseInt(this.port),
						InetAddress.getByName(this.ip));
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, (InetAddress) IPAddress,
						Integer.parseInt(split[1]));
				clientSocket.send(sendPacket);

				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				clientSocket.receive(receivePacket);
				hour = new String(receivePacket.getData());
				logger.info("[MASTER] : Receiving the slave hour - "+hour.trim());
				clientSocket.close();

				splitHourSlave = hour.trim().split(":");
				LocalTime lt = LocalTime.of(Integer.parseInt(splitHourSlave[0]), Integer.parseInt(splitHourSlave[1]));

				this.times.add(lt);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void calculateAvg() {

		long avg = 0;
		long count = 1;
		int index = 0;

		logger.info("[MASTER] : Calculating the average.");
		for (LocalTime timeSlave : this.times) {
			long interval = ChronoUnit.MINUTES.between(timeSlave, this.time);
			if (this.d > interval) {
				avg += interval;
				count++;
			} else {
				this.times.remove(index);
				this.servers.remove(index);
				index--;
			}
			index++;
		}

		this.avg = avg / count;
		logger.info("[MASTER] : The average found was: "+ this.avg);
	}

	private void setTime() {
		this.time = this.time.plusMinutes(avg);
	}

	private void answerSlaves() {
		String sentence = "The difference in minutes is :" + this.avg;
		sendData = sentence.getBytes();

		for (String server : this.servers) {
			String[] split = server.split(":");

			try {
				logger.info("[MASTER] : Answering the "+ server +" with the deviation of "+ this.avg);
				IPAddress = InetAddress.getByName(split[0]);
				DatagramSocket clientSocket = new DatagramSocket(Integer.parseInt(this.port),
						InetAddress.getByName(this.ip));
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, (InetAddress) IPAddress,
						Integer.parseInt(split[1]));
				clientSocket.send(sendPacket);
				System.out.println(sentence);
				clientSocket.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		logger.info("[MASTER] : System down."); 

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
