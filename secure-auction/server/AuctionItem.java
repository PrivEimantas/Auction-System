import javax.crypto.SealedObject;

public class AuctionItem implements java.io.Serializable {
    int itemID;
    String name;
    String description;
    int highestBid;
}