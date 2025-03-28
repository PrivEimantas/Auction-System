This is split into three sepererate parrts.

Basic auction uses interfaces to invoke bids etc whilst featuring encryption, it uses Java SealedObject class, a
mechanism that allows you to encrypt and decrypt Java objects. Using
AES encryption and AES keys rather than plain passwords.

secure auction..

Implements server logic to handle creating and managing listings, and bidding on listed items.
This auctioning system suses the exact interface shown below:
public interface Auction extends Remote {
 public Integer register(String email) throws RemoteException;

 public AuctionItem getSpec(int itemID) throws RemoteException;
 public Integer newAuction(int userID, AuctionSaleItem item) throws RemoteException;
 public AuctionItem[] listItems() throws RemoteException;
public AuctionResult closeAuction(int userID, int itemID) throws RemoteException;
public boolean bid(int userID, int itemID, int price) throws RemoteException;
}
The system provides asymmetric cryptographic authentication. For this level, theauction
interface methods are slightly updated, and there are also two additional methods: challenge() and
authenticate(). 
You should use RSA keys (2048 bits) for all authentication, and you can assume that the server’s public
key has been securely stored in a file named ‘serverKey.pub’ in a ‘keys’ folder located in the root directory
of your submission. Please use the storePublicKey() method given below to store the server’s public
key. Once a client registers its username (i.e., email address and its public key) with the server, from
that point onwards, it must perform a 3-way authentication before any interaction with the auction
server. 

A 3-way authentication involves a challenge() method invocation followed by an authenticate()
method invocation by the client. The challenge() method implementation should use the server’s private
key to generate a signature on the clientChallenge string supplied by the client. The challenge() method
should then return the signature generated by the server as well as a (random) challenge for the client
as part of a ChallengeInfo objec

In the final step of the 3-way authentication, the client must generate a signature on the server’s
challenge (i.e., serverChallenge) with its private key and call the authenticate() method to send its
signature to the server. The authenticate() method implementation at the server must verify the client’s
signature, and if the verification is successful, the server must return a TokenInfo object containing a
one-time use token and its expiration time.
The server expects a valid token with any client request; otherwise, the server does not execute the
request and returns null or false (whichever is appropriate). A valid token is one that was generated by
the server for a given client, has not been used by the client before, and has not expired. In your server
code, you should set each token to expire within ten seconds. The server should use a minimal state
to validate tokens. As in Level 3, you should only use in-memory Java data structures to store any userand auction-related state on the server

The system ensures that these are not broken
1) tampering with bids (one buyer modifying or stopping another buyer’s bid),
2) closing the bidding by a user who did not create the auction,
3) accepting a bid that is the same or lower than the current bid


Dependable Auction...

To ensure the dependability of the auctioning system, you are required to enhance the
availability of your system by using replication techniques. You should implement a passive
replication system to meet these requirements, thereby increasing dependability.
The server implementation should have at least three replicas and allow the user to easily add a
new replica. You must design your server program so that any replica can function as the primary
replica. The clients will communicate with a stateless front-end program which should then
simply forward client requests to the primary replica. Your front end should initially pick one
replica and keep using that replica as the primary replica.

The server replicas may crash unexpectedly due to either hardware or software faults. Your
solution should be able to handle such failures. As long as one replica remains alive, your
auctioning system should continue to function correctly. You should be prepared to justify your
choice of design for failure detection and handling.
Your solution must allow the automated testing to kill all but one of the replicas (including the
primary replica). Even when the current primary replica fails, your system should continue to
function correctly. Your system should also allow replicas to recover and re-join the system.
Ideally, your system should work even with a complete replica turnover; that is, you want your
system to start with 3 replicas (say with IDs 1, 2, and 3), add another replica (with ID 4), kill all
original replicas (with IDs 1, 2, 3) and have the system continue to function properly.
Your front-end should initially pick a replica as the primary replica, and only in the event of a
failure detection, the front-end should select a different (i.e., a working one) replica as the
primary.
