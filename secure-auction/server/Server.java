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
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.io.FileOutputStream;
import java.util.Base64;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.HashMap;
import java.util.Random;
import java.nio.charset.Charset;

public class Server implements Auction{


   ArrayList<String> UserEmails;
   AuctionItem[] AuctionedItems; //global variable used for my method in auctionItems
   Integer AuctionedItemsPosition;
   KeyPair pair;
   HashMap<Integer,PrivateKey> privateKeyMap;
   HashMap<Integer,String> challengeMap;
   HashMap<Integer,PublicKey> publicKeyMap;
   HashMap<Integer,TokenInfo> tokenMap;

   PrivateKey serverPrivateKey;
   PublicKey serverPublicKey;


      public Server() {
         super();
         UserEmails = new ArrayList<String>();
         AuctionedItems = new AuctionItem[100];
         AuctionedItemsPosition = 0;
         privateKeyMap = new HashMap<Integer, PrivateKey>();
         publicKeyMap = new HashMap<Integer, PublicKey>();
         challengeMap = new HashMap<Integer, String>();
         tokenMap = new HashMap<Integer,TokenInfo>();
         
      }
     
     public Integer register(String email,PublicKey pubkey) throws RemoteException { //1

      
         //check if email already exists, if not then add

         if(!UserEmails.contains(email)){
            
            UserEmails.add(email); //else add at the end
            
            System.out.println("Registered as "+email+" .. userID: " +Integer.toString(UserEmails.size()-1));
            System.out.println("\n");


            //Once user registers their email, create keys for server as well
            
            try {
               KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
               generator.initialize(2048);
               this.pair = generator.generateKeyPair();
               serverPublicKey = pair.getPublic();
               publicKeyMap.put(UserEmails.size()-1,pubkey); //public key of every client in system
               serverPrivateKey = pair.getPrivate();
               storePublicKey(serverPublicKey, "../keys/serverKey.pub"); //pass in userID
            } catch (Exception e) {
               // TODO: handle exception
               System.err.println("Exception:");
                e.printStackTrace();
            }

            
            System.out.println("New user created");
            return UserEmails.size()-1; //return userID
            
         }
         else{
            System.out.println("Email already exists");
            return null;//else return null as cant be placed
         }
         
      }

      public Boolean checkToken(String token,Integer userID){
         System.out.println("Checking token..");
         
         if(token==null || tokenMap.get(userID)==null){ //checks if token is null or if fetched token is null
            System.out.println("Token does not exist");
            return false;
         }
         else if(tokenMap.get(userID).expiryTime > System.currentTimeMillis()){ //check if time elapsed
            System.out.println("Token is valid");
            tokenMap.remove(userID);
            return true;
         }
         else{
            System.out.println("Token is invalid");
            return false;
         }
      }

    
      //return a user Id which is position of email in UserEmails
     
     public AuctionItem getSpec(int userID, int itemID,String token) throws RemoteException{
    
      if(checkToken(token, userID)){
      
         AuctionItem RequestedAuctionItem = AuctionedItems[itemID];
          
         if(RequestedAuctionItem==null){
            System.out.println("Item does not exist");
            return null;
         }else{
            System.out.println("\n");
            System.out.println("Getting spec of requested item");
            System.out.println("ItemID: "+RequestedAuctionItem.itemID );  
            System.out.println("Description, bid creator: " + RequestedAuctionItem.description);
            System.out.println("HighestBid: " + RequestedAuctionItem.highestBid);
            System.out.println("Name: "+RequestedAuctionItem.name);
            System.out.println("\n");
            return RequestedAuctionItem; //returns requested auction item
         }
         
      }
        else{
         
         return null;
        }
      
     

      }
      
      

     public ChallengeInfo challenge(int userID, String clientChallenge) throws RemoteException{
      try {
         //Creates challenge and maps this.. handles multiple clients at the same time for their challenges
         byte[] signature = null;
         Signature serverSignature = Signature.getInstance("SHA256withRSA");
         PrivateKey privateKey = this.serverPrivateKey;
         serverSignature.initSign(privateKey);
         serverSignature.update(clientChallenge.getBytes("UTF-8"));
         signature = serverSignature.sign();
   
         String serverChallenge = generateChallenge();
         challengeMap.put(userID,serverChallenge);
         ChallengeInfo a = new ChallengeInfo();
         a.response=signature;
         a.clientChallenge=serverChallenge;

         return a;
      } catch (Exception e) {
         System.err.println("Exception:");
         e.printStackTrace();
         return null;
         // TODO: handle exception
      }
     
     }

     public String generateChallenge(){
      int mysize=12; //size of length of the string generated along with the string below
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"+"0123456789"+"abcdefghijklmnopqrstuvxyz"; 
        StringBuilder myStringBuilder = new StringBuilder(mysize); //class for building my string
        for (int i = 0; i < mysize; i++) {  // run a loop for 12 times here and generate
         int index = (int)(AlphaNumericString.length() * Math.random()); 
         myStringBuilder.append(AlphaNumericString .charAt(index)); 
        } 
        return myStringBuilder.toString();
      }

     
     
