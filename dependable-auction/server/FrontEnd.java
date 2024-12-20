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
import java.sql.Time;
import java.util.HashMap;
import java.util.Random;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;;

public class FrontEnd implements Auction{

   //remove any authentication methods..
   //remove all of these variables since frontend has to be stateless
   //Integer primaryReplica;
   //ArrayList<String> UserEmails;
   //AuctionItem[] AuctionedItems; //global variable used for my method in auctionItems
   //Integer AuctionedItemsPosition;
   //KeyPair pair;
   //HashMap<Integer,PrivateKey> privateKeyMap;
   //HashMap<Integer,String> challengeMap;
   //HashMap<Integer,PublicKey> publicKeyMap;
   //HashMap<Integer,TokenInfo> tokenMap;
   //ArrayList<Server> RunningReplicas;

   //PrivateKey serverPrivateKey;
   //PublicKey serverPublicKey;
   boolean primaryReplicaAssigned;
   int primaryReplicaID;
   int testCount;
   String primReplica;


      public FrontEnd() {
         super();
         //RunningReplicas = new ArrayList<Server>();
         primaryReplicaAssigned=false; //initially no primary replica assigned
         testCount=0;
      }


      public void AssignNewPrimary(){
          System.out.println("Assign a new primary");
          //Primary replica is dead
          try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            String[] namesRepSelect = registry.list();
            String repName="";
            int highestAge=0;
            ReplicaInterface replicaSelect;
            for(String selectingRep: namesRepSelect){
               if(selectingRep.contains("Replica")){ 
                  try {
                     replicaSelect = (ReplicaInterface) registry.lookup(selectingRep);
                     replicaSelect.getUserEmails(); //check if that replica is alive. otherwise backup
                     if(replicaSelect.getreplicaAge()>=highestAge){
                        System.out.println(primReplica +"age is:"+Integer.toString(replicaSelect.getreplicaAge()));
                        primReplica=selectingRep;///selectingRep.substring(selectingRep.lastIndexOf(":")+1);
                        repName=selectingRep;
                        highestAge=replicaSelect.getreplicaAge();
                     }
                    
                  } catch (Exception e) {
                     // If replica is dead then ignore it..
                     System.out.println("Dead replica so we ignore it");
                     continue;
                  }
               }   
         }
         //return(repName); //name of the replica with highest age, so is the primary replica
          } catch (Exception e) {
            System.out.println("assign new primary exception");
           
          }
           
