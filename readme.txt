Christy Jacob
CXJ170002

Files:

1. CPU.java
This java file is the one you should run. It contains the CPU process which creates the 
memory process using java Runtime and communicates with it. It processes instructions 
read in and performs reads and writes to the memory.

2. Memory.java
This java file contains the memory process that is created by the CPU process through 
java Runtime exec and contains an array of 2000 integers which is the memory.

3. CS 4348 Project 1 Summary.docx
This word document contains my summary of the project's purpose, implementation, and
my personal experience working on this project.

4. sample5.txt
This text file contains the sample program I have written. The sample program that I
have written is an addition table which should print this:
*  0  1  2  3  4  5  6  7  8  9
1  1  2  3  4  5  6  7  8  9  10
2  2  3  4  5  6  7  8  9  10  11
3  3  4  5  6  7  8  9  10  11  12
4  4  5  6  7  8  9  10  11  12  13
5  5  6  7  8  9  10  11  12  13  14
6  6  7  8  9  10  11  12  13  14  15
7  7  8  9  10  11  12  13  14  15  16
8  8  9  10  11  12  13  14  15  16  17
9  9  10  11  12  13  14  15  16  17  18

------------------------------------------------------------------------------------------

How To Compile And Run

Please type the following on the command line:	

javac Memory.java
javac CPU.java
java CPU <filename> <timer instructions>

Example:

javac Memory.java
javac CPU.java
java CPU sample1.txt 21

If this doesn't work, according to Professor Ozbirn, this is how to compile:

{cslinux1:~/student} /usr/local/jdk-10.0.1/bin/javac CPU.java
{cslinux1:~/student} /usr/local/jdk-10.0.1/bin/javac Memory.java
{cslinux1:~/student} /usr/local/jdk-10.0.1/bin/java CPU <filename> <timer instructions>

Example:

{cslinux1:~/student} /usr/local/jdk-10.0.1/bin/javac CPU.java
{cslinux1:~/student} /usr/local/jdk-10.0.1/bin/javac Memory.java
{cslinux1:~/student} /usr/local/jdk-10.0.1/bin/java CPU sample1.txt 30