     public TokenInfo authenticate(int userID, byte signature[]) throws RemoteException{
      try {
         // Performs checking and challenges between server and client.. at end creates random string for a token and sets an expiration time
         String serverChallenge = challengeMap.get(userID);
         byte[] bytes =serverChallenge.getBytes();
         Signature sign = Signature.getInstance("SHA256withRSA");
         PublicKey pubkey = publicKeyMap.get(userID);
         sign.initVerify(pubkey);
         sign.update(bytes);
         boolean bool = sign.verify(signature);
        
         TokenInfo a= new TokenInfo();
         String tokenString = generateChallenge();
         a.token = tokenString;
         long tokenTime = System.currentTimeMillis()+10000; //10 seconds plus current time
         a.expiryTime=tokenTime;
         tokenMap.put(userID, a);
         return a;
      } catch (Exception e) {
            //Invalid
            e.printStackTrace();
            return null;
      }   
 
     }

     public Integer newAuction(int userID, AuctionSaleItem item,String token) throws RemoteException{ //returns the auctionID
         

      
        if(checkToken(token,userID)){

         //Checks if item is valid to begin with.. otherwise just creates a new AuctionItem based on arguments given
         AuctionItem newAuctionItem = new AuctionItem();
         if(AuctionedItems[AuctionedItemsPosition]==null){
            if(AuctionedItemsPosition==0){
            
               newAuctionItem.itemID = 0;
               
            }
            else{
              
               newAuctionItem.itemID = AuctionedItemsPosition; //cant use userID since user could bid multiple items
            }
            
            newAuctionItem.name = item.name;
            newAuctionItem.highestBid = item.reservePrice;
            newAuctionItem.description = item.description;

            AuctionedItems[AuctionedItemsPosition] = newAuctionItem;
            
            AuctionedItemsPosition++;

            return AuctionedItemsPosition-1; //return AuctionID item

         }

         else{
            AuctionedItemsPosition++;
            return null;// invalid, cant place item so say on return
         }
      }
      else{
         
         return null;
      }
         
     }

     
     public AuctionItem[] listItems(int userID, String token) throws RemoteException{ //need to have already created auction items
      if(checkToken(token,userID)){
         System.out.println("\n");
         System.out.print("Listing all items.. \n");

         Integer pos = 0; //Lists all items currently in bidding
         while(AuctionedItems[pos] !=null){
            AuctionItem item = AuctionedItems[pos];
            System.out.println("ItemID: "+item.itemID );  
            System.out.println("Description,bid creator: " + item.description);
            System.out.println("HighestBid: " + item.highestBid);
            System.out.println("Name: "+item.name);
            pos++;
            
         }
         System.out.println("\n");
         return AuctionedItems; //on return, can print all of the items
      }
      else{
         return null;
      }
      
     }

   public AuctionResult closeAuction(int userID, int itemID,String token) throws RemoteException{
      if(checkToken(token,userID)){
        
         // Checks if item actually exists first.. then checks if user trying to close is the
        // one who created it..
         if( AuctionedItems[itemID]!=null && AuctionedItems[itemID].description.equalsIgnoreCase(UserEmails.get(userID))  ){
            System.out.println("closing auction made by creator");
            String winningEmail = AuctionedItems[itemID].name;
            System.out.println("Winning email: "+winningEmail);
         
            System.out.println("Winning price: "+Integer.toString(AuctionedItems[itemID].highestBid));
       
            AuctionResult result = new AuctionResult();
            result.winningEmail = AuctionedItems[itemID].name;
            result.winningPrice = AuctionedItems[itemID].highestBid;

            AuctionedItems[itemID] = null; //sets to null
            //On return, print out values which tell who won
            
            return result;
         }
         else{
            System.out.println("Auctioned tried to be closed by someone who did not create auction or token was invalid");
            return null;
         }
         
      }
      else{
         return null;
      }
    
   }

   public boolean bid(int userID, int itemID, int price,String token) throws RemoteException{ //3
      if(checkToken(token,userID)){
         if(AuctionedItems[itemID]!=null &&  price > AuctionedItems[itemID].highestBid){
          
            AuctionedItems[itemID].highestBid = price;
            AuctionedItems[itemID].name = UserEmails.get(userID);
            System.out.println(UserEmails.get(userID));
            System.out.println("Bid has been made!");
            return true;
         }
         else{
         System.out.println("Bid was failed");
         return false;
         
         //on return tell if bid was successful or not
         }
      }
      else{ //unsuccessful
         System.out.println("Token was invalid");
         return false;
      }
     
   }

   // Method to write a public key to a file.
   // Example use: storePublicKey(aPublicKey, ‘../keys/serverKey.pub’)
   public void storePublicKey(PublicKey publicKey, String filePath) throws Exception {
      // Convert the public key to a byte array
      byte[] publicKeyBytes = publicKey.getEncoded(); // Encode the public key bytes as Base64
      String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKeyBytes);

      // Write the Base64 encoded public key to a file
      try (FileOutputStream fos = new FileOutputStream(filePath)) {
         fos.write(publicKeyBase64.getBytes());
      }
   }
   
    public static void main(String[] args) {
      try {
         Server s = new Server();
         String name = "Auction";
         Auction stub = (Auction) UnicastRemoteObject.exportObject(s, 0);
         Registry registry = LocateRegistry.getRegistry();
         registry.rebind(name, stub);
         //s.auctionItems();
         System.out.println("Server ready");
         Server server = new Server();
        } catch (Exception e) {
         System.err.println("Exception:");
         e.printStackTrace();
        }
     }
  }