package br.com.mackenzie.start;

import br.com.mackenzie.master.Master;
import br.com.mackenzie.slave.Slave;

public class Initialize {

	public static void main(String[] args) throws Exception{
		
		switch (args[0]) {
		case "-m":
			if (args.length == 6) {
				System.out.println("You create a master of time.");
				Master master = new Master(args[1], args[2], Integer.parseInt(args[3]), args[4], args[5]);
				master.start();
			} else {
				System.out.println("It was not possible to create a master of time.");
			}
			break;

		case "-s":
			if (args.length == 4) {
				System.out.println("You create a slave of time.");
				Slave slave = new Slave(args[1], args[2], args[3]);
				slave.start();
			} else {
				System.out.println("It was not possible to create a slave of time.");
			}
			break;

		default:
			System.out.println("You have entered a bad numbers of parameters");
			break;
		}
		
	}
}