         //return("");
      }  

      public boolean isPrimaryDead(){
         try {
            //First check if primary is still alive
            Registry registry = LocateRegistry.getRegistry("localhost");
            String[] namesRepSelect = registry.list();
            ReplicaInterface replica;
            
            for(String repAlive: namesRepSelect){
               if(repAlive.contains("Replica")){
                  try {
                     //System.out.println("Retrieving replica");
                     replica=(ReplicaInterface) registry.lookup(repAlive); //retrieve a replica
                     replica.getUserEmails(); //calling any method to see if still alive
                  } catch (Exception e) { //replica is dead..
                     String replicaID=repAlive;//repAlive.substring(repAlive.lastIndexOf(":")+1);
                     System.out.println("Comparing crashed replica of "+replicaID+" with "+ primReplica);
                     if(replicaID.equalsIgnoreCase(primReplica)){ //if crashed replica is a primary replica
                        System.out.println("Crashed replica is primary replica..");
                        primaryReplicaAssigned=false;
                        return(true);
                        //exit loop and now assign a primary replica
                     }
                     //if unavailable for some reason then ignore and move on..
                  }
               }
              
            }
            System.out.println("primary replica is not dead");
            return(false);
      }
      catch(Exception e){
         e.printStackTrace();
      }
      System.out.println("isprimarydead should not print here either");
      return false;
   }
   /*
   public void BackupToOtherReplicas(Replica replica,int type){
      System.out.println("Backing up to all replicas..."+Integer.toString(type));
      try {
         ArrayList<String> userEmails = replica.getUserEmails();
      AuctionItem[] auctionedItems = replica.getAuctionedItems();
      Integer auctionedItemsPosition = replica.getAuctionedItemsPosition();
      HashMap<Integer,PrivateKey> privateKeyMap = replica.getPrivateKeyMap();
      HashMap<Integer,String> challengeMap = replica.getChallengeMap();
      HashMap<Integer,PublicKey> publickeyMap = replica.getPublicKeyMap();
      HashMap<Integer,TokenInfo> tokenMap = replica.getTokenMap();
      PrivateKey serverPrivateKey = replica.getServerPrivateKey();
      PublicKey serverPublicKey = replica.getServerPublicKey();
      int replicaAge=0;
      if(type==1){ //If backing up before processing
         replicaAge = replica.getreplicaAge();
      }
      else{ //backing up after processing
         replicaAge = replica.getreplicaAge()+1;
      }
      Registry registry2 = LocateRegistry.getRegistry("localhost"); // do backup inside replica not inside frontend
      String[] names2 = registry2.list();
      for(String replicaName: names2){ //Backup to all other replicas BEFORE doing anything
         if(replicaName.contains("Replica")){ //Is replica and is not same name as primary
            System.out.println(replicaName);
            System.out.println("Backing up to this replica..");
            try {
               Replica replica2 = (Replica) registry2.lookup(replicaName);
               replica2.getUserEmails(); //check if that replica is alive. otherwise backup
               replica2.setBackUpVariables(userEmails, auctionedItems, auctionedItemsPosition, privateKeyMap, challengeMap, publickeyMap, tokenMap,serverPrivateKey,serverPublicKey,replicaAge);
            } catch (Exception e) {
               // If replica is dead then ignore it..
               System.out.println("Dead replica so we ignore it");
               continue;
            }
         }
      }
      } catch (Exception e) {
         
      }
      
   }
   */
     
     public Integer register(String email,PublicKey pubkey) throws RemoteException { //1
       
         //first find which is primary replica..if timesout or no primary replica then select one
         // to become the primary replica
         System.out.println("Started register");
         //if primary replica not assigned
         int count=0;
        while(true){  //incase of a repeating error, otherwise meant to repeat if primary crashed during here

           try {
              Registry registry = LocateRegistry.getRegistry("localhost");
              String[] names = registry.list();
              if(!primaryReplicaAssigned || isPrimaryDead()){ //If primary not assigned or is dead, assign new one
               System.out.println("primary replica not assigned or dead");  
               AssignNewPrimary(); //prim replica is now primary replica
               primaryReplicaAssigned=true;
              }
              System.out.println("Primary replica is:"+primReplica);
              ReplicaInterface replica = (ReplicaInterface)registry.lookup(primReplica);
              //Backup all values before doing anything with primary
              replica.BackupToOtherReplicas(replica,1);
            //  TimeUnit.SECONDS.sleep(3); //give time to kill a server for testing
              int registerReturn = replica.register(email, pubkey);
              replica.BackupToOtherReplicas(replica, 2); //backup with new age
              System.out.println(registerReturn);
              return(registerReturn);               
        }
        catch(RemoteException e){
         System.out.println("remote exception here because a replica is dead");
        // e.printStackTrace();
         count=count+1;
      }   
        catch(Exception e){
           System.out.println("general exception here");
          // e.printStackTrace();
          // count=count+1;
        }
              
     }
   //return 0; 
   }

      public Boolean checkToken(String token,Integer userID){
         /* 
         //first find which is primary replica..if timesout or no primary replica then select one
         // to become the primary replica
         System.out.println("Started check token");
         //if primary replica not assigned
         int count=0;
        while(count<2){ 

           try {
              Registry registry = LocateRegistry.getRegistry("localhost");
              String[] names = registry.list();
              if(!primaryReplicaAssigned || isPrimaryDead()){ //If primary not assigned or is dead, assign new one
                 AssignNewPrimary();
                 primaryReplicaAssigned=true;
              }
              System.out.println("Primary replica is:"+primReplica);
              Replica replica = (Replica)registry.lookup(primReplica);
              //Backup all values before doing anything with primary
              BackupToOtherReplicas(replica,1);
        //      TimeUnit.SECONDS.sleep(3); //give time to kill a server for testing
              Boolean registerReturn = replica.checkToken(token, userID);
              BackupToOtherReplicas(replica, 2); //backup with new age
              System.out.println(registerReturn);
              return(registerReturn);               
        }
        catch(Exception e){
           System.out.println("nested exception here");
           e.printStackTrace();
           count=count+1;
        }         
     }
     */
     return true;
   }

     public AuctionItem getSpec(int userID, int itemID,String token) throws RemoteException{
         //first find which is primary replica..if timesout or no primary replica then select one
         // to become the primary replica
         System.out.println("Started get spec");
         //if primary replica not assigned
         int count=0;
        while(true){ 

           try {
              Registry registry = LocateRegistry.getRegistry("localhost");
              String[] names = registry.list();
              if(!primaryReplicaAssigned || isPrimaryDead()){ //If primary not assigned or is dead, assign new one
                 AssignNewPrimary();
                 primaryReplicaAssigned=true;
              }
              System.out.println("Primary replica is:"+primReplica);
              ReplicaInterface replica = (ReplicaInterface)registry.lookup(primReplica);
              //Backup all values before doing anything with primary
              replica.BackupToOtherReplicas(replica,1);
        //      TimeUnit.SECONDS.sleep(3); //give time to kill a server for testing
              AuctionItem registerReturn = replica.getSpec(userID, itemID, token);
              replica.BackupToOtherReplicas(replica, 2); //backup with new age
              System.out.println(registerReturn);
              return(registerReturn);               
        }
        catch(Exception e){
           System.out.println("nested exception here");
      //     e.printStackTrace();
           count=count+1;
        }         
     }
     //return null;

   }
         
      public int getPrimaryReplicaID(){
         //METHOD NOT USED

         return 1;

      }

     public ChallengeInfo challenge(int userID, String clientChallenge) throws RemoteException{
      /* DONT USE FOR CW3
      System.out.println("starting challenge");

         //first find which is primary replica..if timesout or no primary replica then select one
         // to become the primary replica
       
         //if primary replica not assigned
         int count=0;
        while(count<2){ 

           try {
              Registry registry = LocateRegistry.getRegistry("localhost");
              String[] names = registry.list();
              if(!primaryReplicaAssigned || isPrimaryDead()){ //If primary not assigned or is dead, assign new one
                 AssignNewPrimary();
                 primaryReplicaAssigned=true;
              }
              System.out.println("Primary replica is:"+primReplica);
              Replica replica = (Replica)registry.lookup(primReplica);
              //Backup all values before doing anything with primary
              BackupToOtherReplicas(replica,1);
         //     TimeUnit.SECONDS.sleep(3); //give time to kill a server for testing
              ChallengeInfo registerReturn = replica.challenge(userID, clientChallenge);
              BackupToOtherReplicas(replica, 2); //backup with new age
              System.out.println(registerReturn);
              return(registerReturn);               
        }
        catch(Exception e){
           System.out.println("nested exception here");
           e.printStackTrace();
           count=count+1;
        }         
     }
     */
     return null;
       
     }

     public String generateChallenge(){
      //METHOD NOT USED
         return null;
      }

     public TokenInfo authenticate(int userID, byte signature[]) throws RemoteException{
      /* DONT USE FOR CW3
         //first find which is primary replica..if timesout or no primary replica then select one
         // to become the primary replica
         System.out.println("Started authenticate");
         //if primary replica not assigned
         int count=0;
        while(count<2){ 

           try {
              Registry registry = LocateRegistry.getRegistry("localhost");
              String[] names = registry.list();
              if(!primaryReplicaAssigned || isPrimaryDead()){ //If primary not assigned or is dead, assign new one
                 AssignNewPrimary();
                 primaryReplicaAssigned=true;
              }
              System.out.println("Primary replica is:"+primReplica);
              Replica replica = (Replica)registry.lookup(primReplica);
              //Backup all values before doing anything with primary
              BackupToOtherReplicas(replica,1);
         //     TimeUnit.SECONDS.sleep(3); //give time to kill a server for testing
              TokenInfo registerReturn = replica.authenticate(userID, signature);
              BackupToOtherReplicas(replica, 2); //backup with new age
              System.out.println(registerReturn);
              return(registerReturn);               
        }
        catch(Exception e){
           System.out.println("nested exception here");
           e.printStackTrace();
           count=count+1;
        }         
     }
     */
     return null;
              
     }

     public Integer newAuction(int userID, AuctionSaleItem item,String token) throws RemoteException{ //returns the auctionID
         
         
             
         //first find which is primary replica..if timesout or no primary replica then select one
         // to become the primary replica
         System.out.println("Started new auction");
         //if primary replica not assigned
         int count=0;
        while(true){ 

           try {
              Registry registry = LocateRegistry.getRegistry("localhost");
              String[] names = registry.list();
              if(!primaryReplicaAssigned || isPrimaryDead()){ //If primary not assigned or is dead, assign new one
                 AssignNewPrimary();
                 primaryReplicaAssigned=true;
              }
              System.out.println("Primary replica is:"+primReplica);
              ReplicaInterface replica = (ReplicaInterface)registry.lookup(primReplica);
              //Backup all values before doing anything with primary
              replica.BackupToOtherReplicas(replica,1);
            //  TimeUnit.SECONDS.sleep(5); //give time to kill a server for testing
              Integer registerReturn = replica.newAuction(userID, item, token);
              replica.BackupToOtherReplicas(replica, 2); //backup with new age
              System.out.println(registerReturn);
              return(registerReturn);               
        }
        catch(Exception e){
           System.out.println("nested exception here");
          // e.printStackTrace();
           count=count+1;
        }         
     }
     //return 0;
         
     }

     
     public AuctionItem[] listItems(int userID, String token) throws RemoteException{ //need to have already created auction items

         
             
         //first find which is primary replica..if timesout or no primary replica then select one
         // to become the primary replica
         System.out.println("Started list items");
         //if primary replica not assigned
         int count=0;
        while(true){ 

           try {
              Registry registry = LocateRegistry.getRegistry("localhost");
              String[] names = registry.list();
              if(!primaryReplicaAssigned || isPrimaryDead()){ //If primary not assigned or is dead, assign new one
                 AssignNewPrimary();
                 primaryReplicaAssigned=true;
              }
              System.out.println("Primary replica is:"+primReplica);
              ReplicaInterface replica = (ReplicaInterface)registry.lookup(primReplica);
              //Backup all values before doing anything with primary
              replica.BackupToOtherReplicas(replica,1);
           //   TimeUnit.SECONDS.sleep(3); //give time to kill a server for testing
              AuctionItem[] registerReturn = replica.listItems(userID, token);
              replica.BackupToOtherReplicas(replica, 2); //backup with new age
              System.out.println(registerReturn);
              return(registerReturn);               
        }
        catch(Exception e){
           System.out.println("nested exception here");
         //  e.printStackTrace();
           count=count+1;
        }         
     }
     //return null;
       
      
      
     }

   public AuctionResult closeAuction(int userID, int itemID,String token) throws RemoteException{
      
         
             
         //first find which is primary replica..if timesout or no primary replica then select one
         // to become the primary replica
         System.out.println("Started close auction");
         //if primary replica not assigned
         int count=0;
        while(true){ 

           try {
              Registry registry = LocateRegistry.getRegistry("localhost");
              String[] names = registry.list();
              if(!primaryReplicaAssigned || isPrimaryDead()){ //If primary not assigned or is dead, assign new one
                 AssignNewPrimary();
                 primaryReplicaAssigned=true;
              }
              System.out.println("Primary replica is:"+primReplica);
              ReplicaInterface replica = (ReplicaInterface)registry.lookup(primReplica);
              //Backup all values before doing anything with primary
              replica.BackupToOtherReplicas(replica,1);
        //      TimeUnit.SECONDS.sleep(3); //give time to kill a server for testing
              AuctionResult registerReturn = replica.closeAuction(userID, itemID, token);
              replica.BackupToOtherReplicas(replica, 2); //backup with new age
              System.out.println(registerReturn);
              return(registerReturn);               
        }
        catch(Exception e){
           System.out.println("nested exception here");
          // e.printStackTrace();
           count=count+1;
        }         
     }
     //return null;
    
   }

   public boolean bid(int userID, int itemID, int price,String token) throws RemoteException{ //3

         //first find which is primary replica..if timesout or no primary replica then select one
         // to become the primary replica
         System.out.println("Started bid");
         //if primary replica not assigned
         int count=0;
        while(true){ 

           try {
              Registry registry = LocateRegistry.getRegistry("localhost");
              String[] names = registry.list();
              if(!primaryReplicaAssigned || isPrimaryDead()){ //If primary not assigned or is dead, assign new one
                 AssignNewPrimary();
                 primaryReplicaAssigned=true;
              }
              System.out.println("Primary replica is:"+primReplica);
              ReplicaInterface replica = (ReplicaInterface)registry.lookup(primReplica);
              //Backup all values before doing anything with primary
              replica.BackupToOtherReplicas(replica,1);
            //  TimeUnit.SECONDS.sleep(3); //give time to kill a server for testing
              boolean registerReturn = replica.bid(userID, itemID, price, token);
              replica.BackupToOtherReplicas(replica, 2); //backup with new age
              System.out.println(registerReturn);
              return(registerReturn);               
        }
        catch(Exception e){
           System.out.println("nested exception here");
      //     e.printStackTrace();
           count=count+1;
        }         
     }
     //return false;
     
      }
      
   
    public static void main(String[] args) {
      
      try {
         FrontEnd frontend  = new FrontEnd();
         String name = "FrontEnd";
         Auction stub = (Auction) UnicastRemoteObject.exportObject(frontend, 0);
         Registry registry = LocateRegistry.getRegistry("localhost");
         registry.rebind(name, stub);
         String[] names = registry.list();
         System.out.println("FrontEnd running");
         for(String boundname: names ){
            System.out.println(boundname);
         }
         
         
        } catch (Exception e) {
         System.err.println("Exception:");
         e.printStackTrace();
        }
     }

     

  }