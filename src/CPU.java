import java.io.*;
import java.util.Scanner;

public class CPU {
	// making all registers private integers
	private static int PC;
	private static int SP;
	private static int IR;
	private static int AC;
	private static int X;
	private static int Y;
	// int to hold the number of instructions, X, before timer interrupts
	private static int timerInstructions;
	// bool to determine whether or not CPU is in kernel mode, if not, it's in user mode
	private static boolean kernelMode;
	// bool to determine whether an interrupt is occurring. Disable interrupts if so
	private static boolean interruptExists;
	// bool to determine whether it's a timer interrupt or int interrupt
	private static boolean isTimerInterrupt;
	
	// main method
	public static void main(String args[])
	{
		// initialize the values of the registers
		PC = 0; // start reading instructions at address 0
		SP = 1000; // start with user stack
		IR = 0; // no instructions loaded at first
		// these registers should start at 0 since nothing has been put in them yet
		AC = 0;
		X = 0;
		Y = 0;
		// all the flags for interrupts and kernel mode start out false
		kernelMode = false;
		interruptExists = false;
		isTimerInterrupt = false;
		
		// get name of file from command line
		String filename = args[0];
		
		// get X, the number of instructions before a timer interrupt, from command line by parsing the string into an integer
		timerInstructions = Integer.parseInt(args[1]);
		
		int userStackAddress = 1000; // user stack begins at 999 and grows down but we decrement first so start at 1000
		int systemStackAddress = 2000; // system stack begins at 1999 and grows down but we decrememnt first so start at 2000
		
		try {

			// creating a process using runtime
			Runtime rt = Runtime.getRuntime();
			// running the java file named memory and passing along the filename
			Process proc = rt.exec("java Memory " + filename);

			// connecting the input and output stream with the process
			InputStream is = proc.getInputStream(); // memory's output is inputstream
			OutputStream os = proc.getOutputStream(); // output stream goes to memory's input			

			// creating a print writer and connecting it to the output stream which is memory's input
			PrintWriter pw = new PrintWriter(os);

			// using scanner with the input stream which is memory's output
			Scanner memoryInput = new Scanner(is);

			// asking to read first line of memory which is at 0
			pw.println("Read  " + PC);
			pw.flush();
			
			// count down the timer for each instruction and have an interrupt at 0
			int countdownTimer = timerInstructions;

			// while you can continue reading in instructions, loop and read them
			while(memoryInput.hasNext())
			{				
				// update IR with the next read instruction
				updateIR(memoryInput.nextInt());
				memoryInput.nextLine();

				// decrement timer if no interrupt exists to determine if it's 0 and you need a timer interrupt
				if(interruptExists == false)
					countdownTimer--;
				PC++; // increment PC to read the next line of memory

				// if it's time for a timer interrupt and an interrupt currently doesn't exist, have a timer interrupt
				if(countdownTimer==0 && interruptExists==false)
				{
					// reset the countdown timer
					countdownTimer = timerInstructions;
					
					// indicate that an interrupt exists to prevent other interrupts
					interruptExists = true; 
					// indicate that the system is in kernel mode
					kernelMode = true;
					// indicate that it is a timer interrupt and not an system call interrupt
					isTimerInterrupt = true;
					
					// update the user stack address to the last known SP
					userStackAddress = SP;
					// change SP to the system stack address
					SP = systemStackAddress;
					
					updateStackPush(); // decrement stack for a push
					// add the user stack address to the system stack
					pw.println("Write " + SP + " " + userStackAddress);
					pw.flush();
					
					PC--; // since current instruction isn't executing due to interrupt, decrement PC so we can go back to it when returning from interrupt			
					updateStackPush(); // decrement stack for a push
					// add PC to the system stack
					pw.println("Write " + SP + " " + PC);
					pw.flush();
					
					PC = 1000; // timer interrupt causes execution at address 1000
					IR = 0; // reset the instruction register so it won't execute something
				}
				
				// integer to hold the load address for load instructions
				int loadAddress;
				// integer to hold the jump address for jump instructions
				int jumpAddress;
													
				// use switch to determine what the instruction is
				switch(IR){
				
					case 1: // load value
						// read in the value for the load
						pw.println("Read  " + PC);
						pw.flush();

						// store the value read in AC
						AC = memoryInput.nextInt();
						memoryInput.nextLine();
												
						PC++; // increment PC to read next instruction
						break;
						
					case 2: // load addr
						// read in the address for the load
						pw.println("Read  " + PC);
						pw.flush();
						loadAddress = memoryInput.nextInt();
						memoryInput.nextLine();
						
						if(kernelMode == true) // if in kernel mode, it can access user and system memory
						{
							// read in the value from the address for the load
							pw.println("Read  " + loadAddress);
							pw.flush();
							AC = memoryInput.nextInt();  // store the value into AC
							memoryInput.nextLine();

						}
						else if(kernelMode == false && loadAddress<=999) // if in user mode, it can only access user memory
						{
							// read in the value from the address for the load
							pw.println("Read  " + loadAddress);
							pw.flush();
							AC = memoryInput.nextInt(); // store the value into AC
							memoryInput.nextLine();

						}
						else // if user program tries to access system memory, exit with an error message
						{
							System.out.println("Memory violation: accessing system address " + loadAddress + " in user mode");
							exit();
						}
						
						PC++; // increment PC to read next instruction
						break;
						
					case 3: // LoadInd addr
						// read in the address for the load
						pw.println("Read  " + PC);
						pw.flush();
						loadAddress = memoryInput.nextInt();
						memoryInput.nextLine();

						
						if(kernelMode == true) // if in kernel mode, it can access user and system memory
						{
							// read in the value from the address for the load
							pw.println("Read  " + loadAddress);
							pw.flush();
							// load the new address from the address
							loadAddress = memoryInput.nextInt();
							memoryInput.nextLine();

							// read in the value from the address for the load
							pw.println("Read  " + loadAddress);
							pw.flush();
							AC = memoryInput.nextInt(); // store the value into AC
							memoryInput.nextLine();

						}
						else if(kernelMode == false && loadAddress<=999) // if in user mode, it can only access user memory
						{
							// read in the value from the address for the load
							pw.println("Read  " + loadAddress);
							pw.flush();
							// load the new address from the address
							loadAddress = memoryInput.nextInt(); 
							memoryInput.nextLine();

							
							if(loadAddress<=999) // make sure the address at the address is less than 999
							{
								// read in the value from the address for the load
								pw.println("Read  " + loadAddress);
								pw.flush();
								AC = memoryInput.nextInt(); // store the value into AC
								memoryInput.nextLine();

							}
							else // if user program tries to access system memory, exit with an error message
							{
								System.out.println("Memory violation: accessing system address " + loadAddress + " in user mode");
								exit();
							}
							
						}
						else // if user program tries to access system memory, exit with an error message
						{
							System.out.println("Memory violation: accessing system address " + loadAddress + " in user mode");
							exit();
						}
						
						PC++; // increment PC to read next instruction
						break;
						
					case 4: // LoadIdxX addr
						
						// read in the address for the load
						pw.println("Read  " + PC);
						pw.flush();
						loadAddress = memoryInput.nextInt();
						memoryInput.nextLine();

						// add X to the load address
						loadAddress += X;
						
						if(kernelMode == true) // if in kernel mode, it can access user and system memory
						{
							// read in the value from the address for the load
							pw.println("Read  " + loadAddress);
							pw.flush();
							AC = memoryInput.nextInt();  // store the value into AC
							memoryInput.nextLine();

						}
						else if(kernelMode == false && loadAddress<=999) // if in user mode, it can only access user memory
						{
							// read in the value from the address for the load
							pw.println("Read  " + loadAddress);
							pw.flush();
							AC = memoryInput.nextInt(); // store the value into AC
							memoryInput.nextLine();

						}
						else // if user program tries to access system memory, exit with an error message
						{
							System.out.println("Memory violation: accessing system address " + loadAddress + " in user mode");
							exit();
						}
						
						PC++; // increment PC to read next instruction
						break;
						
					case 5: // LoadIDxY addr

						// read in the address for the load
						pw.println("Read  " + PC);
						pw.flush();
						loadAddress = memoryInput.nextInt();
						memoryInput.nextLine();
						
						// add Y to the load address
						loadAddress += Y;
						
						if(kernelMode == true) // if in kernel mode, it can access user and system memory
						{
							// read in the value from the address for the load
							pw.println("Read  " + loadAddress);
							pw.flush();
							AC = memoryInput.nextInt();  // store the value into AC
							memoryInput.nextLine();

						}
						else if(kernelMode == false && loadAddress<=999) // if in user mode, it can only access user memory
						{
							// read in the value from the address for the load
							pw.println("Read  " + loadAddress);
							pw.flush();
							AC = memoryInput.nextInt(); // store the value into AC
							memoryInput.nextLine();

						}
						else // if user program tries to access system memory, exit with an error message
						{
							System.out.println("Memory violation: accessing system address " + loadAddress + " in user mode");
							exit();
						}
						
						PC++; // increment PC to read next instruction
						break;
						
					case 6: // LoadSpX

						// add X to SP to get the load address
						loadAddress = SP + X;
						
						if(kernelMode == true) // if in kernel mode, it can access user and system memory
						{
							// read in the value from the address for the load
							pw.println("Read  " + loadAddress);
							pw.flush();
							AC = memoryInput.nextInt();  // store the value into AC
							memoryInput.nextLine();

						}
						else if(kernelMode == false && loadAddress<=999) // if in user mode, it can only access user memory
						{
							// read in the value from the address for the load
							pw.println("Read  " + loadAddress);
							pw.flush();
							AC = memoryInput.nextInt(); // store the value into AC
							memoryInput.nextLine();

						}
						else // if user program tries to access system memory, exit with an error message
						{
							System.out.println("Memory violation: accessing system address " + loadAddress + " in user mode");
							exit();
						}
						
						break;
						
					case 7: // Store addr
						
						// read in the address for the store
						pw.println("Read  " + PC);
						pw.flush();
						
						// integer to hold the address to store to
						int storeAddress = memoryInput.nextInt();
						memoryInput.nextLine();

						
						// send the command to write along with the address to write to and the AC to memory
						pw.println("Write " + storeAddress + " " + AC);
						pw.flush();
						
						PC++; // increment PC to read next instruction
						break;
						
					case 8: // get
						
						// getting a random int 1-100 and storing in AC
						AC = (int)((Math.random()*100)+1);
						break;
						
					case 9: // Put port
						
						// read in port
						pw.println("Read  " + PC);
						pw.flush();
						// int variable to hold port
						int port = memoryInput.nextInt();
						memoryInput.nextLine();
						
						if(port == 1) // if port is 1, writes AC to screen as an int
							System.out.print(AC);
						else if(port == 2) // if port is 2, writes AC to screen as a char
							System.out.print(((char)AC));
						
						PC++; // increment PC to read next instruction

						break;
						
					case 10: // AddX
						
						// adding value in X to the AC
						AC+=X;
						break;
						
					case 11: // AddY
						
						// adding value in Y to the AC
						AC+=Y;
						break;
						
					case 12: // SubX
						
						// subtracting X from AC
						AC-=X;
						break;
						
					case 13: // SubY
						
						// subtracting Y from AC
						AC-=Y;
						break;
						
					case 14: // CopyToX
						
						// copying value in AC to X
						X = AC;
						break;
						
					case 15: //CopyFromX
						
						// copying value in X to AC
						AC = X;
						break;
						
					case 16: // CopyToY

						// copying value in AC to Y
						Y = AC;
						break;
						
					case 17: //CopyFromY
						
						// copying value in Y to AC
						AC = Y;
						break;
					
					case 18: // CopyToSp
						
						// copying value in AC to SP
						SP = AC;
						break;
						
					case 19: // CopyFromSp
						
						// copying value in SP to AC
						AC = SP;
						break;
						
					case 20: // Jump addr
						
						// read in jump address
						pw.println("Read  " + PC);
						pw.flush();
						jumpAddress = memoryInput.nextInt();
						memoryInput.nextLine();

						
						if(kernelMode == true) // if in kernel mode, it can access user and system memory
						{
							// change PC to the jump address
							PC = jumpAddress;
						}
						if(kernelMode == false && jumpAddress<=999) // if in user mode, it can only access user memory
						{
							// change PC to the jump address
							PC = jumpAddress;
						}
						else // if user program tries to access system memory, exit with an error message
						{
							System.out.println("Memory violation: accessing system address " + jumpAddress + " in user mode");
							exit();
						}
						break;
						
					case 21: // JumpIfEqual addr
						
						// if value in AC is 0, then jump to address
						if(AC == 0)
						{
							// read in jump address
							pw.println("Read  " + PC);
							pw.flush();
							jumpAddress = memoryInput.nextInt();
							memoryInput.nextLine();
							
							if(kernelMode == true) // if in kernel mode, it can access user and system memory
							{
								// change PC to the jump address
								PC = jumpAddress;
							}
							if(kernelMode == false && jumpAddress<=999) // if in user mode, it can only access user memory
							{
								// change PC to the jump address
								PC = jumpAddress;
							}
							else // if user program tries to access system memory, exit with an error message
							{
								System.out.println("Memory violation: accessing system address " + jumpAddress + " in user mode");
								exit();
							}
						}
						else
						{
							PC++;
						}
						break; 
						
					case 22: // JumpIfNotEqual addr
						
						// if value in AC is not 0, then jump to address
						if(AC != 0)
						{
							// read in jump address
							pw.println("Read  " + PC);
							pw.flush();
							jumpAddress = memoryInput.nextInt();
							memoryInput.nextLine();
							
							if(kernelMode == true) // if in kernel mode, it can access user and system memory
							{
								// change PC to the jump address
								PC = jumpAddress;
							}
							if(kernelMode == false && jumpAddress<=999) // if in user mode, it can only access user memory
							{
								// change PC to the jump address
								PC = jumpAddress;
							}
							else // if user program tries to access system memory, exit with an error message
							{
								System.out.println("Memory violation: accessing system address " + jumpAddress + " in user mode");
								exit();
							}
						}
						else
						{
							PC++;
						}
						break; 
						
					case 23: // call addr
						
						// read in jump address
						pw.println("Read  " + PC);
						pw.flush();
						jumpAddress = memoryInput.nextInt();
						memoryInput.nextLine();
						
						// decrement SP for push
						updateStackPush();
						
						// push return addr onto stack, jump to address
						PC++; // update PC to come back to next instruction after popping from stack
						pw.println("Write " + SP + " " + PC); // pushing PC to stack
						pw.flush();
						
						if(kernelMode == true) // if in kernel mode, it can access user and system memory
						{
							// change PC to the jump address
							PC = jumpAddress;
						}
						if(kernelMode == false && jumpAddress<=999) // if in user mode, it can only access user memory
						{
							// change PC to the jump address
							PC = jumpAddress;
						}
						else // if user program tries to access system memory, exit with an error message
						{
							System.out.println("Memory violation: accessing system address " + jumpAddress + " in user mode");
							exit();
						}
						break;
						
					case 24: // ret
						
						// read in return address from stack
						pw.println("Read  " + SP);
						pw.flush();
						jumpAddress = memoryInput.nextInt();
						memoryInput.nextLine();
						
						//increment SP for pop
						updateStackPop();
						
						if(kernelMode == true) // if in kernel mode, it can access user and system memory
						{
							// change PC to the jump address
							PC = jumpAddress;
						}
						if(kernelMode == false && jumpAddress<=999) // if in user mode, it can only access user memory
						{
							// change PC to the jump address
							PC = jumpAddress;
						}
						else // if user program tries to access system memory, exit with an error message
						{
							System.out.println("Memory violation: accessing system address " + jumpAddress + " in user mode");
							exit();
						}
						break;
						
					case 25: // IncX
						
						// incrementing X
						X++;
						break;

					case 26: // DecX

						// decrementing Y
						X--;
						break;
					
					case 27: // push
						
						// decrement SP for push
						updateStackPush();
												
						//pushing AC onto stack
						pw.println("Write " + SP + " " + AC);
						pw.flush();
						
						break;
						
					case 28: //pop
						
						// read in AC from stack
						pw.println("Read  " + SP);
						pw.flush();
						AC = memoryInput.nextInt();
						memoryInput.nextLine();

						// increment SP for pop
						updateStackPop();
						
						break;
						
					case 29: //int
						
						if(interruptExists==false && kernelMode==false)
						{
							kernelMode = true;
							interruptExists = true;
							
							// update the user stack address
							userStackAddress = SP;
							// update SP to the system stack address
							SP = systemStackAddress;

							// decrement SP for a push
							updateStackPush();
							
							// pop SP on the system stack
							pw.println("Write " + SP + " " + userStackAddress);
							pw.flush();

							// decrement SP for a push
							updateStackPush();

							// pop SP on the system stack
							pw.println("Write " + SP + " " + PC);
							pw.flush();

							// setting PC to 1500 for system call interrupts
							PC = 1500;
						}
						break;
						
					case 30: // IRet
						
						// return from system call
						// set kernel mode and interrupt exists to false
						kernelMode = false;
						interruptExists = false;
						
						// resetting is timer interrupt to false if there was a timer interrupt going on to indicate that the timer interrupt is done
						if(isTimerInterrupt==true)
						{
							isTimerInterrupt = false;
						}
						
						// get back PC by popping off stack
						pw.println("Read  " + SP);
						pw.flush();
						PC = memoryInput.nextInt();
						memoryInput.nextLine();
						
						// increment SP for pop
						updateStackPop();
												
						// get back the user stack pointer by popping off stack
						pw.println("Read  " + SP);
						pw.flush();
						
						// increment SP for pop
						updateStackPop();
						
						// read in old value for SP
						SP = memoryInput.nextInt();
						memoryInput.nextLine();
						
						// increment system stack address for pop
												
						break;
						
					case 50: // end
						exit();
						break;
				}

				// continue reading from memory unless it exited
				pw.println("Read  " + PC);
				pw.flush();
				
			}
			
			memoryInput.close();
			
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		
	}
	
	// method to store instructions fetched from in the IR
	public static void updateIR(int instruction)
	{
		IR = instruction;
	}
	
	// method that returns the current address of the user stack and updates it for a push
	public static void updateStackPush()
	{
		SP--; // decrement stack pointer so stack grows down
	}
	
	// method that returns the current address of the user stack and updates it for a pop
	public static void updateStackPop()
	{
		SP++; // increment stack pointer for pop
	}
	
	public static void exit()
	{
		System.exit(0);
	}

}
