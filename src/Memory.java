import java.io.*;
import java.util.Scanner;

public class Memory{

	// declaring an array variable to hold memory
	private static int[] memoryArray;

	// main method
	public static void main(String args[]) throws IOException
	{
		// memory array is an array with 2000 integers
		memoryArray = new int[2000];
				
		// input file name is sent by processor when spawning memory process
		String inputFileName = args[0];
		
		// scanner to get input from the CPU 
		Scanner CPUInput = new Scanner(System.in);
		
		// initialize the array by reading in from memory
		initializeArray(inputFileName);

		// while CPU sends input, keep looping and doing a write or read
		while(CPUInput.hasNext())
		{
			// job tells whether the job is to read or write
			String job = CPUInput.nextLine();
			
			// gets the address and/or value to read or write to
			String addressValue = job.substring(6);
			
			if(job.contains("Read")) // if a read is specified, perform a read at the sent address
			{
				// get the address by parsing the string into an int
				int address = Integer.parseInt(addressValue);
				
				// use the read method to read value from memory and send it to process using system.out.println
				read(address);
			}
			else  // if a write is specified, perform a write at the sent address with the sent value
			{
				// there's a space between the address and value so find that index
				int indexOfSpace = addressValue.indexOf(' ');
				
				// address is first number so parse it into an int
				int address = Integer.parseInt(addressValue.substring(0,indexOfSpace));

				// value is the second number after the space so parse it into an int as well
				int value = Integer.parseInt(addressValue.substring(indexOfSpace+1));

				// use write method to write the value specified to memory
				write(value, address);
			}
			
		}
		
		CPUInput.close();
		
	}

	
	// method to initialize the memory array by reading a program file
	public static void initializeArray(String filename)  throws IOException
	{
		// using Scanner to read a file
		File inputFile = new File(filename);
		Scanner memory = new Scanner(inputFile);

		// integer to keep track of the current memory address
		int currentMemoryAddress = 0;
		
		// while the input file has lines to read, continue looping and reading them in
		while(memory.hasNextLine())
		{
			// getting the current line of memory
			String currentLine = memory.nextLine();
			int currentLineIndex; // keeps track of what index of the current line we are reading in
			
			// if currentLine is blank don't do anything
			if(currentLine.length()<1)
			{
				
			}
			// if line starts with a period, you have to change the current location in memory
			else if(currentLine.charAt(0)=='.')
			{
				// start with the digit after the period and read in the complete integer
				currentLineIndex = 1; // keeping track of the index of the string to read in the complete integer

				// integer to hold the new address
				int newAddress = 0;

				// while there is an integer to be read, keep reading integers to get the new address
				while(currentLineIndex<currentLine.length() && (((int)currentLine.charAt(currentLineIndex))>=48)&&(((int)currentLine.charAt(currentLineIndex))<=57))
				{
					// multiply the current address by 10 since we're reading left to right
					newAddress *= 10;
					// add the current integer to the address
					newAddress += ((int)currentLine.charAt(currentLineIndex))-48;
					currentLineIndex++; // increment so we can see if next index has an integer
				}
				// update the current memory address and current array index to the new memory address
				currentMemoryAddress = newAddress;
			}
			else if(((int)currentLine.charAt(0))>=48 && ((int)currentLine.charAt(0))<=57)// if the line doesn't start with a period or is blank then read in the instruction's number
			{
				// start with at index 0 of the current line since that's where the leftmost digit of the integer is
				currentLineIndex = 0;
				// integer to hold the value of the instruction
				int value = 0;
				// while the character at the current index is a number keep reading in integers
				while(currentLineIndex<currentLine.length() && (((int)currentLine.charAt(currentLineIndex))>=48)&&(((int)currentLine.charAt(currentLineIndex))<=57))
				{
					// multiply the current value by 10 since we're reading left to right
					value *= 10;
					// add the current integer to the value
					value += ((int)currentLine.charAt(currentLineIndex))-48;
					currentLineIndex++; // increment so we can see if next index has an integer
				}
				// update the value of the memory at the current array index
				memoryArray[currentMemoryAddress] = value;
				// increment the current memory address
				currentMemoryAddress++;
			}
			else // if current line doesn't start with a period or a number, don't do anything
			{
				
			}
		}

		// close the scanner
		memory.close();
	}

	// read method returns the value at the passed address
	public static void read(int address)
	{
		System.out.println(memoryArray[address]);
	}

	// write method writes the passed data to the passed address
	public static void write(int data, int address)
	{
		memoryArray[address] = data;
	}


}
