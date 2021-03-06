package Buffer;

import ConcurrenceControl.*;
import SharedMemoryUtilities.SharedMemory;

/**
 *This class implement logicals methods and functions for managing Producer and Consumer. 
 *This class read and write values into the Shared Memory for a inter-process communication
 *@author Zi Long
 *
 */
public class BoundedBuffer implements Buffer
{ 
   public static final int BUFFER_SIZE = 3;

   public static final int PRODUCER = 0; 
   public static final int CONSUMER = 1; 
   public static final int TURN = 2;   
   public static final int COUNT = 3;
   public static final int IN = 4;   
   public static final int OUT = 5;
   public static final int BUFFER_BEGIN = 6;
   private volatile int in;  // points to the next free position in the buffer
   private volatile int out; // points to the next full position in the buffer
   private volatile int count;
   private int handle;
   ConcurrenceControlPeterson control = new ConcurrenceControlPeterson();

   public BoundedBuffer(String memory_address)
   {
      handle = Integer.parseInt(memory_address);

      // variables initialisation 
      in = readIntFromSharedMemory(handle, IN);
      out = readIntFromSharedMemory(handle, OUT);
      count = 0;
   }

   /**
   *Insert a item(String) into a buffer in a Shared Memory.
   *
   *@param item The item that a producer wanna add to the buffer
   */
   // producer calls this method
   public synchronized void insert(String item) {

      try{
         boolean full = false;
         while (full == false || count >= 3){
            control.acquire(handle, PRODUCER);
            count = readIntFromSharedMemory(handle, COUNT); // critical section
            control.release(handle, PRODUCER);
            Thread.sleep(1);
            full = true;
         }

         SharedMemory.write(handle, (in+BUFFER_BEGIN), item);
         in = (in + 1) % BUFFER_SIZE;
         writeIntToSharedMemory(handle, IN, in);

         System.out.println("BOUNDEDBUFFER: Le produit " + item + "a ete enmagasine. Il y a = " + (count+1) + " produits.");
         
         control.acquire(handle, PRODUCER);
         writeIntToSharedMemory(handle, COUNT, readIntFromSharedMemory(handle, COUNT)+1); // critical section
         control.release(handle, PRODUCER);
      }
      catch(Throwable t){
         System.out.println(t);
      }
   }
   
   /**
   *Remove a item(String) from a buffer in a Shared Memory.
   *
   *@return item The item(String) read in the buffer 
   */
   // consumer calls this method
   public synchronized String remove() {

      String item = " ";

      try{
         boolean empty = false;

         while (empty == false || count <=0){
            control.acquire(handle, CONSUMER);
            count = readIntFromSharedMemory(handle, COUNT); // critical section
            control.release(handle, CONSUMER);
            Thread.sleep(1);
            empty = true;
         }
         
         item = SharedMemory.read(handle, (out+BUFFER_BEGIN));
         System.out.println("lu");
         out = (out + 1) % BUFFER_SIZE;
         writeIntToSharedMemory(handle, OUT, out);
         
         System.out.println("BOUNDEDBUFFER: Le produit " + item + "a ete consomme. Il y a = " +  (count-1) + " produits.");

         control.acquire(handle, CONSUMER);
         writeIntToSharedMemory(handle, COUNT, readIntFromSharedMemory(handle,COUNT)-1); // critical section
         control.release(handle, CONSUMER);

         
      }
      catch(Throwable t)
      {
         System.out.println(t);
      }

      return item;
   }
   /**
   *Write an integer into a String talbe of the Shared Memory
   *@param handle The memory adress
   *@param position The position of the string in SharedMemory
   *@param value The integer to write
   */
      //Reading and writing methods
   public static void writeIntToSharedMemory(int handle, int position, int value){
      String stringValue = Integer.toString(value);
      SharedMemory.write(handle, position, stringValue);
   }

   /**
   *Get an integer from a String talbe of the Shared Memory
   *@param handle The memory adress
   *@param position The position of the string in SharedMemory
   *@return The integer to get
   */
   public static int readIntFromSharedMemory(int handle, int position){
      return Integer.parseInt(SharedMemory.read(handle, position));
   }
}
