import javax.crypto.SealedObject;

public class AuctionItem implements java.io.Serializable {
    int itemID;
    String name;
    String description;
    int highestBid;

    public AuctionItem(int ItemIDx){
        this.itemID = ItemIDx;
        this.name = "Auctionitem"+Integer.toString(ItemIDx);
        this.description = "Description"+Integer.toString(ItemIDx);
        this.highestBid = ItemIDx*1;
    }

   
}