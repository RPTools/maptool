/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 *
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 *
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.model;

/**
 * @author drice
 * 
 */
public class TestModelSerialization {
	//
	//    private static final Zone generateZone() {
	//        Zone z = new Zone("FOOBAR".getBytes());
	//        z.setGridScale(107);
	//        
	//        return z;
	//    }
	//
	//    public static void main(String[] args) throws IOException {
	//        ByteArrayOutputStream bout = new ByteArrayOutputStream();
	//        HessianOutput hout = new HessianOutput(bout);
	//
	//        try {
	//            hout.call("test", new Object[] { generateZone() });
	//        } catch (IOException e) {
	//            e.printStackTrace();
	//        }
	//
	//        byte[] data = bout.toByteArray();
	//
	//        HessianInput in = new HessianInput(new ByteArrayInputStream(data));
	//        in.startCall();
	//        List<Object> arguments = new ArrayList<Object>();
	//        while (!in.isEnd()) {
	//            arguments.add(in.readObject());
	//        }
	//        in.completeCall();
	//        
	//        Zone z = (Zone) arguments.get(0);
	//        
	//        System.out.println("background: " + new String(z.getBackground()));
	//        
	//        System.out.println(z.getGridScale());
	//
	//    }
}
