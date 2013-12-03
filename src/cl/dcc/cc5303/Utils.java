package cl.dcc.cc5303;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Utils {

	/**
	 * Counts how many elements of a boolean array are true
	 * 
	 * @param array the array to count
	 * @return the number of true elements
	 */
	public static int countTrue(boolean array[]) {
		int count = 0;
		for (int i=0; i<array.length; i++) {
			if (array[i]) count++;
		}
		return count;
	}
	
	public static void setFalse(boolean array[]) {
		for (int i=0; i<array.length; i++) {
			array[i] = false;
		}
	}
	
	public static class Pair<L,R> {
		private final L left;
		private final R right;

		public Pair(L left, R right) {
			this.left = left;
			this.right = right;
		}
		
		public L left() {
			return left;
		}
		
		public R right() {
			return right;
		}
	}
	
	public static void generateNewToken(String identifier, int num, Object data){
		String token = num + identifier;
		
		 File tokenDir = new File("tokens");
		 if (!tokenDir.exists()) {
			 tokenDir.mkdir();
		 }
		 
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(new FileOutputStream("tokens/" + token + ".ser"));
			out.writeObject("taken");
			out.writeObject(data);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			 
	}
	
	public static Object getToken(String token){
		Object data = null;
		
		try{
			FileInputStream door = new FileInputStream("tokens/" + token + ".ser");
			ObjectInputStream reader = new ObjectInputStream(door);
			data = reader.readObject();
			reader.close();
			return data;
		} catch (IOException | ClassNotFoundException e){ 
			return null; 
		}
	}
	
	public static void setToken(String token, Object data){
		 File tokenDir = new File("tokens");
		 if (!tokenDir.exists()) {
			 tokenDir.mkdir();
		 }
		 
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(new FileOutputStream("tokens/" + token + ".ser"));
			out.writeObject(data);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Object getFirstToken(String identifier){
		int minNum = 0;
		Object data = null;
		String fileName = minNum + identifier;
		
		try{
			while(true){
				FileInputStream door = new FileInputStream("tokens/" + fileName);
				ObjectInputStream reader = new ObjectInputStream(door);
				data = (String) reader.readObject();
				if (data.equals("taken")) {
					fileName = (++minNum) + identifier;
					reader.close();
				} else {
					data = reader.readObject();
					reader.close();
					return data;
				}
			}
		}
		catch (IOException | ClassNotFoundException e){ 
			return null; 
		}
	}
	
	public static void releaseToken(String identifier, int num, Object data){
		String token = num + identifier;
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(new FileOutputStream("tokens/" + token + ".ser"));
			out.writeObject("released");
			out.writeObject(data);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
