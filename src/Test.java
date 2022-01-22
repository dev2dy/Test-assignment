import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class Test {

	// Every word is a key and has its own duration value.
	static Map<String, Double> words = new HashMap<>();
	// Every word stored (multiple time) in display order.
	static ArrayList<String> allWords = new ArrayList<String>();
	// Global input scanner.
	static final Scanner sc = new Scanner(System.in).useLocale(Locale.US);
	
	// Upload a file
	public static String Upload() {
		
		String input = null;
		File file;
		do {
			System.out.print("\nEnter the file's absolute path with its extension! \nPath: ");
			input = sc.next();
			input = input.replace('\\', '/');
			
			file = new File(input);	
			
			if (input.charAt(0) == 'e') {
				input = null;
				break;
			}
			else if (!file.exists()) {
				System.out.println("The file does not exist! Try again or enter 'e' to exit.");
			}
			
			if (!input.contains(".srt")) {
				System.out.println("It must be an .srt file! Make sure you included the extension!");
			}
			
		} while (!file.exists());
		
		return input;
	}
	
	public static void LongestDisplayed(String path) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String str;
			double duration = 0;
			String from;
			String to;
			while ((str = br.readLine()) != null) {
				if (str.contains("-->")) {
					
					// Changing format to be able to calculate with it.
					from = str.substring(0, (str.indexOf(" ")+1));
					to = str.substring(str.lastIndexOf(" "));
					from = from.replace(',', '.');
					to = to.replace(',', '.');
					
					duration = Converter(to);
					double forSubtraction = Converter(from);
					duration -= forSubtraction;
					
					// Storing the key, value pairs, and each of the word in display order.
					String arr[] = null;
					while ((str = br.readLine()) != null && !str.trim().isEmpty()) {
						str = str.toLowerCase();
						str = str.replaceAll("[^a-zA-Z0-9\\s'#]", "");
						arr = str.split(" ");
						for (int i = 0; i < arr.length; i++) {
							// The words only be checked if they have more then 0 characters.
							if (arr[i].length() > 0) {
								if (!words.containsKey(arr[i])) {
									words.put(arr[i], duration);
								}
								else if (words.containsKey(arr[i])) {
									double currDuration = duration + words.get(arr[i]);
									words.replace(arr[i], currDuration);
								}
								allWords.add(arr[i]);
							}
						}
					}
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Looking for the word that has the longest display duration.
		double longestDuration = 0;
		String theWord = null;
		for (Map.Entry<String, Double> entry : words.entrySet()) {
			if (entry.getKey().length() > 3 && entry.getValue() > longestDuration) {
				longestDuration = entry.getValue();
				theWord = entry.getKey();
			}
		}
		System.out.println("Displayed word for the longest amount of time: " + theWord);
	}
	
	public static void Observe() {
		
		// Input word with validation.
		String input = null;
		boolean exit = false;
		do {
			System.out.print("\nEnter a word or 'e' to exit: ");
			input = sc.next();
			input = input.toLowerCase();
			if (input.charAt(0) == 'e' && input.length() == 1) {
				System.out.println("Exit to main page.");
				exit = true;
				break;
			}
			if (!words.containsKey(input)) {
				System.out.println("The file does not contain the given word! Please try again!");
			}
			else {
				// Counting how many times has the input word occured.
				int counter = 0;
				for(String word : allWords){
					if(word.equals(input)) 
						counter++;
				}
				
				DecimalFormat df = new DecimalFormat("0.000");
				System.out.println("The given word occured " + counter + " time" + ((counter>1)?"s":"") + ", and displayed for " + df.format(words.get(input)) + " seconds.");				
			}
			
		} while (/*!words.containsKey(input) &&*/ !exit);
		
	}
	
	public static void Shift(String path) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String str;
			
			System.out.print("\nEnter output file name: ");
			String filename = null;
			filename = sc.next();
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename + ".srt"));
			
			// In the first while loop, the input number being validated to be sure it cannot be shifted under 0.
			String from = null;
			double input = 0;
			while ((str = br.readLine()) != null) {
				if (str.contains("-->")) {					
					from = str.substring(0, (str.indexOf(" ")+1));
					from = from.replace(',', '.');
					
					// Validation phase.
					boolean valid = true;
					do {
						System.out.print("How many seconds do you want to shift? (negative also valid): ");				
						valid = true;
						try {
							input = sc.nextDouble();
						} catch (InputMismatchException e) {
							System.out.println("Please try again!");
							input = 0;
							valid = false;
							sc.next();
						}
						if (Converter(from)<-input) {
							System.out.println("The given number is greater than the first starting point!");
							valid = false;
						}
					} while (!valid);
					break;
				}
			}
			//sc.close();
			br.close();
			
			// Second while loop writes the lines and changes the SRT times if needed.
			br = new BufferedReader(new FileReader(path));
			str = null;
			from = null;
			String to = null;
			while ((str = br.readLine()) != null) {
				if (str.contains("-->")) {			
					from = str.substring(0, (str.indexOf(" ")+1));
					to = str.substring(str.lastIndexOf(" "));
					from = from.replace(',', '.');
					to = to.replace(',', '.');
					
					bw.write(ConvertBack(Converter(from) + input) + " --> " + ConvertBack(Converter(to) + input) + "\n");
				}
				else {
					bw.write(str+"\n");
				}
			}			
			bw.close();
			System.out.println("Writing file succeded.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// Convert SRT time format to seconds
	public static double Converter(String convertable) {
		double time = Double.parseDouble(convertable.substring(convertable.lastIndexOf(':')+1));
		time += 60*Double.parseDouble(convertable.substring(convertable.indexOf(':')+1, convertable.lastIndexOf(':')));
		time += 60*60*Double.parseDouble(convertable.substring(0, convertable.indexOf(':')));
		return time;
	}
	// Convert seconds to SRT time format (e.g. 00:00:04,724)
	public static String ConvertBack(double convertable) {
		double hour = 0;
		double min = 0;
		double sec = convertable;
		if (sec>60) {
			min = Math.floor(sec/60);
			sec = sec%60;
		}
		if (min>60) {
			hour = Math.floor(min/60);
			min = min%60;
		}
		
		// Bring it to normal shape.
		DecimalFormat dhour_min = new DecimalFormat("00");
		DecimalFormat dsec = new DecimalFormat("00.000");
		String output = dhour_min.format(hour) + ':' + dhour_min.format(min) + ':' + dsec.format(sec);
		output = output.replace('.', ',');
		return output;
	}
	
	public static void main(String[] args) {
		//char[] options = {'e','u','w','s'};
		char input = ' ';
		boolean uploaded = false;
		String path = null;
		do {
			if (uploaded) {
				System.out.print("\nUPLOADED FILE: " + path.substring(path.lastIndexOf('/') + 1));
			}
			System.out.print("\nOptions: e=exit, u=upload, o=observe word, s=shift time. \nChoose an option: ");
			
			String curr = null;
			try {
				curr = sc.next();
				if (curr.length() == 1) {					
					input = curr.charAt(0);
				}
				else {
					input = ' ';
				}
			} catch (Exception e) {
				System.out.println("Error occured.");
			}
			
			if (input == 'u') {		
				// Task1
				path = Upload();
				if (path != null) {
					uploaded = true;	
					words.clear();
					allWords.clear();
					LongestDisplayed(path);
				}
				else {
					uploaded = false;
				}
			}
			else if (input == 'o' && uploaded) {
				// Task2
				Observe();
			}
			else if (input == 's' && uploaded) {
				// Task3
				Shift(path);				
			}
			else if ((input == 'o' || input == 's') && !uploaded) {
				System.out.println("Before observe or shift time, please upload an SRT file!");
			}
		
		} while (input != 'e');
		
		//System.out.println(words);
		//System.out.println(allWords);
		sc.close();
		System.out.println("Exit...");
		
		// E.G. PATH
		// C:\Users\davos\git\Test-assignment\mandalorian.srt
	}
	
}

