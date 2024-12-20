import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.nio.file.Paths;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import java.security.SecureRandom;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.ArrayList;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class Server implements Auction{
   AuctionItem[] auctionedItems; //global variable used for my method in auctionItems
    public Server() {
      super();
     }
     
    public SealedObject getSpec(int itemID) {
     System.out.println("client request handled");

     try  {
      KeyGenerator mkeyGenerator;
      SecretKey mkey;

      AuctionItem auctionItem = auctionedItems[itemID]; //fetches item by indx, if doesnt exist throws exception which returns null
      mkeyGenerator = KeyGenerator.getInstance("AES");//get AES
      mkeyGenerator.init(256); //size of 256 for the key generating
      mkey = mkeyGenerator.generateKey(); //create the key

      // generate IV used for encrypting 
      SecureRandom srandom = new SecureRandom();
      byte [] Newiv = new byte [16];
      srandom.nextBytes( Newiv );

      //save the iv alongside key as a string, since not being the key which is decrypting, doesnt matter if this can be read
      try (FileOutputStream out = new FileOutputStream("../keys/IVFile.txt")) {
         out.write(Newiv);
         }
    
      // create cipher
      Cipher cipher = Cipher.getInstance( mkey.getAlgorithm() + "/CBC/PKCS5Padding" );
      cipher.init( Cipher.ENCRYPT_MODE, mkey, new IvParameterSpec( Newiv ) );

       byte[] encoded = mkey.getEncoded();
         //save encrypted key asa an AES file as per coursework spec
         try (FileOutputStream fos = new FileOutputStream("../keys/testKey.aes")) {
            fos.write(encoded);
         }
      
         SealedObject mso = new SealedObject(auctionItem,cipher); //final sealed object
         return mso;


     } catch (Exception e) {
      return null; //if item doesnt exist
      // TODO: handle exception
     }
      

   }


   public void auctionItems(){ //creates a set of auction items (7 here)
      this.auctionedItems = new AuctionItem[7];
      for(int i=0;i<7;i++){
         this.auctionedItems[i] =new AuctionItem(i);
      }
      
   }

    public static void main(String[] args) {
      try {
         Server s = new Server();
         String name = "Auction";
         Auction stub = (Auction) UnicastRemoteObject.exportObject(s, 0);
         Registry registry = LocateRegistry.getRegistry();
         registry.rebind(name, stub);
         s.auctionItems();
         System.out.println("Server ready");
         Server server = new Server();
        } catch (Exception e) {
         System.err.println("Exception:");
         e.printStackTrace();
        }
     }
  }