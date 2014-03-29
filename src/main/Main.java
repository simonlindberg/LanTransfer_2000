package main;

import java.net.InetAddress;
import java.net.UnknownHostException;

import GUI.GUI;

public class Main {
	public static void main(String[] args) throws UnknownHostException {
		new GUI(System.getProperty("user.name"), InetAddress.getLocalHost().getHostAddress());
	}
}