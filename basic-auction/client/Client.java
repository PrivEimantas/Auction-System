import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import java.nio.file.Paths;
import java.nio.file.Files;

public class Client{
    public static void main(String[] args) {
        if (args.length < 1) {
        System.out.println("Usage: java Client n");
        return;
        }
 
          int n = Integer.parseInt(args[0]);
          try {
                String name = "Auction";
                Registry registry = LocateRegistry.getRegistry("localhost");
                Auction server = (Auction) registry.lookup(name);
                SealedObject result = server.getSpec(n); // fetches from server
                //use the same cipher as used for encrypting 
                Cipher cipher = Cipher.getInstance( "AES/CBC/PKCS5Padding" );
                //read in bytes of the aes encrypted key since its not plain text
                byte[] keyb = Files.readAllBytes(Paths.get("../keys/testKey.aes"));
                SecretKeySpec skey = new SecretKeySpec(keyb, "AES");
                //get iv from text file
                byte[] ivb = Files.readAllBytes(Paths.get("../keys/IVFile.txt"));
                IvParameterSpec ivspec = new IvParameterSpec(ivb);
                //if result fetched from server wasnt null then can try to read it, otherwise dont
                if(result!=null){
                  cipher.init(Cipher.DECRYPT_MODE,skey, new IvParameterSpec(ivb));
                  AuctionItem auc = (AuctionItem) result.getObject(cipher);

                  System.out.println("ItemID is " + auc.itemID );
                  System.out.println("name is is " + auc.name );
                  System.out.println("description is " + auc.description );
                  System.out.println("highestbid is " + auc.highestBid );
                }
                
               }
               catch (Exception e) {
                System.err.println("Exception:");
                e.printStackTrace();
                }
       }
  }
  