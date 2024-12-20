import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import java.nio.file.Paths;
import java.nio.file.Files;

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
import java.security.Signature;
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
import java.util.Random;
import java.nio.charset.Charset;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.nio.file.Path;


public class Client{
 String email;
 Integer userId;
 Auction server;
 String clientsideToken;
 PrivateKey clientsidePrivateKey;
 PublicKey clientsidePublicKey;
 KeyPair pair;
 Client client;
 

    public static void main(String[] args) {
        //  Scanner myObj = new Scanner(System.in);
        //  System.out.println("Type what to test..");
         // String input = myObj.nextLine();

          //Either register..
          Client client = new Client();
      
          try {
            String name = "Auction";
            Registry registry = LocateRegistry.getRegistry("localhost");
            client.server = (Auction) registry.lookup(name);
            client.running(client);
          } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
          }

        
       }

       public void running(Client client){
      

        while(true){ //run loop so client doesnt close after one transaction
          Scanner myObj = new Scanner(System.in);
          System.out.println("Type what to test..");
          String input = myObj.nextLine();
          
          try {
            if("register".equalsIgnoreCase(input)){
              client.register(server,client);
            }
  
            if("getspec".equalsIgnoreCase(input)){
              client.getSpec(server,client);
            }
  
            if("newauction".equalsIgnoreCase(input)){
              client.newAuction(server,client);
            }
  
            if("listitems".equalsIgnoreCase(input)){
              client.listItems(server,client);
            }
  
            if("closeauction".equalsIgnoreCase(input)){
              client.closeAuction(server,client);
            }
  
            if("bid".equalsIgnoreCase(input)){
              client.bid(server,client);
            }
          } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
          }

          

        }
       }

       
       public void generateNewToken(Client client){
        Integer userId = client.getUserId();
        String challenge = client.generateChallenge(); //gen random 12 letter string
      
        byte[] bytes =challenge.getBytes();
        try {
          ChallengeInfo a = server.challenge(userId,challenge); //once receives serverSignaute and serveChallenge
        
          PublicKey serverPubKey = client.readServerPublicKey();
          Signature sign = Signature.getInstance("SHA256withRSA");
          sign.initVerify(serverPubKey);
          sign.update(bytes); //performs signature and checking for challenges between the server and client
          boolean bool = sign.verify(a.response);
            if(bool){
            byte[] signature = null;
            Signature clientSignature=Signature.getInstance("SHA256withRSA");
            clientSignature.initSign(client.getKeyPair().getPrivate());
            clientSignature.update(a.clientChallenge.getBytes("UTF-8"));
            signature = clientSignature.sign();
            TokenInfo clientTokenInfo = server.authenticate(userId,signature);
            clientsideToken = clientTokenInfo.token;
            
           
          }
        } catch (Exception e) {
          e.printStackTrace();
          // TODO: handle exception
        }
       
      }

      public void setKeyPair(KeyPair myKeyPair){
        this.pair = myKeyPair;
      }

      public KeyPair getKeyPair(){
        return(this.pair);
      }

      public void setUserId(Integer myuserId){
        this.userId=myuserId;
      }
      
      public Integer getUserId(){
        return(this.userId);
      }

      public String getEmail(){
        return(this.email);
      }
      public void setEmail(String myemail){
        this.email = myemail;
      }
      
       public void register(Auction server,Client client){
         // now need to generate priv-c,pub-c, can get public key from file

         try {
          Scanner o = new Scanner(System.in);
         System.out.println("enter an email");
         this.email = o.nextLine();
          client.setEmail(email);

          //generate set of keys
         KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
         generator.initialize(2048);
         pair = generator.generateKeyPair();
         clientsidePublicKey = pair.getPublic();
         clientsidePrivateKey = pair.getPrivate();
         client.setKeyPair(pair);

         
        userId=server.register(email,clientsidePublicKey);
        if(userId==null){ //error handling
          System.out.println("user already exists");
        }
        else{
          client.setUserId(userId);
        
          client.generateNewToken(client);
        }
        


         } catch (Exception e) {
          e.printStackTrace();
          // TODO: handle exception
         }

              
         
       }

       public void getSpec(Auction server,Client client){
        try {
          Scanner o = new Scanner(System.in);
          System.out.println("enter Id of auction item");
          Integer inp = Integer.parseInt(o.nextLine());
          if(client.getUserId()!=null){ // user must register first..
            AuctionItem specItem= server.getSpec(client.getUserId(),inp,clientsideToken);
        
            if(specItem==null){ //either result of bad token or 
              System.out.println("invalid token..generating new one");
              client.generateNewToken(client);
              specItem= server.getSpec(client.getUserId(),inp,clientsideToken);
              if(specItem==null){ //if still invalid, list must be empty
                System.out.println("list is empty");
              }

            }
          }
          else{
            System.out.println("Register first");
          }
          
        } catch (Exception e) {
          e.printStackTrace();
          // TODO: handle exception
        }
     
       }

       public void newAuction(Auction server,Client client){
        try {
    
        AuctionSaleItem newItem = new AuctionSaleItem();
        newItem.description = this.email; //name of owner
        if(client.getUserId()!=null){
          newItem.name = Integer.toString(client.getUserId()); // name of user holding bid
          newItem.reservePrice = 10; //hardcoded set of values
          Integer auctionID=0;
          
          auctionID = server.newAuction(client.getUserId(), newItem,clientsideToken); // add token as argument to each one now
          if(auctionID==null){
            System.out.println("invalid token..generating new one");
            client.generateNewToken(client);
            auctionID = server.newAuction(client.getUserId(), newItem,clientsideToken);
            System.out.println("Auction ID is..: "+ Integer.toString(auctionID));

          }else{
            System.out.println("Auction ID is..: "+ Integer.toString(auctionID));
          }
        }
        else{
          System.out.println("Register first");
        }
    
        
        } catch (Exception e) {
          e.printStackTrace();
          // TODO: handle exception
        }
        
       }

       public void listItems(Auction server,Client client){
        try {
          if(client.getUserId()!=null){ //ccheck if a user has registered yet.. otherwise dont
            AuctionItem[] items = server.listItems(userId,clientsideToken);
            // TODO: handle exception
             if(items==null){
               System.out.println("invalid token.. generating new one");
               client.generateNewToken(client);
               items = server.listItems(userId,clientsideToken);
               if(items==null){
                 System.out.println("current list is empty");
               }
             }
          }
          else{
            System.out.println("Register first");
          }
         
        } catch (Exception e) {
          e.printStackTrace();
          // TODO: handle exception
        }
        
       
       }

       public void bid(Auction server,Client client){
        
          Scanner o = new Scanner(System.in);
          System.out.println("enter bid amount");
          Integer bidAmount = Integer.parseInt(o.nextLine());
  
          Scanner o2 = new Scanner(System.in); // NO userID input, stored by client and behind what user sees
          System.out.println("enter auction id item");
          Integer auctionId = Integer.parseInt(o.nextLine());
  
        if(client.getUserId()!=null){ //checking if a user has registered yet
          Integer userid = client.getUserId(); //Integer.parseInt(o.nextLine());
         

          try {
            Boolean bidding = server.bid(userid,auctionId,bidAmount,clientsideToken);
         
          if(bidding==false){ //if returns invalid, either result of token or bad bid
           
            System.out.println("Invalid token.. regenerating");
            client.generateNewToken(client);
            bidding = server.bid(userid,auctionId,bidAmount,clientsideToken);
            if(bidding==false){ //if still returns bad value, then the bid is low
              System.out.println("Bid is too low");
            }
            else{
              System.out.println("Bid has been made"); //otherwise is made
            }
          }
          else{ //assuming token is still valid and not expired
            System.out.println("Bid has been successful");
          }
          } catch (Exception e) {
            System.out.println("invalid token");
            e.printStackTrace();
            // TODO: handle exception
          }
        }
        else{
          System.out.println("Register first");
        }
          
          
     
       }

       public void closeAuction(Auction server,Client client){
          //fetch user input for which item to close.. but not for userid, each client stores this without user input
          Scanner o = new Scanner(System.in);
          System.out.println("enter Id of auction item to close");
          Integer inp = Integer.parseInt(o.nextLine());
          try {
            AuctionResult closedAuctionResult= server.closeAuction(client.getUserId(),inp,clientsideToken);
          if(closedAuctionResult==null){ //if returns null then invalid token or invalid result
            client.generateNewToken(client);
            closedAuctionResult= server.closeAuction(client.getUserId(),inp,clientsideToken);
            if(closedAuctionResult==null){ //if even after new token is generated and still invalid, must be invalid values
              System.out.println("Tried to close an auction that is not yours");
            }
          }
       
          } catch (Exception e) {
            e.printStackTrace();
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

       public PublicKey readServerPublicKey(){

        try{ //gets path from serverkey in folder and reads it.. assuming folders same root dir
          Path path = Paths.get("../keys/serverKey.pub");
          byte[] bytes = Files.readAllBytes(path);
          String s = new String(bytes);
          bytes = Base64.getDecoder().decode(s.getBytes());
        
          /* Generate public key. */ //returns the same one
          X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
          KeyFactory kf = KeyFactory.getInstance("RSA");
          PublicKey pub = kf.generatePublic(ks);

          return pub;
       
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
        
       }
      
  }
  