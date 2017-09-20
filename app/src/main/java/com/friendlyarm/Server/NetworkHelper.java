package com.friendlyarm.Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.SocketAddress;
import java.util.ArrayList;

/**
 * Created by magichuang on 17-4-16.
 */
public class NetworkHelper {
    public String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    public static ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    "/proc/net/arp"));

            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        connectedIP.remove(0);
        return connectedIP;
    }

    public static String getIpBySocketAddress(SocketAddress socketAddress) {
        String temp = socketAddress.toString();
        int i = 1;
        StringBuilder sb = new StringBuilder();
        while (temp.charAt(i) != ':') {
            sb.append(temp.charAt(i));
            i++;
        }
        return sb.toString();
    }
}
